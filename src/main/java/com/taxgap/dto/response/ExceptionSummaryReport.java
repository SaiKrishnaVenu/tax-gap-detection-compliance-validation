package com.taxgap.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ExceptionSummaryReport {
    private long totalExceptions;
    private Map<String, Long> countBySeverity;
    private Map<String, Long> countByCustomer;
}
