package com.reptrack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CongressSummaryDto {
    public String actionDate;
    public String actionDesc;
    public String text;
}