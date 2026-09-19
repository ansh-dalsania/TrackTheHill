package com.trackthehill;

import com.trackthehill.dto.BillDetailResponse;
import com.trackthehill.dto.BillSummaryResponse;
import com.trackthehill.service.BillSyncService;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Page;

@RestController
public class BillController {

    private final BillRepository billRepository;
    private final BillSyncService billSyncService;
    private final BillCosponsorRepository billCosponsorRepository;
    private final VoteRepository voteRepository;

    public BillController(BillRepository billRepository, BillSyncService billSyncService,
            BillCosponsorRepository billCosponsorRepository, VoteRepository voteRepository) {
        this.billRepository = billRepository;
        this.billSyncService = billSyncService;
        this.billCosponsorRepository = billCosponsorRepository;
        this.voteRepository = voteRepository;
    }

    @GetMapping("/api/bills")
    public Page<BillSummaryResponse> getAllBills(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String policyArea,
            @RequestParam(required = false) String query) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Bill> bills;

        if (query != null && !query.isBlank()) {
            bills = (policyArea != null)
                    ? billRepository.searchWithVotesByPolicyArea(query, policyArea, pageable)
                    : billRepository.searchWithVotes(query, pageable);
        } else {
            bills = (policyArea != null)
                    ? billRepository.findWithVotesByPolicyArea(policyArea, pageable)
                    : billRepository.findWithVotes(pageable);
        }

        return bills.map(bill -> new BillSummaryResponse(
                bill.getId(), bill.getTitle(), bill.getBillType(), bill.getBillNumber(),
                bill.getOriginChamber(), bill.getLatestActionText(), bill.getLatestActionDate(),
                bill.getPolicyArea(),
                bill.getSponsor() != null ? bill.getSponsor().getBioguideId() : null,
                bill.getSponsor() != null ? bill.getSponsor().getFirstName() + " " + bill.getSponsor().getLastName()
                        : null));
    }

    // TEMPORARY: manually triggers a bill sync from Congress.gov.
    @GetMapping("/api/sync/bills/{congress}")
    public String triggerSync(@PathVariable int congress) {
        billSyncService.syncBills(congress);
        return "Bill sync triggered for Congress " + congress;
    }

    @GetMapping("/api/sync/bills/{congress}/enrich")
    public String triggerEnrichment(@PathVariable int congress) {
        billSyncService.enrichBills(congress);
        return "Bill enrichment triggered for Congress " + congress;
    }

    @GetMapping("/api/sync/bills/{congress}/cosponsors")
    public String triggerCosponsorSync(@PathVariable int congress) {
        billSyncService.syncCosponsors(congress);
        return "Cosponsor sync triggered for Congress " + congress;
    }

    @GetMapping("/api/sync/bills/{congress}/summaries")
    public String triggerSummarySync(@PathVariable int congress) {
        billSyncService.syncSummaries(congress);
        return "Summary sync triggered for Congress " + congress;
    }

    @GetMapping("/api/policy-areas")
    public List<String> getPolicyAreas() {
        return billRepository.findDistinctPolicyAreas();
    }

    @GetMapping("/api/bills/{id}")
    public BillDetailResponse getBillDetail(@PathVariable String id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found: " + id));

        List<BillDetailResponse.CosponsorEntry> cosponsors = billCosponsorRepository.findByBillId(id).stream()
                .map(bc -> new BillDetailResponse.CosponsorEntry(
                        bc.getMember().getBioguideId(),
                        bc.getMember().getFirstName() + " " + bc.getMember().getLastName(),
                        bc.getSponsorshipDate(),
                        bc.getIsOriginalCosponsor()))
                .collect(Collectors.toList());

        return new BillDetailResponse(
                bill.getId(),
                bill.getTitle(),
                bill.getBillType(),
                bill.getBillNumber(),
                bill.getOriginChamber(),
                bill.getLatestActionText(),
                bill.getLatestActionDate(),
                bill.getIntroducedDate(),
                bill.getPolicyArea(),
                bill.getSummary(),
                bill.getCongressGovUrl(),
                bill.getSponsor() != null ? bill.getSponsor().getBioguideId() : null,
                bill.getSponsor() != null ? bill.getSponsor().getFirstName() + " " + bill.getSponsor().getLastName()
                        : null,
                cosponsors);
    }

    @GetMapping("/api/bills/{id}/votes")
    public List<Vote> getVotesForBill(@PathVariable String id) {
        return voteRepository.findByBillIdOrderByVoteDateDesc(id);
    }
}