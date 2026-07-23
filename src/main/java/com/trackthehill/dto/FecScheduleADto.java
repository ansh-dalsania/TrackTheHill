package com.trackthehill.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FecScheduleADto {
    public String contributor_name;
    public String contributor_employer;
    public String contributor_occupation;
    public String contributor_state;
    public BigDecimal contribution_receipt_amount;
    public String contribution_receipt_date;
}