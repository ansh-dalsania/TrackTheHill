package com.reptrack;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Represents one member's recorded position on a single roll call vote.
 * Maps to the "member_vote" table created in V3__create_vote_tables.sql.
 */
@Entity
@Table(name = "member_vote")
@Data
public class MemberVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;

    @ManyToOne
    @JoinColumn(name = "member_bioguide_id", nullable = false)
    private Member member;

    // 'Yea', 'Nay', 'Present', 'Not Voting'
    @Column(nullable = false)
    private String position;

    @Column(name = "party_at_vote")
    private String partyAtVote;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}