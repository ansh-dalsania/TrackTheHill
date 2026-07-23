package com.trackthehill.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CongressCosponsorDto {
    public String bioguideId;
    public String sponsorshipDate;
    public Boolean isOriginalCosponsor;
}