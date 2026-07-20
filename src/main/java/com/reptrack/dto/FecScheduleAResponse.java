package com.reptrack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FecScheduleAResponse {
    public List<FecScheduleADto> results;
}