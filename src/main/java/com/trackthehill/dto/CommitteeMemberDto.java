package com.trackthehill.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommitteeMemberDto {
    public String bioguide;
    public String title; // only present for Chair/Ranking Member
    public Integer rank;
}