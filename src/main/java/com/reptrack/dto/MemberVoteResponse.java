package com.reptrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Creates a vote for an individual member, combining from MemberVote, Vote,
 * and sometimes Bill.
 */
@Data
@AllArgsConstructor
public class MemberVoteResponse {
    private String voteId;
    private String chamber;
    private LocalDateTime voteDate;
    private String voteQuestion;
    private String result;
    private String position; //Yea/Nay/Present/Not Voting
    private String billId; //nullable (null for procedural votes)
    private String billTitle; //nullable
}