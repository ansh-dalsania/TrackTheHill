package com.trackthehill.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FecTotalsResponse {
    public List<FecTotalsDto> results;
}