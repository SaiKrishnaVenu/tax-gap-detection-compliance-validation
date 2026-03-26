package com.taxgap.domain.entity;

import com.taxgap.domain.enums.ComplianceStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TaxResultTest {

    @Test
    void shouldBuildTaxResult() {

        TaxResult tr = TaxResult.builder()
                .transactionId("TXN1")
                .expectedTax(new BigDecimal("180"))
                .taxGap(BigDecimal.ZERO)
                .complianceStatus(ComplianceStatus.COMPLIANT)
                .build();

        assertThat(tr.getTransactionId()).isEqualTo("TXN1");
        assertThat(tr.getComplianceStatus()).isEqualTo(ComplianceStatus.COMPLIANT);
    }
}