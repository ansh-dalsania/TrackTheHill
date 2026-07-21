package com.reptrack;

import com.reptrack.service.MemberSyncService;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

import com.reptrack.dto.AttendanceResponse;
import com.reptrack.dto.BillCosponsorResponse;
import com.reptrack.dto.BillSummaryResponse;
import com.reptrack.dto.MemberVoteResponse;
import com.reptrack.dto.PartisanScoreResponse;
import com.reptrack.service.CampaignFinanceSyncService;
import com.reptrack.service.CommitteeSyncService;
import com.reptrack.service.FecCrosswalkSyncService;
import com.reptrack.service.HouseVoteSyncService;
import java.util.stream.Collectors;
import com.reptrack.service.VoteAnalyticsService;
import java.util.Map;
import com.reptrack.service.NominateScoreSyncService;
import com.reptrack.service.TopDonorSyncService;
import org.springframework.data.domain.Page;

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
    private final TopDonorSyncService topDonorSyncService;
    private final TopDonorRepository topDonorRepository;
    private final CommitteeSyncService committeeSyncService;
    private final CommitteeMembershipRepository committeeMembershipRepository;
    private final CampaignFinanceSummaryRepository campaignFinanceSummaryRepository;

    public MemberController(MemberRepository memberRepository, MemberSyncService memberSyncService,
            MemberVoteRepository memberVoteRepository, VoteAnalyticsService voteAnalyticsService,
            NominateScoreSyncService nominateScoreSyncService, BillRepository billRepository,
            BillCosponsorRepository billCosponsorRepository, FecCrosswalkSyncService fecCrosswalkSyncService,
            CampaignFinanceSyncService campaignFinanceSyncService, TopDonorSyncService topDonorSyncService,
            TopDonorRepository topDonorRepository, CommitteeSyncService committeeSyncService,
            CommitteeMembershipRepository committeeMembershipRepository, 
            CampaignFinanceSummaryRepository campaignFinanceSummaryRepository) {
        this.memberRepository = memberRepository;
        this.memberSyncService = memberSyncService;
        this.memberVoteRepository = memberVoteRepository;
        this.voteAnalyticsService = voteAnalyticsService;
        this.nominateScoreSyncService = nominateScoreSyncService;
        this.billRepository = billRepository;
        this.billCosponsorRepository = billCosponsorRepository;
        this.fecCrosswalkSyncService = fecCrosswalkSyncService;
        this.campaignFinanceSyncService = campaignFinanceSyncService;
        this.topDonorSyncService = topDonorSyncService;
        this.topDonorRepository = topDonorRepository;
        this.committeeSyncService = committeeSyncService;
        this.committeeMembershipRepository = committeeMembershipRepository;
        this.campaignFinanceSummaryRepository = campaignFinanceSummaryRepository;
    }

    // Returns all members currently in the database
    @GetMapping("/api/members")
    public Page<Member> getAllMembers(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return memberRepository.findAll(PageRequest.of(page, size));
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
    public List<BillSummaryResponse> getBillsSponsored(@PathVariable String id) {
        return billRepository.findBySponsorBioguideId(id).stream()
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
                                : null))
                .collect(Collectors.toList());
    }

    @GetMapping("/api/members/{id}/bills-cosponsored")
    public List<BillCosponsorResponse> getBillsCosponsored(@PathVariable String id) {
        return billCosponsorRepository.findByMemberBioguideId(id).stream()
                .map(bc -> new BillCosponsorResponse(
                        new BillSummaryResponse(
                                bc.getBill().getId(),
                                bc.getBill().getTitle(),
                                bc.getBill().getBillType(),
                                bc.getBill().getBillNumber(),
                                bc.getBill().getOriginChamber(),
                                bc.getBill().getLatestActionText(),
                                bc.getBill().getLatestActionDate(),
                                bc.getBill().getPolicyArea(),
                                bc.getBill().getSponsor() != null ? bc.getBill().getSponsor().getBioguideId() : null,
                                bc.getBill().getSponsor() != null ? bc.getBill().getSponsor().getFirstName() + " "
                                        + bc.getBill().getSponsor().getLastName() : null),
                        bc.getSponsorshipDate(),
                        bc.getIsOriginalCosponsor()))
                .collect(Collectors.toList());
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

    @GetMapping("/api/sync/top-donors/{cycle}")
    public String triggerTopDonorSync(@PathVariable int cycle) {
        topDonorSyncService.syncTopDonors(cycle);
        return "Top donor sync triggered for cycle " + cycle;
    }

    @GetMapping("/api/members/{id}/top-donors")
    public List<TopDonor> getTopDonors(@PathVariable String id, @RequestParam(defaultValue = "2026") int cycle) {
        return topDonorRepository.findByMemberBioguideIdAndCycleOrderByContributionAmountDesc(id, cycle);
    }

    @GetMapping("/api/members/{id}/votes-by-issue")
    public List<MemberVoteResponse> getVotesByPolicyArea(@PathVariable String id, @RequestParam String policyArea) {
        Map<String, Boolean> alignment = voteAnalyticsService.getPartyAlignmentByVote(id);

        return memberVoteRepository.findVotingHistoryForMemberByPolicyArea(id, policyArea).stream()
                .map(mv -> new MemberVoteResponse(
                        mv.getVote().getId(),
                        mv.getVote().getChamber(),
                        mv.getVote().getVoteDate(),
                        mv.getVote().getVoteQuestion(),
                        mv.getVote().getResult(),
                        mv.getPosition(),
                        mv.getVote().getBill().getId(),
                        mv.getVote().getBill().getTitle(),
                        alignment.get(mv.getVote().getId())))
                .collect(Collectors.toList());
    }

    @GetMapping("/api/sync/committees")
    public String triggerCommitteeSync() throws Exception {
        committeeSyncService.syncCommittees();
        return "Committee sync triggered";
    }

    @GetMapping("/api/members/{id}/committees")
    public List<CommitteeMembership> getMemberCommittees(@PathVariable String id) {
        return committeeMembershipRepository.findByMemberBioguideId(id);
    }

    @GetMapping("/api/states")
    public List<Map<String, String>> getStates() {
        Map<String, String> stateNames = new LinkedHashMap<>();
        stateNames.put("AL", "Alabama");
        stateNames.put("AK", "Alaska");
        stateNames.put("AZ", "Arizona");
        stateNames.put("AR", "Arkansas");
        stateNames.put("CA", "California");
        stateNames.put("CO", "Colorado");
        stateNames.put("CT", "Connecticut");
        stateNames.put("DE", "Delaware");
        stateNames.put("FL", "Florida");
        stateNames.put("GA", "Georgia");
        stateNames.put("HI", "Hawaii");
        stateNames.put("ID", "Idaho");
        stateNames.put("IL", "Illinois");
        stateNames.put("IN", "Indiana");
        stateNames.put("IA", "Iowa");
        stateNames.put("KS", "Kansas");
        stateNames.put("KY", "Kentucky");
        stateNames.put("LA", "Louisiana");
        stateNames.put("ME", "Maine");
        stateNames.put("MD", "Maryland");
        stateNames.put("MA", "Massachusetts");
        stateNames.put("MI", "Michigan");
        stateNames.put("MN", "Minnesota");
        stateNames.put("MS", "Mississippi");
        stateNames.put("MO", "Missouri");
        stateNames.put("MT", "Montana");
        stateNames.put("NE", "Nebraska");
        stateNames.put("NV", "Nevada");
        stateNames.put("NH", "New Hampshire");
        stateNames.put("NJ", "New Jersey");
        stateNames.put("NM", "New Mexico");
        stateNames.put("NY", "New York");
        stateNames.put("NC", "North Carolina");
        stateNames.put("ND", "North Dakota");
        stateNames.put("OH", "Ohio");
        stateNames.put("OK", "Oklahoma");
        stateNames.put("OR", "Oregon");
        stateNames.put("PA", "Pennsylvania");
        stateNames.put("RI", "Rhode Island");
        stateNames.put("SC", "South Carolina");
        stateNames.put("SD", "South Dakota");
        stateNames.put("TN", "Tennessee");
        stateNames.put("TX", "Texas");
        stateNames.put("UT", "Utah");
        stateNames.put("VT", "Vermont");
        stateNames.put("VA", "Virginia");
        stateNames.put("WA", "Washington");
        stateNames.put("WV", "West Virginia");
        stateNames.put("WI", "Wisconsin");
        stateNames.put("WY", "Wyoming");
        stateNames.put("DC", "District of Columbia");
        stateNames.put("PR", "Puerto Rico");
        stateNames.put("GU", "Guam");
        stateNames.put("AS", "American Samoa");
        stateNames.put("VI", "Virgin Islands");
        stateNames.put("MP", "Northern Mariana Islands");

        return memberRepository.findDistinctStates().stream()
                .map(code -> {
                    Map<String, String> entry = new LinkedHashMap<>();
                    entry.put("code", code);
                    entry.put("name", stateNames.getOrDefault(code, code));
                    return entry;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/api/members/by-state/{state}")
    public List<Member> getMembersByState(@PathVariable String state) {
        return memberRepository.findByStateOrderByChamberAscDistrictAsc(state);
    }

    @GetMapping("/api/members/search")
    public List<Member> searchMembers(@RequestParam String chamber, @RequestParam String query) {
        return memberRepository.findByChamberAndFirstNameContainingIgnoreCaseOrChamberAndLastNameContainingIgnoreCase(
                chamber, query, chamber, query);
    }

    @GetMapping("/api/members/{id}/finance-summary")
    public CampaignFinanceSummary getFinanceSummary(@PathVariable String id,
            @RequestParam(defaultValue = "2026") int cycle) {
        return campaignFinanceSummaryRepository.findByMemberBioguideIdAndCycle(id, cycle).orElse(null);
    }
}