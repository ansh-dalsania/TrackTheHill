package com.trackthehill;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents one of a member's largest individual itemized campaign
 * contributions for a given cycle, sourced from FEC's schedule_a endpoint.
 * This captures top single-transaction donors, not aggregated lifetime
 * totals per person.
 */
@Entity
@Table(name = "top_donor")
@Data
public class TopDonor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_bioguide_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private Integer cycle;

    @Column(name = "contributor_name")
    private String contributorName;

    @Column(name = "contributor_employer")
    private String contributorEmployer;

    @Column(name = "contributor_occupation")
    private String contributorOccupation;

    @Column(name = "contributor_state")
    private String contributorState;

    @Column(name = "contribution_amount")
    private BigDecimal contributionAmount;

    @Column(name = "contribution_date")
    private LocalDate contributionDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}