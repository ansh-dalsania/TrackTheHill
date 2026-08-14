package com.trackthehill.service;

import com.trackthehill.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pulls Senate roll call vote data from Voteview's bulk CSV exports and syncs
 * it into the vote and member_vote tables. Unlike Congress.gov (House data),
 * Senate.gov blocks programmatic access to its vote XML feeds, so Voteview
 * (an academic aggregator run by UCLA) is used as the Senate data source instead.
 *
 * Voteview's files cover ALL congresses since 1789, so each file is streamed
 * and filtered down to just the target congress/chamber rather than loaded
 * into memory in full — HSall_votes.csv alone is ~700MB.
 *
 * The whole sync is wrapped in a retry loop, since streaming a 700MB file
 * over a long-running sync (now longer, due to per-vote bill fallback fetches)
 * occasionally hits a connection reset partway through.
 */
@Service
public class SenateVoteSyncService {

    private final VoteRepository voteRepository;
    private final MemberVoteRepository memberVoteRepository;
    private final MemberRepository memberRepository;
    private final BillSyncService billSyncService;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private static final String MEMBERS_URL = "https://voteview.com/static/data/out/members/HSall_members.csv";
    private static final String ROLLCALLS_URL = "https://voteview.com/static/data/out/rollcalls/HSall_rollcalls.csv";
    private static final String VOTES_URL = "https://voteview.com/static/data/out/votes/HSall_votes.csv";

    public SenateVoteSyncService(VoteRepository voteRepository,
                                  MemberVoteRepository memberVoteRepository,
                                  MemberRepository memberRepository,
                                  BillSyncService billSyncService) {
        this.voteRepository = voteRepository;
        this.memberVoteRepository = memberVoteRepository;
        this.memberRepository = memberRepository;
        this.billSyncService = billSyncService;
    }

    /**
     * Public entry point — retries the full sync up to 3 times if a network
     * error (e.g. connection reset mid-stream) interrupts it. Safe to retry
     * from scratch since all writes are upserts.
     */
    public void syncSenateVotes(int congress) throws Exception {
        int maxAttempts = 3;
        Exception lastError = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                attemptSync(congress);
                return; // success
            } catch (Exception e) {
                lastError = e;
                System.out.println("Senate sync attempt " + attempt + " failed: " + e.getMessage() + ". Retrying...");
                Thread.sleep(5000);
            }
        }
        throw lastError;
    }

    private void attemptSync(int congress) throws Exception {
        Map<String, String> icpsrToBioguide = buildMemberMap(congress);
        System.out.println("Loaded " + icpsrToBioguide.size() + " Senate member mappings.");

        Map<Integer, Vote> rollnumberToVote = syncRollCalls(congress);
        System.out.println("Loaded " + rollnumberToVote.size() + " Senate roll calls.");

        int memberVoteCount = syncMemberVotes(congress, icpsrToBioguide, rollnumberToVote);
        System.out.println("Senate vote sync complete. Member votes saved: " + memberVoteCount);
    }

    /** Streams HSall_members.csv, returns a map of icpsr -> bioguideId for the given congress's Senate. */
    private Map<String, String> buildMemberMap(int congress) throws Exception {
        Map<String, String> map = new HashMap<>();
        HttpRequest request = HttpRequest.newBuilder(URI.create(MEMBERS_URL)).GET().build();
        HttpResponse<java.io.InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        try (InputStreamReader reader = new InputStreamReader(response.body(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {

            for (CSVRecord record : parser) {
                if (Integer.parseInt(record.get("congress")) != congress) continue;
                if (!"Senate".equals(record.get("chamber"))) continue;

                String bioguideId = record.get("bioguide_id");
                if (bioguideId != null && !bioguideId.isBlank()) {
                    map.put(record.get("icpsr"), bioguideId);
                }
            }
        }
        return map;
    }

    /** Streams HSall_rollcalls.csv, saves Vote rows, returns a map of rollnumber -> Vote for lookup during member-vote processing. */
    private Map<Integer, Vote> syncRollCalls(int congress) throws Exception {
        Map<Integer, Vote> rollnumberToVote = new HashMap<>();
        HttpRequest request = HttpRequest.newBuilder(URI.create(ROLLCALLS_URL)).GET().build();
        HttpResponse<java.io.InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        try (InputStreamReader reader = new InputStreamReader(response.body(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {

            for (CSVRecord record : parser) {
                if (Integer.parseInt(record.get("congress")) != congress) continue;
                if (!"Senate".equals(record.get("chamber"))) continue;

                int rollnumber = Integer.parseInt(record.get("rollnumber"));
                int session = Integer.parseInt(record.get("session"));
                String voteId = "S-" + congress + "-" + session + "-" + rollnumber;

                Vote vote = voteRepository.findById(voteId).orElse(new Vote());
                vote.setId(voteId);
                vote.setCongress(congress);
                vote.setSession(session);
                vote.setRollCallNumber(rollnumber);
                vote.setChamber("Senate");
                vote.setVoteQuestion(getOrNull(record, "vote_desc"));
                vote.setResult(getOrNull(record, "vote_result"));

                String date = getOrNull(record, "date");
                if (date != null) {
                    vote.setVoteDate(LocalDate.parse(date).atStartOfDay());
                }

                String billNumber = getOrNull(record, "bill_number");
                if (billNumber != null && !billNumber.startsWith("PN")) {
                    Bill linkedBill = parseAndFindBill(billNumber, congress);
                    if (linkedBill != null) {
                        vote.setBill(linkedBill);
                    }
                }

                vote.setCreatedAt(vote.getCreatedAt() != null ? vote.getCreatedAt() : LocalDateTime.now());
                vote.setUpdatedAt(LocalDateTime.now());
                voteRepository.save(vote);

                rollnumberToVote.put(rollnumber, vote);
            }
        }
        return rollnumberToVote;
    }

    /** Streams HSall_votes.csv (~700MB, all history) and saves only the rows matching the target congress + Senate. */
    private int syncMemberVotes(int congress, Map<String, String> icpsrToBioguide, Map<Integer, Vote> rollnumberToVote) throws Exception {
        int count = 0;
        HttpRequest request = HttpRequest.newBuilder(URI.create(VOTES_URL)).GET().build();
        HttpResponse<java.io.InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        try (InputStreamReader reader = new InputStreamReader(response.body(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {

            for (CSVRecord record : parser) {
                if (Integer.parseInt(record.get("congress")) != congress) continue;
                if (!"Senate".equals(record.get("chamber"))) continue;

                String icpsr = record.get("icpsr");
                String bioguideId = icpsrToBioguide.get(icpsr);
                if (bioguideId == null) continue;

                var memberOpt = memberRepository.findById(bioguideId);
                if (memberOpt.isEmpty()) continue;

                int rollnumber = Integer.parseInt(record.get("rollnumber"));
                Vote vote = rollnumberToVote.get(rollnumber);
                if (vote == null) continue;

                int castCode = Integer.parseInt(record.get("cast_code"));
                String position = mapCastCode(castCode);
                if (position == null) continue;

                MemberVote memberVote = memberVoteRepository
                        .findByVoteIdAndMemberBioguideId(vote.getId(), bioguideId)
                        .orElse(new MemberVote());
                memberVote.setVote(vote);
                memberVote.setMember(memberOpt.get());
                memberVote.setPosition(position);
                if (memberVote.getCreatedAt() == null) {
                    memberVote.setCreatedAt(LocalDateTime.now());
                }
                memberVoteRepository.save(memberVote);
                count++;
            }
        }
        return count;
    }

    /** Maps Voteview's numeric cast_code to a plain-text position, matching the House data's format. */
    private String mapCastCode(int castCode) {
        return switch (castCode) {
            case 1, 2, 3 -> "Yea";
            case 4, 5, 6 -> "Nay";
            case 7, 8 -> "Present";
            case 9 -> "Not Voting";
            default -> null;
        };
    }

    /**
     * Parses Voteview's bill_number format (e.g. "S5", "SJRES12") into our
     * bill.id scheme and looks it up — fetching and saving it from Congress.gov
     * on the spot if it's not already in our table. Presidential Nominations
     * (prefixed "PN") are filtered out before this is ever called.
     */
    private Bill parseAndFindBill(String billNumber, int congress) {
        Matcher matcher = Pattern.compile("^([A-Z]+)(\\d+)$").matcher(billNumber);
        if (!matcher.matches()) return null;

        String billType = matcher.group(1).toLowerCase();
        int number = Integer.parseInt(matcher.group(2));

        return billSyncService.fetchAndSaveBillIfMissing(congress, billType, number);
    }

    private String getOrNull(CSVRecord record, String column) {
        try {
            String value = record.get(column);
            return (value == null || value.isBlank()) ? null : value;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}