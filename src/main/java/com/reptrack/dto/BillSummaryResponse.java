package com.reptrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

/**
 * A lightweight bill representation for list/summary contexts.
 * 
 * Only has sponsor name and bioguide ID
 */
@Data
@AllArgsConstructor
public class BillSummaryResponse {
    private String id;
    private String title;
    private String billType;
    private Integer billNumber;
    private String originChamber;
    private String latestActionText;
    private LocalDate latestActionDate;
    private String policyArea;
    private String sponsorBioguideId;
    private String sponsorName;
}