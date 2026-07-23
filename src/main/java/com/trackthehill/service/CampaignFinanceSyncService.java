package com.trackthehill.service;

import com.trackthehill.*;
import com.trackthehill.config.WebClientConfig;
import com.trackthehill.dto.FecTotalsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Pulls aggregated campaign finance totals from FEC's /candidate/{id}/totals/
 * endpoint for each member with a known FEC candidate ID.
 */
@Service
public class CampaignFinanceSyncService {

    private final WebClient fecApiClient;
    private final WebClientConfig webClientConfig;
    private final MemberRepository memberRepository;
    private final CampaignFinanceSummaryRepository summaryRepository;

    public CampaignFinanceSyncService(@Qualifier("fecApiClient") WebClient fecApiClient,
            WebClientConfig webClientConfig,
            MemberRepository memberRepository,
            CampaignFinanceSummaryRepository summaryRepository) {
        this.fecApiClient = fecApiClient;
        this.webClientConfig = webClientConfig;
        this.memberRepository = memberRepository;
        this.summaryRepository = summaryRepository;
    }

    public void syncFinanceSummaries(int cycle) {
        List<Member> members = memberRepository.findAll();
        int updatedCount = 0;

        for (Member member : members) {
            if (member.getFecCandidateId() == null)
                continue;

            FecTotalsResponse response = fecApiClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/candidate/{candidateId}/totals/")
                            .queryParam("api_key", webClientConfig.getFecApiKey())
                            .queryParam("cycle", cycle)
                            .build(member.getFecCandidateId()))
                    .retrieve()
                    .bodyToMono(FecTotalsResponse.class)
                    .retryWhen(reactor.util.retry.Retry.backoff(3, java.time.Duration.ofSeconds(5)))
                    .block();

            if (response == null || response.results == null || response.results.isEmpty()) {
                sleep();
                continue;
            }

            var dto = response.results.get(0);
            CampaignFinanceSummary summary = summaryRepository
                    .findByMemberBioguideIdAndCycle(member.getBioguideId(), cycle)
                    .orElse(new CampaignFinanceSummary());
            summary.setMember(member);
            summary.setCycle(cycle);
            summary.setIndividualContributions(dto.individual_contributions);
            summary.setPacContributions(dto.other_political_committee_contributions);
            summary.setPartyContributions(dto.political_party_committee_contributions);
            summary.setTotalReceipts(dto.receipts);
            summary.setTotalDisbursements(dto.disbursements);
            summary.setCashOnHand(dto.last_cash_on_hand_end_period);
            if (dto.coverage_end_date != null) {
                summary.setCoverageEndDate(LocalDate.parse(dto.coverage_end_date.substring(0, 10)));
            }
            summary.setCreatedAt(summary.getCreatedAt() != null ? summary.getCreatedAt() : LocalDateTime.now());
            summary.setUpdatedAt(LocalDateTime.now());
            summaryRepository.save(summary);
            updatedCount++;

            sleep();
        }

        System.out.println("Campaign finance summary sync complete. Members updated: " + updatedCount);
    }

    private void sleep() {
        try {
            Thread.sleep(1200); // ~50 requests/minute, safely under FEC's rate limit
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}