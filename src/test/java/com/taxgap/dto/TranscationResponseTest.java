package com.taxgap.dto;

import com.taxgap.domain.enums.ComplianceStatus;
import com.taxgap.domain.enums.ValidationStatus;
import com.taxgap.dto.response.TransactionResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionResponseTest {

    @Test
    void shouldBuildTransactionResponseSuccessfully() {

        TransactionResponse res = TransactionResponse.builder()
                .transactionId("TXN1")
                .date(LocalDate.now())
                .customerId("CUST1")
                .amount(new BigDecimal("1000"))
                .taxRate(new BigDecimal("0.18"))
                .reportedTax(new BigDecimal("180"))
                .transactionType("SALE")
                .validationStatus(ValidationStatus.SUCCESS)
                .failureReasons(null)
                .expectedTax(new BigDecimal("180"))
                .taxGap(BigDecimal.ZERO)
                .complianceStatus(ComplianceStatus.COMPLIANT)
                .build();

        assertThat(res.getTransactionId()).isEqualTo("TXN1");
        assertThat(res.getCustomerId()).isEqualTo("CUST1");
        assertThat(res.getComplianceStatus()).isEqualTo(ComplianceStatus.COMPLIANT);
    }

    @Test
    void shouldHandleNullValues() {
        TransactionResponse res = TransactionResponse.builder().build();

        assertThat(res.getTransactionId()).isNull();
        assertThat(res.getComplianceStatus()).isNull();
    }
}