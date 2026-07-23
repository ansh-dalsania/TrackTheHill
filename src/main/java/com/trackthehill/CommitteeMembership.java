package com.trackthehill;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Links a member to a committee they currently serve on (current assignments).
 */
@Entity
@Table(name = "committee_membership")
@Data
public class CommitteeMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_bioguide_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "committee_id", nullable = false)
    private Committee committee;

    private String title; // "Chair", "Ranking Member", or null for regular member

    private Integer rank;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}