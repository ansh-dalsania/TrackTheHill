package com.reptrack;

import com.reptrack.service.SenateVoteSyncService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SenateVoteController {

    private final SenateVoteSyncService senateVoteSyncService;

    public SenateVoteController(SenateVoteSyncService senateVoteSyncService) {
        this.senateVoteSyncService = senateVoteSyncService;
    }

    // TEMPORARY: manually triggers a Senate vote sync from Voteview.
    @GetMapping("/api/sync/votes/senate/{congress}")
    public String triggerSync(@PathVariable int congress) throws Exception {
        senateVoteSyncService.syncSenateVotes(congress);
        return "Senate vote sync triggered for Congress " + congress;
    }
}