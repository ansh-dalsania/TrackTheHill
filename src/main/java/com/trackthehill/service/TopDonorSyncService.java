package com.trackthehill.service;

import com.trackthehill.*;
import com.trackthehill.config.WebClientConfig;
import com.trackthehill.dto.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * For each member with a known FEC candidate ID: finds their current-cycle
 * principal campaign committee, then pulls their top 20 individual itemized
 * contributions by amount from FEC's schedule_a endpoint.
 */
@Service
public class TopDonorSyncService {

    private final WebClient fecApiClient;
    private final WebClientConfig webClientConfig;
    private final MemberRepository memberRepository;
    private final TopDonorRepository topDonorRepository;
    private final TopDonorPersistenceService topDonorPersistenceService;

    public TopDonorSyncService(@Qualifier("fecApiClient") WebClient fecApiClient,
            WebClientConfig webClientConfig, MemberRepository memberRepository,
            TopDonorRepository topDonorRepository, TopDonorPersistenceService topDonorPersistenceService) {
        this.fecApiClient = fecApiClient;
        this.webClientConfig = webClientConfig;
        this.memberRepository = memberRepository;
        this.topDonorRepository = topDonorRepository;
        this.topDonorPersistenceService = topDonorPersistenceService;
    }

    public void syncTopDonors(int cycle) {
        List<Member> members = memberRepository.findAll();
        int membersProcessed = 0;

        for (Member member : members) {
            if (member.getFecCandidateId() == null)
                continue;

            try {
                String committeeId = findPrincipalCommittee(member.getFecCandidateId(), cycle);
                sleep();

                if (committeeId == null)
                    continue;

                member.setFecCommitteeId(committeeId);
                memberRepository.save(member);

                List<FecScheduleADto> topContributions = fetchTopContributions(committeeId, cycle);
                sleep();

                topDonorPersistenceService.replaceTopDonors(member, cycle, topContributions);

                membersProcessed++;
            } catch (Exception e) {
                System.out.println("Skipping member " + member.getBioguideId() + " due to error: " + e.getMessage());
            }
        }

        System.out.println("Top donor sync complete. Members processed: " + membersProcessed);
    }

    private String findPrincipalCommittee(String candidateId, int cycle) {
        FecCommitteeListResponse response = fecApiClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/candidate/{candidateId}/committees/")
                        .queryParam("api_key", webClientConfig.getFecApiKey())
                        .queryParam("designation", "P")
                        .build(candidateId))
                .retrieve()
                .bodyToMono(FecCommitteeListResponse.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(5)))
                .block();

        if (response == null || response.results == null)
            return null;

        return response.results.stream()
                .filter(c -> c.cycles != null && c.cycles.contains(cycle))
                .map(c -> c.committee_id)
                .findFirst()
                .orElse(null);
    }

    private List<FecScheduleADto> fetchTopContributions(String committeeId, int cycle) {
        FecScheduleAResponse response = fecApiClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/schedules/schedule_a/")
                        .queryParam("api_key", webClientConfig.getFecApiKey())
                        .queryParam("committee_id", committeeId)
                        .queryParam("two_year_transaction_period", cycle)
                        .queryParam("is_individual", true)
                        .queryParam("sort", "-contribution_receipt_amount")
                        .queryParam("per_page", 20)
                        .build())
                .retrieve()
                .bodyToMono(FecScheduleAResponse.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(5)))
                .block();

        return (response != null && response.results != null) ? response.results : List.of();
    }

    private void sleep() {
        try {
            Thread.sleep(2500); // slower pace specifically for schedule_a's stricter limit
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}