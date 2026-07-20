package com.reptrack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FecCommitteeDto {
    public String committee_id;
    public String designation;
    public List<Integer> cycles;
}