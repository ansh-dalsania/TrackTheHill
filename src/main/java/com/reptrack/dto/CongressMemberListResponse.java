package com.reptrack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Mirrors Congress.gov's JSON structure (list of members plus pagination info)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CongressMemberListResponse {
    public List<CongressMemberDto> members;
    public Pagination pagination;

    public static class Pagination {
        public Integer count;
    }
}