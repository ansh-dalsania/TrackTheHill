package com.trackthehill;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a single bill or resolution introduced in Congress.
 * Maps to the "bill" table created in V2__create_bill_table.sql.
 *
 * id mirrors Congress' bill identification scheme.
 *
 * Detail-level fields (sponsor, introducedDate, policyArea, summary) are nullable —
 * they're only populated for bills that pass "has progressed" filter during
 * enrichment sync pass, not for every bill in the initial list-level sync.
 */
@Entity
@Table(name = "bill")
@Data
public class Bill {

    @Id
    private String id;

    @Column(nullable = false)
    private Integer congress;

    @Column(name = "bill_type", nullable = false)
    private String billType;

    @Column(name = "bill_number", nullable = false)
    private Integer billNumber;

    @Column(nullable = false)
    private String title;

    // "House" or "Senate"
    @Column(name = "origin_chamber", nullable = false)
    private String originChamber;

    @Column(name = "latest_action_text")
    private String latestActionText;

    @Column(name = "latest_action_date")
    private LocalDate latestActionDate;

    @Column(name = "update_date")
    private LocalDate updateDate;

    @Column(name = "congress_gov_url")
    private String congressGovUrl;

    // Detail-level fields — populated during enrichment pass, nullable until then
    @ManyToOne
    @JoinColumn(name = "sponsor_bioguide_id")
    private Member sponsor;

    @Column(name = "introduced_date")
    private LocalDate introducedDate;

    @Column(name = "policy_area")
    private String policyArea;

    private String summary;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}