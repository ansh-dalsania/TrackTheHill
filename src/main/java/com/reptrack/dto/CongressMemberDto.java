package com.reptrack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Mirrors the JSON shape returned by Congress.gov's member list endpoint.
 * Field names match JSON.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CongressMemberDto {
    public String bioguideId;
    public String name;
    public String partyName;
    public String state;
    public Integer district;
    public Depiction depiction;
    public Terms terms;

    public static class Depiction {
        public String imageUrl;
    }

    public static class Terms {
        public List<TermItem> item;
    }

    public static class TermItem {
        public String chamber;
        public Integer startYear;
    }
}