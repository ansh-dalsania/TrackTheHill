package com.reptrack;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TopDonorRepository extends JpaRepository<TopDonor, Long> {
    List<TopDonor> findByMemberBioguideIdAndCycleOrderByContributionAmountDesc(String bioguideId, Integer cycle);
    void deleteByMemberBioguideIdAndCycle(String bioguideId, Integer cycle);
}