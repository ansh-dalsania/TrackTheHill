package com.reptrack;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, String> {
    List<Bill> findByCongress(int congress);
    List<Bill> findBySponsorBioguideId(String bioguideId);
}