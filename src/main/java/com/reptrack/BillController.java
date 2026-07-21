package com.reptrack;

import com.reptrack.dto.BillSummaryResponse;
import com.reptrack.service.BillSyncService;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.springframework.data.domain.Page;

@RestController
public class BillController {

    private final BillRepository billRepository;
    private final BillSyncService billSyncService;

    public BillController(BillRepository billRepository, BillSyncService billSyncService) {
        this.billRepository = billRepository;
        this.billSyncService = billSyncService;
    }

    @GetMapping("/api/bills")
    public Page<BillSummaryResponse> getAllBills(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return billRepository.findAll(PageRequest.of(page, size))
                .map(bill -> new BillSummaryResponse(
                        bill.getId(),
                        bill.getTitle(),
                        bill.getBillType(),
                        bill.getBillNumber(),
                        bill.getOriginChamber(),
                        bill.getLatestActionText(),
                        bill.getLatestActionDate(),
                        bill.getPolicyArea(),
                        bill.getSponsor() != null ? bill.getSponsor().getBioguideId() : null,
                        bill.getSponsor() != null
                                ? bill.getSponsor().getFirstName() + " " + bill.getSponsor().getLastName()
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
}