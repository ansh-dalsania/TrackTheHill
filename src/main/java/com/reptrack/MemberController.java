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
import com.reptrack.service.HouseVoteSyncService; // if not already present
import java.util.stream.Collectors;
import com.reptrack.dto.AttendanceResponse;
import org.springframework.web.bind.annotation.RequestParam;
import com.reptrack.dto.PartisanScoreResponse;
import com.reptrack.service.VoteAnalyticsService;
import java.util.Map;

/**
 * Exposes Member data over HTTP.
 */
@RestController
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberSyncService memberSyncService;
    private final MemberVoteRepository memberVoteRepository;
    private final VoteAnalyticsService voteAnalyticsService;

    public MemberController(MemberRepository memberRepository, MemberSyncService memberSyncService,
            MemberVoteRepository memberVoteRepository, VoteAnalyticsService voteAnalyticsService) {
        this.memberRepository = memberRepository;
        this.memberSyncService = memberSyncService;
        this.memberVoteRepository = memberVoteRepository;
        this.voteAnalyticsService = voteAnalyticsService;
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
}