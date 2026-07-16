package com.reptrack;

import com.reptrack.service.HouseVoteSyncService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HouseVoteController {

    private final HouseVoteSyncService houseVoteSyncService;

    public HouseVoteController(HouseVoteSyncService houseVoteSyncService) {
        this.houseVoteSyncService = houseVoteSyncService;
    }

    // TEMPORARY: manually triggers a House vote sync from Congress.gov.
    @GetMapping("/api/sync/votes/house/{congress}")
    public String triggerSync(@PathVariable int congress) {
        houseVoteSyncService.syncVotes(congress);
        return "House vote sync triggered for Congress " + congress;
    }
}