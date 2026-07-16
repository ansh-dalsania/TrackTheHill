package com.reptrack;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberVoteRepository extends JpaRepository<MemberVote, Long> {
    Optional<MemberVote> findByVoteIdAndMemberBioguideId(String voteId, String memberBioguideId);
}