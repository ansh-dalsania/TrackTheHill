package com.trackthehill;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BillCosponsorRepository extends JpaRepository<BillCosponsor, Long> {
    Optional<BillCosponsor> findByBillIdAndMemberBioguideId(String billId, String memberBioguideId);
    List<BillCosponsor> findByMemberBioguideId(String memberBioguideId);
    List<BillCosponsor> findByBillId(String billId);
}