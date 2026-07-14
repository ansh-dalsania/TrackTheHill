package com.reptrack;

import com.reptrack.service.BillSyncService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class BillController {

    private final BillRepository billRepository;
    private final BillSyncService billSyncService;

    public BillController(BillRepository billRepository, BillSyncService billSyncService) {
        this.billRepository = billRepository;
        this.billSyncService = billSyncService;
    }

    @GetMapping("/api/bills")
    public List<Bill> getAllBills() {
        return billRepository.findAll();
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
}