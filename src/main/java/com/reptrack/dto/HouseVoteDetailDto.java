package com.reptrack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HouseVoteDetailDto {
    public HouseRollCallVoteMemberVotes houseRollCallVoteMemberVotes;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HouseRollCallVoteMemberVotes {
        public String voteQuestion;
        public String voteType;
        public List<MemberVoteEntry> results;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MemberVoteEntry {
        public String bioguideID; // note: capital ID, matches Congress.gov's actual field name
        public String voteCast;
        public String voteParty;
    }
}