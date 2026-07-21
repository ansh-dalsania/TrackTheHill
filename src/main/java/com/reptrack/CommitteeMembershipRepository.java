package com.reptrack;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommitteeMembershipRepository extends JpaRepository<CommitteeMembership, Long> {
    List<CommitteeMembership> findByMemberBioguideId(String bioguideId);
    void deleteAll(); // used for clear-and-replace sync
}