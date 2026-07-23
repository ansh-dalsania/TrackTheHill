package com.trackthehill.service;

import com.trackthehill.Member;
import com.trackthehill.MemberRepository;
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

/**
 * Pulls DW-NOMINATE ideological scores from Voteview's HSall_members.csv
 * and applies them to existing Member rows.
 */
@Service
public class NominateScoreSyncService {

    private final MemberRepository memberRepository;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String MEMBERS_URL = "https://voteview.com/static/data/out/members/HSall_members.csv";

    public NominateScoreSyncService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public void syncNominateScores(int congress) throws Exception {
        int updatedCount = 0;
        HttpRequest request = HttpRequest.newBuilder(URI.create(MEMBERS_URL)).GET().build();
        HttpResponse<java.io.InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        try (InputStreamReader reader = new InputStreamReader(response.body(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {

            for (CSVRecord record : parser) {
                if (Integer.parseInt(record.get("congress")) != congress) continue;

                String bioguideId = record.get("bioguide_id");
                if (bioguideId == null || bioguideId.isBlank()) continue;

                var memberOpt = memberRepository.findById(bioguideId);
                if (memberOpt.isEmpty()) continue; // skip members not in our roster

                Member member = memberOpt.get();
                member.setNominateDim1(parseDoubleOrNull(record, "nominate_dim1"));
                member.setNominateDim2(parseDoubleOrNull(record, "nominate_dim2"));
                memberRepository.save(member);
                updatedCount++;
            }
        }

        System.out.println("Nominate score sync complete. Members updated: " + updatedCount);
    }

    private Double parseDoubleOrNull(CSVRecord record, String column) {
        try {
            String value = record.get(column);
            return (value == null || value.isBlank()) ? null : Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }
}