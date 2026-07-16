package com.reptrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Summarizes a member's voting attendance within the current Congress,
 * and within last thirty days.
 */
@Data
@AllArgsConstructor
public class AttendanceResponse {
    private int congress;
    private long totalVotes;
    private long missedVotes;
    private double attendancePercentage;

    private long recentTotalVotes;
    private long recentMissedVotes;
    private double recentAttendancePercentage;
}