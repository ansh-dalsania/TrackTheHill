package com.trackthehill.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Summarizes how often a member voted with the majority position of their
 * own party within a specific Congress.
 */
@Data
@AllArgsConstructor
public class PartisanScoreResponse {
    private int congress;
    private String party;
    private long votesConsidered;
    private long votesWithPartyMajority;
    private double partisanScorePercentage;
}