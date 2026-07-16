package com.reptrack;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface MemberVoteRepository extends JpaRepository<MemberVote, Long> {
    Optional<MemberVote> findByVoteIdAndMemberBioguideId(String voteId, String memberBioguideId);

    @Query("SELECT mv FROM MemberVote mv " +
            "JOIN FETCH mv.vote v " +
            "LEFT JOIN FETCH v.bill " +
            "WHERE mv.member.bioguideId = :bioguideId " +
            "ORDER BY v.voteDate DESC")
    List<MemberVote> findVotingHistoryForMember(@Param("bioguideId") String bioguideId);

    @Query("SELECT COUNT(mv) FROM MemberVote mv WHERE mv.member.bioguideId = :bioguideId")
    long countTotalVotesForMember(@Param("bioguideId") String bioguideId);

    @Query("SELECT COUNT(mv) FROM MemberVote mv WHERE mv.member.bioguideId = :bioguideId AND mv.position = 'Not Voting'")
    long countMissedVotesForMember(@Param("bioguideId") String bioguideId);

    @Query("SELECT COUNT(mv) FROM MemberVote mv WHERE mv.member.bioguideId = :bioguideId AND mv.vote.voteDate >= :since")
    long countTotalVotesForMemberSince(@Param("bioguideId") String bioguideId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(mv) FROM MemberVote mv WHERE mv.member.bioguideId = :bioguideId AND mv.position = 'Not Voting' AND mv.vote.voteDate >= :since")
    long countMissedVotesForMemberSince(@Param("bioguideId") String bioguideId, @Param("since") LocalDateTime since);
}