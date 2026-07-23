package com.trackthehill.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Runs all sync services on a recurring schedule. Frequencies reflect how 
 * often each data source actually changes. Times are staggered to avoid 
 * overlapping heavy syncs and to run during off-peak hours. CURRENT_CONGRESS 
 * is hardcoded and must be updated when a new Congress begins (~every 2 years).
 */
@Service
public class ScheduledSyncService {

    private static final int CURRENT_CONGRESS = 119;

    private final MemberSyncService memberSyncService;
    private final BillSyncService billSyncService;
    private final HouseVoteSyncService houseVoteSyncService;
    private final SenateVoteSyncService senateVoteSyncService;
    private final CampaignFinanceSyncService campaignFinanceSyncService;
    private final TopDonorSyncService topDonorSyncService;
    private final CommitteeSyncService committeeSyncService;
    private final FecCrosswalkSyncService fecCrosswalkSyncService;
    private final NominateScoreSyncService nominateScoreSyncService;

    public ScheduledSyncService(MemberSyncService memberSyncService,
                                 BillSyncService billSyncService,
                                 HouseVoteSyncService houseVoteSyncService,
                                 SenateVoteSyncService senateVoteSyncService,
                                 CampaignFinanceSyncService campaignFinanceSyncService,
                                 TopDonorSyncService topDonorSyncService,
                                 CommitteeSyncService committeeSyncService,
                                 FecCrosswalkSyncService fecCrosswalkSyncService,
                                 NominateScoreSyncService nominateScoreSyncService) {
        this.memberSyncService = memberSyncService;
        this.billSyncService = billSyncService;
        this.houseVoteSyncService = houseVoteSyncService;
        this.senateVoteSyncService = senateVoteSyncService;
        this.campaignFinanceSyncService = campaignFinanceSyncService;
        this.topDonorSyncService = topDonorSyncService;
        this.committeeSyncService = committeeSyncService;
        this.fecCrosswalkSyncService = fecCrosswalkSyncService;
        this.nominateScoreSyncService = nominateScoreSyncService;
    }

    // Daily at 2:00 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void dailyMemberSync() {
        log("Starting daily member sync");
        memberSyncService.syncMembers(CURRENT_CONGRESS);
        log("Finished daily member sync");
    }

    // Daily at 2:30 AM
    @Scheduled(cron = "0 30 2 * * *")
    public void dailyBillSync() {
        log("Starting daily bill sync");
        billSyncService.syncBills(CURRENT_CONGRESS);
        billSyncService.enrichBills(CURRENT_CONGRESS);
        billSyncService.syncSummaries(CURRENT_CONGRESS);
        billSyncService.syncCosponsors(CURRENT_CONGRESS);
        log("Finished daily bill sync");
    }

    // Daily at 3:30 AM
    @Scheduled(cron = "0 30 3 * * *")
    public void dailyHouseVoteSync() {
        log("Starting daily House vote sync");
        houseVoteSyncService.syncVotes(CURRENT_CONGRESS);
        log("Finished daily House vote sync");
    }

    // Weekly, Sunday 1:00 AM
    @Scheduled(cron = "0 0 1 * * SUN")
    public void weeklySenateVoteSync() throws Exception {
        log("Starting weekly Senate vote sync");
        senateVoteSyncService.syncSenateVotes(CURRENT_CONGRESS);
        log("Finished weekly Senate vote sync");
    }

    // Weekly, Sunday 4:00 AM
    @Scheduled(cron = "0 0 4 * * SUN")
    public void weeklyFinanceSync() {
        log("Starting weekly campaign finance sync");
        campaignFinanceSyncService.syncFinanceSummaries(2026);
        topDonorSyncService.syncTopDonors(2026);
        log("Finished weekly campaign finance sync");
    }

    // Weekly, Sunday 5:00 AM
    @Scheduled(cron = "0 0 5 * * SUN")
    public void weeklyCommitteeAndNominateSync() throws Exception {
        log("Starting weekly committee and nominate score sync");
        committeeSyncService.syncCommittees();
        nominateScoreSyncService.syncNominateScores(CURRENT_CONGRESS);
        log("Finished weekly committee and nominate score sync");
    }

    // Monthly, 1st of the month, 5:30 AM
    @Scheduled(cron = "0 30 5 1 * *")
    public void monthlyFecCrosswalkSync() throws Exception {
        log("Starting monthly FEC crosswalk sync");
        fecCrosswalkSyncService.syncCrosswalk();
        log("Finished monthly FEC crosswalk sync");
    }

    private void log(String message) {
        System.out.println("[SCHEDULED SYNC] " + message);
    }
}