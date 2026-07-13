package com.reptrack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Mirrors outer JSON structure returned by Congress.gov — a list of members and 
 * pagination info.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CongressMemberListResponse {
    public List<CongressMemberDto> members;
}