package com.reptrack;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a single member of Congress.
 * Maps directly to the "member" table created in V1__create_member_table.sql.
 */
@Entity
@Table(name = "member")
@Data // Lombok: auto-generates getters, setters, equals/hashCode, and toString
public class Member {

    // Primary key — Congress.gov's official unique ID for this member
    @Id
    @Column(name = "bioguide_id")
    private String bioguideId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    // Political party ("D", "R", "I")
    @Column(nullable = false)
    private String party;

    // Two letter abbreviation
    @Column(nullable = false)
    private String state;

    // Null for Senators because they don't represent a district
    private String district;

    // "House" or "Senate"
    @Column(nullable = false)
    private String chamber;

    @Column(name = "headshot_url")
    private String headshotUrl;

    // Start of current term — used to compute "years in office"
    @Column(name = "term_start_date")
    private LocalDate termStartDate;

    @Column(name = "official_website_url")
    private String officialWebsiteUrl;

    @Column(name = "office_email")
    private String officeEmail;

    // False if the member has left office (lost re-election, retired, resigned,
    // etc.)
    @Column(name = "in_office", nullable = false)
    private Boolean inOffice;

    @Column(name = "twitter_handle")
    private String twitterHandle;

    // Record-keeping timestamps — when this row was first created and last synced
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "nominate_dim1")
    private Double nominateDim1;

    @Column(name = "nominate_dim2")
    private Double nominateDim2;
}