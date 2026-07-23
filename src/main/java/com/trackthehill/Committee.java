package com.trackthehill;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Represents a congressional committee or subcommittee. Sourced from
 * unitedstates/congress-legislators.
 *
 * parentCommittee is null for top-level committees, set for subcommittees.
 */
@Entity
@Table(name = "committee")
@Data
public class Committee {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String chamber;

    @ManyToOne
    @JoinColumn(name = "parent_committee_id")
    private Committee parentCommittee;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}