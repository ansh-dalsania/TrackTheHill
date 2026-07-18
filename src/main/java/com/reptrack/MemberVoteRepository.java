package com.reptrack;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberVoteRepository extends JpaRepository<MemberVote, Long> {
    Optional<MemberVote> findByVoteIdAndMemberBioguideId(String voteId, String memberBioguideId);

    @Query("SELECT mv FROM MemberVote mv " +
            "JOIN FETCH mv.vote v " +
            "LEFT JOIN FETCH v.bill " +
            "WHERE mv.member.bioguideId = :bioguideId " +
            "ORDER BY v.voteDate DESC")
    List<MemberVote> findVotingHistoryForMember(@Param("bioguideId") String bioguideId);

    @Query("SELECT COUNT(mv) FROM MemberVote mv WHERE mv.member.bioguideId = :bioguideId AND mv.vote.congress = :congress")
    long countTotalVotesForMemberInCongress(@Param("bioguideId") String bioguideId, @Param("congress") int congress);

    @Query("SELECT COUNT(mv) FROM MemberVote mv WHERE mv.member.bioguideId = :bioguideId AND mv.position = 'Not Voting' AND mv.vote.congress = :congress")
    long countMissedVotesForMemberInCongress(@Param("bioguideId") String bioguideId, @Param("congress") int congress);

    @Query("SELECT COUNT(mv) FROM MemberVote mv WHERE mv.member.bioguideId = :bioguideId AND mv.vote.voteDate >= :since")
    long countTotalVotesForMemberSince(@Param("bioguideId") String bioguideId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(mv) FROM MemberVote mv WHERE mv.member.bioguideId = :bioguideId AND mv.position = 'Not Voting' AND mv.vote.voteDate >= :since")
    long countMissedVotesForMemberSince(@Param("bioguideId") String bioguideId, @Param("since") LocalDateTime since);

    @Query("""
            SELECT mv.vote.id, mv.position, COUNT(mv)
            FROM MemberVote mv
            WHERE mv.vote.id IN (
                SELECT mv2.vote.id FROM MemberVote mv2
                WHERE mv2.member.bioguideId = :bioguideId AND mv2.vote.congress = :congress
            )
            AND mv.member.party = :party
            GROUP BY mv.vote.id, mv.position
            """)
    List<Object[]> getPartyPositionCountsForMemberVotes(@Param("bioguideId") String bioguideId,
            @Param("congress") int congress,
            @Param("party") String party);
}