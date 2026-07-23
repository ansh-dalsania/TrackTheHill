package com.trackthehill.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FecTotalsDto {
    public BigDecimal individual_contributions;
    public BigDecimal other_political_committee_contributions;
    public BigDecimal political_party_committee_contributions;
    public BigDecimal receipts;
    public BigDecimal disbursements;
    public BigDecimal last_cash_on_hand_end_period;
    public String coverage_end_date;
}