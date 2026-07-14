package com.reptrack.service;

import com.reptrack.Bill;
import com.reptrack.BillRepository;
import com.reptrack.config.WebClientConfig;
import com.reptrack.dto.CongressBillDto;
import com.reptrack.dto.CongressBillListResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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

    public BillSyncService(WebClient congressApiClient,
            WebClientConfig webClientConfig,
            BillRepository billRepository) {
        this.congressApiClient = congressApiClient;
        this.webClientConfig = webClientConfig;
        this.billRepository = billRepository;
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
}