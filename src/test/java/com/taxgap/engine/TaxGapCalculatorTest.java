package com.taxgap.engine;

import com.taxgap.domain.entity.TaxResult;
import com.taxgap.domain.entity.Transaction;
import com.taxgap.domain.enums.ComplianceStatus;
import com.taxgap.domain.enums.TransactionType;
import com.taxgap.domain.enums.ValidationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TaxGapCalculatorTest {

    private TaxGapCalculator calculator;

    @BeforeEach
    void setUp() {

        calculator = new TaxGapCalculator();
    }

    private Transaction buildTransaction(BigDecimal amount, BigDecimal taxRate, BigDecimal reportedTax) {
        return Transaction.builder()
                .transactionId("TXN001")
                .customerId("CUST001")
                .amount(amount)
                .taxRate(taxRate)
                .reportedTax(reportedTax)
                .transactionType(TransactionType.SALE)
                .validationStatus(ValidationStatus.SUCCESS)
                .build();
    }

    @Test
    void shouldBeCompliantWhenTaxGapWithinTolerance() {
        Transaction t = buildTransaction(
                new BigDecimal("10000"),
                new BigDecimal("0.18"),
                new BigDecimal("1800.00")); // exact

        TaxResult result = calculator.calculate(t);

        assertThat(result.getExpectedTax()).isEqualByComparingTo("1800.0000");
        assertThat(result.getTaxGap()).isEqualByComparingTo("0.0000");
        assertThat(result.getComplianceStatus()).isEqualTo(ComplianceStatus.COMPLIANT);
    }

    @Test
    void shouldBeCompliantWhenTaxGapExactlyAtTolerance() {
        // reportedTax is 1 less → taxGap = 1.00 → still COMPLIANT
        Transaction t = buildTransaction(
                new BigDecimal("10000"),
                new BigDecimal("0.18"),
                new BigDecimal("1799.00"));

        TaxResult result = calculator.calculate(t);

        assertThat(result.getTaxGap()).isEqualByComparingTo("1.0000");
        assertThat(result.getComplianceStatus()).isEqualTo(ComplianceStatus.COMPLIANT);
    }

    @Test
    void shouldBeUnderpaidWhenReportedTaxTooLow() {
        Transaction t = buildTransaction(
                new BigDecimal("10000"),
                new BigDecimal("0.18"),
                new BigDecimal("1700.00")); // gap = 100

        TaxResult result = calculator.calculate(t);

        assertThat(result.getTaxGap()).isEqualByComparingTo("100.0000");
        assertThat(result.getComplianceStatus()).isEqualTo(ComplianceStatus.UNDERPAID);
    }

    @Test
    void shouldBeOverpaidWhenReportedTaxTooHigh() {
        Transaction t = buildTransaction(
                new BigDecimal("10000"),
                new BigDecimal("0.18"),
                new BigDecimal("1900.00")); // gap = -100

        TaxResult result = calculator.calculate(t);

        assertThat(result.getTaxGap()).isEqualByComparingTo("-100.0000");
        assertThat(result.getComplianceStatus()).isEqualTo(ComplianceStatus.OVERPAID);
    }

    @Test
    void shouldBeNonCompliantWhenReportedTaxIsNull() {
        Transaction t = buildTransaction(
                new BigDecimal("10000"),
                new BigDecimal("0.18"),
                null);

        TaxResult result = calculator.calculate(t);

        assertThat(result.getComplianceStatus()).isEqualTo(ComplianceStatus.NON_COMPLIANT);
        assertThat(result.getReportedTax()).isNull();
        assertThat(result.getTaxGap()).isEqualByComparingTo(result.getExpectedTax());
    }

    @Test
    void shouldComputeExpectedTaxCorrectly() {
        Transaction t = buildTransaction(
                new BigDecimal("5000"),
                new BigDecimal("0.05"),
                new BigDecimal("250.00"));

        TaxResult result = calculator.calculate(t);

        assertThat(result.getExpectedTax()).isEqualByComparingTo("250.0000");
    }

    @Test
    void shouldHandleZeroTaxRate() {
        Transaction t = buildTransaction(
                new BigDecimal("5000"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"));

        TaxResult result = calculator.calculate(t);

        assertThat(result.getExpectedTax()).isEqualByComparingTo("0.0000");
        assertThat(result.getComplianceStatus()).isEqualTo(ComplianceStatus.COMPLIANT);
    }

    @Test
    void shouldSetTransactionIdAndCustomerId() {
        Transaction t = buildTransaction(
                new BigDecimal("1000"),
                new BigDecimal("0.10"),
                new BigDecimal("100.00"));

        TaxResult result = calculator.calculate(t);

        assertThat(result.getTransactionId()).isEqualTo("TXN001");
        assertThat(result.getCustomerId()).isEqualTo("CUST001");
    }
}
