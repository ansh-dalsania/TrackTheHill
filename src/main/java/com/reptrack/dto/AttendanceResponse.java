package com.reptrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Summarizes a member's voting attendance, both lifetime (all recorded votes)
 * and recent (last 30 days).
 */
@Data
@AllArgsConstructor
public class AttendanceResponse {
    private long totalVotes;
    private long missedVotes;
    private double attendancePercentage;

    private long recentTotalVotes;
    private long recentMissedVotes;
    private double recentAttendancePercentage;
}