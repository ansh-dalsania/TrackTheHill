package com.trackthehill.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommitteeDto {
    public String type; // "house", "senate", "joint"
    public String name;
    public String thomas_id;
    public List<SubcommitteeDto> subcommittees;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubcommitteeDto {
        public String name;
        public String thomas_id;
    }
}