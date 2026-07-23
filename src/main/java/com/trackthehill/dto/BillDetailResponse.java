package com.trackthehill.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class BillDetailResponse {
    private String id;
    private String title;
    private String billType;
    private Integer billNumber;
    private String originChamber;
    private String latestActionText;
    private LocalDate latestActionDate;
    private LocalDate introducedDate;
    private String policyArea;
    private String summary;
    private String congressGovUrl;
    private String sponsorBioguideId;
    private String sponsorName;
    private List<CosponsorEntry> cosponsors;

    @Data
    @AllArgsConstructor
    public static class CosponsorEntry {
        private String bioguideId;
        private String name;
        private LocalDate sponsorshipDate;
        private Boolean isOriginalCosponsor;
    }
}