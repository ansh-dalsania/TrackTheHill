package com.reptrack.service;

import com.reptrack.Bill;
import com.reptrack.BillCosponsor;
import com.reptrack.BillRepository;
import com.reptrack.MemberRepository;
import com.reptrack.config.WebClientConfig;
import com.reptrack.dto.CongressBillDetailDto;
import com.reptrack.dto.CongressBillDto;
import com.reptrack.dto.CongressBillListResponse;
import com.reptrack.dto.CongressCosponsorDto;
import com.reptrack.dto.CongressCosponsorListResponse;
import com.reptrack.dto.CongressSummaryDto;
import com.reptrack.dto.CongressSummaryListResponse;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.reptrack.dto.CongressBillDetailDto;
import com.reptrack.BillCosponsor;
import com.reptrack.BillCosponsorRepository;
import com.reptrack.dto.CongressCosponsorDto;
import com.reptrack.dto.CongressCosponsorListResponse;
import com.reptrack.dto.CongressSummaryDto;
import com.reptrack.dto.CongressSummaryListResponse;
import java.util.Comparator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Pulls bill data from the Congress.gov API and syncs into database.
 *
 * Too many bills so we only keep bills whose latest action indicates
 * legislative progress —
 * passing a chamber, out of committee, becoming law, etc.
 */
@Service
public class BillSyncService {

    private final WebClient congressApiClient;
    private final WebClientConfig webClientConfig;
    private final BillRepository billRepository;
    private final MemberRepository memberRepository;
    private final BillCosponsorRepository billCosponsorRepository;

    public BillSyncService(WebClient congressApiClient, WebClientConfig webClientConfig,
            BillRepository billRepository, MemberRepository memberRepository,
            BillCosponsorRepository billCosponsorRepository) {
        this.congressApiClient = congressApiClient;
        this.webClientConfig = webClientConfig;
        this.billRepository = billRepository;
        this.memberRepository = memberRepository;
        this.billCosponsorRepository = billCosponsorRepository;
    }

    // Keywords in latestAction.text that indicate a bill has progressed
    private static final List<String> PROGRESS_KEYWORDS = List.of(
            "Passed", "Agreed to", "Reported by", "Placed on", "Ordered to be Reported",
            "Signed by the President", "Became Public Law", "Committee Discharged",
            "Read the second time", "Read the third time", "Cloture", "Vetoed");

    public void syncBills(int congressNumber) {
        int limit = 250;
        int offset = 0;
        int totalCount = Integer.MAX_VALUE;
        int keptCount = 0;

        while (offset < totalCount) {
            final int currentOffset = offset;

            CongressBillListResponse response = congressApiClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/bill/{congress}")
                            .queryParam("limit", limit)
                            .queryParam("offset", currentOffset)
                            .queryParam("api_key", webClientConfig.getApiKey())
                            .build(congressNumber))
                    .retrieve()
                    .bodyToMono(CongressBillListResponse.class)
                    .retry(3)
                    .block();

            if (response == null || response.bills == null || response.bills.isEmpty()) {
                break;
            }

            for (CongressBillDto dto : response.bills) {
                if (hasProgressed(dto)) {
                    billRepository.save(mapToBill(dto));
                    keptCount++;
                }
            }

            if (response.pagination != null && response.pagination.count != null) {
                totalCount = response.pagination.count;
            } else {
                break;
            }

            offset += limit;
        }

        System.out.println("Bill sync complete. Bills kept after filtering: " + keptCount);

        try {
            Thread.sleep(200); // brief pause between pages to avoid overwhelming the API
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Enriches synced bills with detailed data: sponsor, introduced, date,
     * and policy area. Call after syncBills() since it loops through
     * bills already in database.
     */
    public void enrichBills(int congressNumber) {
        List<Bill> bills = billRepository.findByCongress(congressNumber);
        int enrichedCount = 0;

        for (Bill bill : bills) {
            CongressBillDetailDto response = congressApiClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/bill/{congress}/{type}/{number}")
                            .queryParam("api_key", webClientConfig.getApiKey())
                            .build(bill.getCongress(), bill.getBillType(), bill.getBillNumber()))
                    .retrieve()
                    .bodyToMono(CongressBillDetailDto.class)
                    .retry(3)
                    .block();

            if (response == null || response.bill == null) {
                continue;
            }

            if (response.bill.introducedDate != null) {
                bill.setIntroducedDate(LocalDate.parse(response.bill.introducedDate));
            }
            if (response.bill.policyArea != null) {
                bill.setPolicyArea(response.bill.policyArea.name);
            }
            if (response.bill.sponsors != null && !response.bill.sponsors.isEmpty()) {
                String sponsorId = response.bill.sponsors.get(0).bioguideId;
                memberRepository.findById(sponsorId).ifPresent(bill::setSponsor);
            }

            bill.setUpdatedAt(LocalDateTime.now());
            billRepository.save(bill);
            enrichedCount++;

            try {
                Thread.sleep(200); // be a good API citizen, same as bulk sync
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Bill enrichment complete. Bills enriched: " + enrichedCount);
    }

    private boolean hasProgressed(CongressBillDto dto) {
        if (dto.latestAction == null || dto.latestAction.text == null) {
            return false;
        }
        String actionText = dto.latestAction.text;
        return PROGRESS_KEYWORDS.stream().anyMatch(actionText::contains);
    }

    private Bill mapToBill(CongressBillDto dto) {
        String billType = dto.type.toLowerCase();
        String id = dto.congress + "-" + billType + "-" + dto.number;

        Bill bill = billRepository.findById(id).orElse(new Bill());
        bill.setId(id);
        bill.setCongress(dto.congress);
        bill.setBillType(billType);
        bill.setBillNumber(Integer.parseInt(dto.number));
        bill.setTitle(dto.title);
        bill.setOriginChamber(dto.originChamber);
        bill.setLatestActionText(dto.latestAction != null ? dto.latestAction.text : null);
        bill.setLatestActionDate(dto.latestAction != null && dto.latestAction.actionDate != null
                ? LocalDate.parse(dto.latestAction.actionDate)
                : null);
        bill.setUpdateDate(dto.updateDate != null ? LocalDate.parse(dto.updateDate) : null);
        bill.setCongressGovUrl(dto.url);
        bill.setCreatedAt(bill.getCreatedAt() != null ? bill.getCreatedAt() : LocalDateTime.now());
        bill.setUpdatedAt(LocalDateTime.now());

        return bill;
    }

    /**
     * Fetches and stores cosponsor data for all bills in a given congress.
     * Run this after syncBills() and enrichBills(), since it loops through
     * bills already in the database.
     */
    public void syncCosponsors(int congressNumber) {
        List<Bill> bills = billRepository.findByCongress(congressNumber);
        int totalCosponsors = 0;

        for (Bill bill : bills) {
            CongressCosponsorListResponse response = congressApiClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/bill/{congress}/{type}/{number}/cosponsors")
                            .queryParam("api_key", webClientConfig.getApiKey())
                            .build(bill.getCongress(), bill.getBillType(), bill.getBillNumber()))
                    .retrieve()
                    .bodyToMono(CongressCosponsorListResponse.class)
                    .retry(3)
                    .block();

            if (response == null || response.cosponsors == null || response.cosponsors.isEmpty()) {
                sleep();
                continue;
            }

            for (CongressCosponsorDto dto : response.cosponsors) {
                var memberOpt = memberRepository.findById(dto.bioguideId);
                if (memberOpt.isEmpty())
                    continue; // skip cosponsors not in our roster

                BillCosponsor cosponsor = billCosponsorRepository
                        .findByBillIdAndMemberBioguideId(bill.getId(), dto.bioguideId)
                        .orElse(new BillCosponsor());
                cosponsor.setBill(bill);
                cosponsor.setMember(memberOpt.get());
                cosponsor.setSponsorshipDate(dto.sponsorshipDate != null ? LocalDate.parse(dto.sponsorshipDate) : null);
                cosponsor.setIsOriginalCosponsor(dto.isOriginalCosponsor);
                if (cosponsor.getCreatedAt() == null) {
                    cosponsor.setCreatedAt(LocalDateTime.now());
                }
                billCosponsorRepository.save(cosponsor);
                totalCosponsors++;
            }

            sleep();
        }

        System.out.println("Cosponsor sync complete. Total cosponsor records: " + totalCosponsors);
    }

    private void sleep() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Fetches official Congressional Research Service summaries for all
     * bills in a given congress, storing the most recent version (by actionDate)
     * in bill.summary.
     */
    public void syncSummaries(int congressNumber) {
        List<Bill> bills = billRepository.findByCongress(congressNumber);
        int updatedCount = 0;

        for (Bill bill : bills) {
            try {
                CongressSummaryListResponse response = congressApiClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/bill/{congress}/{type}/{number}/summaries")
                                .queryParam("api_key", webClientConfig.getApiKey())
                                .build(bill.getCongress(), bill.getBillType(), bill.getBillNumber()))
                        .retrieve()
                        .bodyToMono(CongressSummaryListResponse.class)
                        .retry(3)
                        .block();

                if (response == null || response.summaries == null || response.summaries.isEmpty()) {
                    sleep();
                    continue;
                }

                CongressSummaryDto mostRecent = response.summaries.stream()
                        .filter(s -> s.actionDate != null)
                        .max(Comparator.comparing(s -> s.actionDate))
                        .orElse(response.summaries.get(response.summaries.size() - 1));

                bill.setSummary(mostRecent.text);
                bill.setUpdatedAt(LocalDateTime.now());
                billRepository.save(bill);
                updatedCount++;
            } catch (Exception e) {
                System.out.println("Skipping bill " + bill.getId() + " due to error: " + e.getMessage());
            }

            sleep();
        }

        System.out.println("Summary sync complete. Bills updated: " + updatedCount);
    }
}