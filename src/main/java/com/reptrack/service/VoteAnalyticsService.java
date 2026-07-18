package com.reptrack.service;

import com.reptrack.Member;
import com.reptrack.MemberRepository;
import com.reptrack.MemberVote;
import com.reptrack.MemberVoteRepository;
import com.reptrack.dto.PartisanScoreResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes derived voting statistics — currently partisan score — from raw
 * MemberVote data.
 */
@Service
public class VoteAnalyticsService {

    private final MemberRepository memberRepository;
    private final MemberVoteRepository memberVoteRepository;

    public VoteAnalyticsService(MemberRepository memberRepository, MemberVoteRepository memberVoteRepository) {
        this.memberRepository = memberRepository;
        this.memberVoteRepository = memberVoteRepository;
    }

    public PartisanScoreResponse calculatePartisanScore(String bioguideId, int congress) {
        Member member = memberRepository.findById(bioguideId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + bioguideId));
        String party = member.getParty();

        // Get the member's own votes for current congress
        List<MemberVote> ownVotes = memberVoteRepository.findVotingHistoryForMember(bioguideId);

        // Get party position counts for every vote this member participated in
        List<Object[]> rawCounts = memberVoteRepository.getPartyPositionCountsForMemberVotes(bioguideId, congress,
                party);

        // Group into voteId, position, count
        Map<String, Map<String, Long>> partyCountsByVote = new HashMap<>();
        for (Object[] row : rawCounts) {
            String voteId = (String) row[0];
            String position = (String) row[1];
            Long count = (Long) row[2];
            partyCountsByVote.computeIfAbsent(voteId, k -> new HashMap<>()).put(position, count);
        }

        long votesConsidered = 0;
        long votesWithMajority = 0;

        for (MemberVote mv : ownVotes) {
            if (mv.getVote().getCongress() != congress)
                continue;

            String position = mv.getPosition();
            if (!"Yea".equals(position) && !"Nay".equals(position))
                continue; // skip Present/Not Voting

            Map<String, Long> counts = partyCountsByVote.get(mv.getVote().getId());
            if (counts == null)
                continue;

            long yeaCount = counts.getOrDefault("Yea", 0L);
            long nayCount = counts.getOrDefault("Nay", 0L);
            if (yeaCount == 0 && nayCount == 0)
                continue; // no clear party position

            String partyMajorityPosition = yeaCount >= nayCount ? "Yea" : "Nay";

            votesConsidered++;
            if (position.equals(partyMajorityPosition)) {
                votesWithMajority++;
            }
        }

        double percentage = votesConsidered == 0 ? 0.0 : ((double) votesWithMajority / votesConsidered) * 100;

        return new PartisanScoreResponse(congress, party, votesConsidered, votesWithMajority,
                Math.round(percentage * 100.0) / 100.0);
    }

    /**
     * Returns a map of voteId, whether the member's position matched their
     * party's majority position, for every vote the member participated in.
     */
    public Map<String, Boolean> getPartyAlignmentByVote(String bioguideId) {
        Member member = memberRepository.findById(bioguideId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + bioguideId));
        String party = member.getParty();

        List<MemberVote> ownVotes = memberVoteRepository.findVotingHistoryForMember(bioguideId);
        List<Object[]> rawCounts = memberVoteRepository.getPartyPositionCountsForAllMemberVotes(bioguideId, party);

        Map<String, Map<String, Long>> partyCountsByVote = new HashMap<>();
        for (Object[] row : rawCounts) {
            String voteId = (String) row[0];
            String position = (String) row[1];
            Long count = (Long) row[2];
            partyCountsByVote.computeIfAbsent(voteId, k -> new HashMap<>()).put(position, count);
        }

        Map<String, Boolean> alignment = new HashMap<>();
        for (MemberVote mv : ownVotes) {
            String position = mv.getPosition();
            if (!"Yea".equals(position) && !"Nay".equals(position))
                continue;

            Map<String, Long> counts = partyCountsByVote.get(mv.getVote().getId());
            if (counts == null)
                continue;

            long yeaCount = counts.getOrDefault("Yea", 0L);
            long nayCount = counts.getOrDefault("Nay", 0L);
            if (yeaCount == 0 && nayCount == 0)
                continue;

            String partyMajorityPosition = yeaCount >= nayCount ? "Yea" : "Nay";
            alignment.put(mv.getVote().getId(), position.equals(partyMajorityPosition));
        }

        return alignment;
    }
}