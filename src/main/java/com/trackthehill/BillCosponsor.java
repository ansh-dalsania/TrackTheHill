package com.trackthehill;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents one member's cosponsorship of a bill. Bills can have many cosponsors 
 * and a member can cosponsor many bills.
 *
 * isOriginalCosponsor distinguishes members who signed on when the bill was
 * first introduced from those who added their support later.
 */
@Entity
@Table(name = "bill_cosponsor")
@Data
public class BillCosponsor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @ManyToOne
    @JoinColumn(name = "member_bioguide_id", nullable = false)
    private Member member;

    @Column(name = "sponsorship_date")
    private LocalDate sponsorshipDate;

    @Column(name = "is_original_cosponsor")
    private Boolean isOriginalCosponsor;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}