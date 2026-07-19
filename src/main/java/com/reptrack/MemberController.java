package com.reptrack;

import com.reptrack.service.MemberSyncService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import com.reptrack.dto.AttendanceResponse;
import com.reptrack.dto.MemberVoteResponse;
import com.reptrack.dto.PartisanScoreResponse;
import com.reptrack.service.CampaignFinanceSyncService;
import com.reptrack.service.FecCrosswalkSyncService;
import com.reptrack.service.HouseVoteSyncService; // if not already present
import java.util.stream.Collectors;
import com.reptrack.dto.AttendanceResponse;
import org.springframework.web.bind.annotation.RequestParam;
import com.reptrack.dto.PartisanScoreResponse;
import com.reptrack.service.VoteAnalyticsService;
import java.util.Map;
import com.reptrack.service.NominateScoreSyncService;

/**
 * Exposes Member data over HTTP.
 */
@RestController
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberSyncService memberSyncService;
    private final MemberVoteRepository memberVoteRepository;
    private final VoteAnalyticsService voteAnalyticsService;
    private final NominateScoreSyncService nominateScoreSyncService;
    private final BillRepository billRepository;
    private final BillCosponsorRepository billCosponsorRepository;
    private final FecCrosswalkSyncService fecCrosswalkSyncService;
    private final CampaignFinanceSyncService campaignFinanceSyncService;

    public MemberController(MemberRepository memberRepository, MemberSyncService memberSyncService,
            MemberVoteRepository memberVoteRepository, VoteAnalyticsService voteAnalyticsService,
            NominateScoreSyncService nominateScoreSyncService, BillRepository billRepository,
            BillCosponsorRepository billCosponsorRepository, FecCrosswalkSyncService fecCrosswalkSyncService,
            CampaignFinanceSyncService campaignFinanceSyncService) {
        this.memberRepository = memberRepository;
        this.memberSyncService = memberSyncService;
        this.memberVoteRepository = memberVoteRepository;
        this.voteAnalyticsService = voteAnalyticsService;
        this.nominateScoreSyncService = nominateScoreSyncService;
        this.billRepository = billRepository;
        this.billCosponsorRepository = billCosponsorRepository;
        this.fecCrosswalkSyncService = fecCrosswalkSyncService;
        this.campaignFinanceSyncService = campaignFinanceSyncService;
    }

    // Returns all members currently in the database
    @GetMapping("/api/members")
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    // Returns a single member by their bioguide ID
    @GetMapping("/api/members/{id}")
    public Member getMember(@PathVariable String id) {
        return memberRepository.findById(id).orElse(null);
    }

    // TEMPORARY: manually triggers a sync from Congress.gov.
    // Will be replaced by a scheduled job later.
    @GetMapping("/api/sync/members/{congress}")
    public String triggerSync(@PathVariable int congress) {
        memberSyncService.syncMembers(congress);
        return "Sync triggered for Congress " + congress;
    }

    @GetMapping("/api/members/{id}/votes")
    public List<MemberVoteResponse> getMemberVotingHistory(@PathVariable String id) {
        Map<String, Boolean> alignment = voteAnalyticsService.getPartyAlignmentByVote(id);

        return memberVoteRepository.findVotingHistoryForMember(id).stream()
                .map(mv -> new MemberVoteResponse(
                        mv.getVote().getId(),
                        mv.getVote().getChamber(),
                        mv.getVote().getVoteDate(),
                        mv.getVote().getVoteQuestion(),
                        mv.getVote().getResult(),
                        mv.getPosition(),
                        mv.getVote().getBill() != null ? mv.getVote().getBill().getId() : null,
                        mv.getVote().getBill() != null ? mv.getVote().getBill().getTitle() : null,
                        alignment.get(mv.getVote().getId())))
                .collect(Collectors.toList());
    }

    @GetMapping("/api/members/{id}/attendance")
    public AttendanceResponse getAttendance(@PathVariable String id,
            @RequestParam(defaultValue = "119") int congress) {
        long total = memberVoteRepository.countTotalVotesForMemberInCongress(id, congress);
        long missed = memberVoteRepository.countMissedVotesForMemberInCongress(id, congress);
        double percentage = total == 0 ? 0.0 : ((total - missed) / (double) total) * 100;

        LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);
        long recentTotal = memberVoteRepository.countTotalVotesForMemberSince(id, oneMonthAgo);
        long recentMissed = memberVoteRepository.countMissedVotesForMemberSince(id, oneMonthAgo);
        double recentPercentage = recentTotal == 0 ? 0.0 : ((recentTotal - recentMissed) / (double) recentTotal) * 100;

        return new AttendanceResponse(
                congress, total, missed, Math.round(percentage * 100.0) / 100.0,
                recentTotal, recentMissed, Math.round(recentPercentage * 100.0) / 100.0);
    }

    @GetMapping("/api/members/{id}/partisan-score")
    public PartisanScoreResponse getPartisanScore(@PathVariable String id,
            @RequestParam(defaultValue = "119") int congress) {
        return voteAnalyticsService.calculatePartisanScore(id, congress);
    }

    @GetMapping("/api/sync/members/{congress}/nominate")
    public String triggerNominateSync(@PathVariable int congress) throws Exception {
        nominateScoreSyncService.syncNominateScores(congress);
        return "Nominate score sync triggered for Congress " + congress;
    }

    @GetMapping("/api/members/{id}/bills-sponsored")
    public List<Bill> getBillsSponsored(@PathVariable String id) {
        return billRepository.findBySponsorBioguideId(id);
    }

    @GetMapping("/api/members/{id}/bills-cosponsored")
    public List<BillCosponsor> getBillsCosponsored(@PathVariable String id) {
        return billCosponsorRepository.findByMemberBioguideId(id);
    }

    @GetMapping("/api/sync/members/fec-crosswalk")
    public String triggerFecCrosswalkSync() throws Exception {
        fecCrosswalkSyncService.syncCrosswalk();
        return "FEC crosswalk sync triggered";
    }

    @GetMapping("/api/sync/finance/{cycle}")
    public String triggerFinanceSync(@PathVariable int cycle) {
        campaignFinanceSyncService.syncFinanceSummaries(cycle);
        return "Campaign finance sync triggered for cycle " + cycle;
    }
}