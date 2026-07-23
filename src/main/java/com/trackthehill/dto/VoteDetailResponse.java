package com.trackthehill.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class VoteDetailResponse {
    private String voteId;
    private String chamber;
    private int congress;
    private LocalDateTime voteDate;
    private String voteQuestion;
    private String voteType;
    private String result;
    private String billId;
    private String billTitle;
    private List<MemberPositionEntry> memberPositions;

    @Data
    @AllArgsConstructor
    public static class MemberPositionEntry {
        private String bioguideId;
        private String firstName;
        private String lastName;
        private String party;
        private String state;
        private String position;
    }
}