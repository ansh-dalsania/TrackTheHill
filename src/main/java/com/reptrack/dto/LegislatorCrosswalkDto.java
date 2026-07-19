package com.reptrack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Mirrors the relevant subset of unitedstates/congress-legislators'
 * legislators-current.yaml structure to extract the
 * bioguide-to-FEC-ID crosswalk.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegislatorCrosswalkDto {
    public Id id;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Id {
        public String bioguide;
        public List<String> fec;
    }
}