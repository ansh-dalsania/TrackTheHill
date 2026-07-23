package com.trackthehill.service;

import com.trackthehill.Member;
import com.trackthehill.TopDonor;
import com.trackthehill.TopDonorRepository;
import com.trackthehill.dto.FecScheduleADto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles the actual database writes for top donor syncing, scoped to a
 * single member's data per call.
 */
@Service
public class TopDonorPersistenceService {

    private final TopDonorRepository topDonorRepository;

    public TopDonorPersistenceService(TopDonorRepository topDonorRepository) {
        this.topDonorRepository = topDonorRepository;
    }

    @Transactional
    public void replaceTopDonors(Member member, int cycle, List<FecScheduleADto> contributions) {
        topDonorRepository.deleteByMemberBioguideIdAndCycle(member.getBioguideId(), cycle);
        for (FecScheduleADto dto : contributions) {
            TopDonor donor = new TopDonor();
            donor.setMember(member);
            donor.setCycle(cycle);
            donor.setContributorName(dto.contributor_name);
            donor.setContributorEmployer(dto.contributor_employer);
            donor.setContributorOccupation(dto.contributor_occupation);
            donor.setContributorState(dto.contributor_state);
            donor.setContributionAmount(dto.contribution_receipt_amount);
            if (dto.contribution_receipt_date != null) {
                donor.setContributionDate(LocalDate.parse(dto.contribution_receipt_date.substring(0, 10)));
            }
            donor.setCreatedAt(LocalDateTime.now());
            topDonorRepository.save(donor);
        }
    }
}