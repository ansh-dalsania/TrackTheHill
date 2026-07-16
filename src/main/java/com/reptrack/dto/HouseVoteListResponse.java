package com.reptrack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HouseVoteListResponse {
    public List<HouseVoteDto> houseRollCallVotes;
    public Pagination pagination;

    public static class Pagination {
        public Integer count;
    }
}