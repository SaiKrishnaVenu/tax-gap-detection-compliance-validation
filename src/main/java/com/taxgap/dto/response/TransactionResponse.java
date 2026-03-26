package com.taxgap.dto.response;

import com.taxgap.domain.enums.ComplianceStatus;
import com.taxgap.domain.enums.ValidationStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class TransactionResponse {
    private String transactionId;
    private LocalDate date;
    private String customerId;
    private BigDecimal amount;
    private BigDecimal taxRate;
    private BigDecimal reportedTax;
    private String transactionType;
    private ValidationStatus validationStatus;
    private String failureReasons;
    private BigDecimal expectedTax;
    private BigDecimal taxGap;
    private ComplianceStatus complianceStatus;
}
