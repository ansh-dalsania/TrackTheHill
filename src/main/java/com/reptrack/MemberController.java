package com.reptrack;

import com.reptrack.service.MemberSyncService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import com.reptrack.dto.MemberVoteResponse;
import com.reptrack.service.HouseVoteSyncService; // if not already present
import java.util.stream.Collectors;

/**
 * Exposes Member data over HTTP.
 */
@RestController
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberSyncService memberSyncService;
    private final MemberVoteRepository memberVoteRepository;

    public MemberController(MemberRepository memberRepository, MemberSyncService memberSyncService,
            MemberVoteRepository memberVoteRepository) {
        this.memberRepository = memberRepository;
        this.memberSyncService = memberSyncService;
        this.memberVoteRepository = memberVoteRepository;
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
        return memberVoteRepository.findVotingHistoryForMember(id).stream()
                .map(mv -> new MemberVoteResponse(
                        mv.getVote().getId(),
                        mv.getVote().getChamber(),
                        mv.getVote().getVoteDate(),
                        mv.getVote().getVoteQuestion(),
                        mv.getVote().getResult(),
                        mv.getPosition(),
                        mv.getVote().getBill() != null ? mv.getVote().getBill().getId() : null,
                        mv.getVote().getBill() != null ? mv.getVote().getBill().getTitle() : null))
                .collect(Collectors.toList());
    }
}