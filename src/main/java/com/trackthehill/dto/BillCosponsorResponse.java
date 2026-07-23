package com.trackthehill.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

/**
 * A cosponsorship record paired with a brief bill summary.
 */
@Data
@AllArgsConstructor
public class BillCosponsorResponse {
    private BillSummaryResponse bill;
    private LocalDate sponsorshipDate;
    private Boolean isOriginalCosponsor;
}