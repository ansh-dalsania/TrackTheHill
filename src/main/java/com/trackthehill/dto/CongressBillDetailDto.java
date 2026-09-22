package com.trackthehill.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CongressBillDetailDto {
    public Bill bill;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Bill {
        public String introducedDate;
        public PolicyArea policyArea;
        public List<Sponsor> sponsors;
        public String title;
        public LatestAction latestAction;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PolicyArea {
        public String name;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sponsor {
        public String bioguideId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LatestAction {
        public String actionDate;
        public String text;
    }
}