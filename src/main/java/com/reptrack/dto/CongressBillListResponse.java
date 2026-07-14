package com.reptrack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CongressBillListResponse {
    public List<CongressBillDto> bills;
    public Pagination pagination;

    public static class Pagination {
        public Integer count;
    }
}