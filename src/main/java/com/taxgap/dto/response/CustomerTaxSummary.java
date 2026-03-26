package com.taxgap.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CustomerTaxSummary {
    private String customerId;
    private BigDecimal totalAmount;
    private BigDecimal totalReportedTax;
    private BigDecimal totalExpectedTax;
    private BigDecimal totalTaxGap;
    private long totalTransactions;
    private long nonCompliantTransactions;
    private double complianceScore;
}
