package com.reptrack;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Represents a single House roll call vote event.
 * Maps to the "vote" table created in V3__create_vote_tables.sql.
 *
 * bill is nullable — procedural votes don't tie to a specific bill.
 */
@Entity
@Table(name = "vote")
@Data
public class Vote {

    @Id
    private String id;

    @Column(nullable = false)
    private Integer congress;

    @Column(nullable = false)
    private Integer session;

    @Column(name = "roll_call_number", nullable = false)
    private Integer rollCallNumber;

    @ManyToOne
    @JoinColumn(name = "bill_id")
    private Bill bill;

    @Column(name = "vote_question")
    private String voteQuestion;

    @Column(name = "vote_type")
    private String voteType;

    private String result;

    @Column(name = "vote_date")
    private LocalDateTime voteDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String chamber; // "House" or "Senate"
}