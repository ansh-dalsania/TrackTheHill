package com.trackthehill.service;

import com.trackthehill.*;
import com.trackthehill.config.WebClientConfig;
import com.trackthehill.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Pulls House roll call vote data from the Congress.gov API and syncs it
 * into the vote and member_vote tables.
 *
 * Fetches list of roll call votes for a Congress first, second call fetches
 * every
 * member's individual vote.
 */
@Service
public class HouseVoteSyncService {

    private final WebClient congressApiClient;
    private final WebClientConfig webClientConfig;
    private final VoteRepository voteRepository;
    private final MemberVoteRepository memberVoteRepository;
    private final MemberRepository memberRepository;
    private final BillRepository billRepository;

    public HouseVoteSyncService(WebClient congressApiClient,
            WebClientConfig webClientConfig,
            VoteRepository voteRepository,
            MemberVoteRepository memberVoteRepository,
            MemberRepository memberRepository,
            BillRepository billRepository) {
        this.congressApiClient = congressApiClient;
        this.webClientConfig = webClientConfig;
        this.voteRepository = voteRepository;
        this.memberVoteRepository = memberVoteRepository;
        this.memberRepository = memberRepository;
        this.billRepository = billRepository;
    }

    public void syncVotes(int congressNumber) {
        int limit = 20; // endpoint's default/max page size, per sample response
        int offset = 0;
        int totalCount = Integer.MAX_VALUE;
        int syncedCount = 0;

        while (offset < totalCount) {
            final int currentOffset = offset;

            HouseVoteListResponse response = congressApiClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/house-vote/{congress}")
                            .queryParam("limit", limit)
                            .queryParam("offset", currentOffset)
                            .queryParam("api_key", webClientConfig.getApiKey())
                            .build(congressNumber))
                    .retrieve()
                    .bodyToMono(HouseVoteListResponse.class)
                    .retry(3)
                    .block();

            if (response == null || response.houseRollCallVotes == null || response.houseRollCallVotes.isEmpty()) {
                break;
            }

            for (HouseVoteDto dto : response.houseRollCallVotes) {
                syncSingleVote(dto);
                syncedCount++;
                sleep();
            }

            if (response.pagination != null && response.pagination.count != null) {
                totalCount = response.pagination.count;
            } else {
                break;
            }

            offset += limit;
        }

        System.out.println("House vote sync complete. Votes synced: " + syncedCount);
    }

    private void syncSingleVote(HouseVoteDto dto) {
        String voteId = dto.congress + "-" + dto.sessionNumber + "-" + dto.rollCallNumber;

        Vote vote = voteRepository.findById(voteId).orElse(new Vote());
        vote.setId(voteId);
        vote.setCongress(dto.congress);
        vote.setSession(dto.sessionNumber);
        vote.setRollCallNumber(dto.rollCallNumber);
        vote.setResult(dto.result);
        if (dto.startDate != null) {
            vote.setVoteDate(OffsetDateTime.parse(dto.startDate).toLocalDateTime());
        }

        // Link to a bill if applicable
        if (dto.legislationType != null && dto.legislationNumber != null) {
            String billId = dto.congress + "-" + dto.legislationType.toLowerCase() + "-" + dto.legislationNumber;
            billRepository.findById(billId).ifPresent(vote::setBill);
        }

        vote.setCreatedAt(vote.getCreatedAt() != null ? vote.getCreatedAt() : LocalDateTime.now());
        vote.setUpdatedAt(LocalDateTime.now());
        voteRepository.save(vote);

        // Get every member's position on this vote
        HouseVoteDetailDto detail = congressApiClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/house-vote/{congress}/{session}/{rollCall}/members")
                        .queryParam("api_key", webClientConfig.getApiKey())
                        .build(dto.congress, dto.sessionNumber, dto.rollCallNumber))
                .retrieve()
                .bodyToMono(HouseVoteDetailDto.class)
                .retry(3)
                .block();

        if (detail == null || detail.houseRollCallVoteMemberVotes == null
                || detail.houseRollCallVoteMemberVotes.results == null) {
            return;
        }

        // Update vote_question/vote_type
        vote.setVoteQuestion(detail.houseRollCallVoteMemberVotes.voteQuestion);
        vote.setVoteType(detail.houseRollCallVoteMemberVotes.voteType);
        voteRepository.save(vote);

        for (HouseVoteDetailDto.MemberVoteEntry entry : detail.houseRollCallVoteMemberVotes.results) {
            Optional<Member> memberOpt = memberRepository.findById(entry.bioguideID);
            if (memberOpt.isEmpty()) {
                continue; // skip votes from members not in our table (e.g. former members)
            }

            MemberVote memberVote = memberVoteRepository
                    .findByVoteIdAndMemberBioguideId(voteId, entry.bioguideID)
                    .orElse(new MemberVote());
            memberVote.setVote(vote);
            memberVote.setMember(memberOpt.get());
            memberVote.setPosition(entry.voteCast);
            memberVote.setPartyAtVote(entry.voteParty);
            if (memberVote.getCreatedAt() == null) {
                memberVote.setCreatedAt(LocalDateTime.now());
            }
            memberVoteRepository.save(memberVote);
        }
    }

    private void sleep() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}