package com.reptrack;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
}