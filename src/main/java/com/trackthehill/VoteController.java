package com.trackthehill;

import com.trackthehill.dto.VoteDetailResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Read access to individual roll call votes, covering both House and
 * Senate, since Vote/MemberVote store both under one schema.
 */
@RestController
public class VoteController {

    private final VoteRepository voteRepository;
    private final MemberVoteRepository memberVoteRepository;

    public VoteController(VoteRepository voteRepository, MemberVoteRepository memberVoteRepository) {
        this.voteRepository = voteRepository;
        this.memberVoteRepository = memberVoteRepository;
    }

    @GetMapping("/api/votes/{id}")
    public VoteDetailResponse getVoteDetail(@PathVariable String id) {
        Vote vote = voteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vote not found: " + id));

        List<VoteDetailResponse.MemberPositionEntry> positions = memberVoteRepository.findAllPositionsForVote(id)
                .stream()
                .map(mv -> new VoteDetailResponse.MemberPositionEntry(
                        mv.getMember().getBioguideId(),
                        mv.getMember().getFirstName(),
                        mv.getMember().getLastName(),
                        mv.getMember().getParty(),
                        mv.getMember().getState(),
                        mv.getPosition()))
                .collect(Collectors.toList());

        return new VoteDetailResponse(
                vote.getId(),
                vote.getChamber(),
                vote.getCongress(),
                vote.getVoteDate(),
                vote.getVoteQuestion(),
                vote.getVoteType(),
                vote.getResult(),
                vote.getBill() != null ? vote.getBill().getId() : null,
                vote.getBill() != null ? vote.getBill().getTitle() : null,
                positions);
    }
}