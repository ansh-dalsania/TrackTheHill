package com.reptrack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HouseVoteDto {
    public Integer congress;
    public Integer sessionNumber;
    public Integer rollCallNumber;
    public String legislationType;
    public String legislationNumber;
    public String result;
    public String startDate;
}