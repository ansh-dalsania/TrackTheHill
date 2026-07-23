package com.trackthehill.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mirrors JSON shape returned by Congress.gov's bill list endpoint.
 * Does not include sponsor, policy area, or introduced date (need detail call).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CongressBillDto {
    public Integer congress;
    public String number;
    public String type;
    public String title;
    public String originChamber;
    public String updateDate;
    public String url;
    public LatestAction latestAction;

    public static class LatestAction {
        public String actionDate;
        public String text;
    }
}