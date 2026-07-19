package com.reptrack;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Aggregated campaign finance totals for one member in one election cycle,
 * sourced from FEC's /candidate/{id}/totals/ endpoint. Covers individual,
 * PAC, and party committee contributions (no super PAC spending, which
 * legally can't flow through a candidate's own committee and is tracked
 * separately).
 */
@Entity
@Table(name = "campaign_finance_summary")
@Data
public class CampaignFinanceSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_bioguide_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private Integer cycle;

    @Column(name = "individual_contributions")
    private BigDecimal individualContributions;

    @Column(name = "pac_contributions")
    private BigDecimal pacContributions;

    @Column(name = "party_contributions")
    private BigDecimal partyContributions;

    @Column(name = "total_receipts")
    private BigDecimal totalReceipts;

    @Column(name = "total_disbursements")
    private BigDecimal totalDisbursements;

    @Column(name = "cash_on_hand")
    private BigDecimal cashOnHand;

    @Column(name = "coverage_end_date")
    private LocalDate coverageEndDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}