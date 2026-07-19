package com.reptrack;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CampaignFinanceSummaryRepository extends JpaRepository<CampaignFinanceSummary, Long> {
    Optional<CampaignFinanceSummary> findByMemberBioguideIdAndCycle(String bioguideId, Integer cycle);
}