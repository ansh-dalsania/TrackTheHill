package com.trackthehill;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BillRepository extends JpaRepository<Bill, String> {
    List<Bill> findByCongress(int congress);

    List<Bill> findBySponsorBioguideId(String bioguideId);

    @Query("SELECT DISTINCT b.policyArea FROM Bill b WHERE b.policyArea IS NOT NULL ORDER BY b.policyArea")
    List<String> findDistinctPolicyAreas();

    Page<Bill> findByPolicyArea(String policyArea, Pageable pageable);
}