package com.taxgap.engine;

import com.taxgap.domain.enums.TransactionType;
import com.taxgap.dto.request.TransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationEngineTest {

    private ValidationEngine validationEngine;

    @BeforeEach
    void setUp() {

        validationEngine = new ValidationEngine();
    }

    private TransactionRequest validRequest() {
        TransactionRequest req = new TransactionRequest();
        req.setTransactionId("TXN001");
        req.setDate(LocalDate.now());
        req.setCustomerId("CUST001");
        req.setAmount(new BigDecimal("5000"));
        req.setTaxRate(new BigDecimal("0.18"));
        req.setReportedTax(new BigDecimal("900"));
        req.setTransactionType(TransactionType.SALE);
        return req;
    }

    @Test
    void shouldPassForValidRequest() {
        List<String> errors = validationEngine.validate(validRequest());
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldFailWhenTransactionIdIsNull() {
        TransactionRequest req = validRequest();
        req.setTransactionId(null);
        List<String> errors = validationEngine.validate(req);
        assertThat(errors).anyMatch(e -> e.contains("transactionId"));
    }

    @Test
    void shouldFailWhenTransactionIdIsBlank() {
        TransactionRequest req = validRequest();
        req.setTransactionId("   ");
        List<String> errors = validationEngine.validate(req);
        assertThat(errors).anyMatch(e -> e.contains("transactionId"));
    }

    @Test
    void shouldFailWhenDateIsNull() {
        TransactionRequest req = validRequest();
        req.setDate(null);
        List<String> errors = validationEngine.validate(req);
        assertThat(errors).anyMatch(e -> e.contains("date"));
    }

    @Test
    void shouldFailWhenCustomerIdIsNull() {
        TransactionRequest req = validRequest();
        req.setCustomerId(null);
        List<String> errors = validationEngine.validate(req);
        assertThat(errors).anyMatch(e -> e.contains("customerId"));
    }

    @Test
    void shouldFailWhenAmountIsNull() {
        TransactionRequest req = validRequest();
        req.setAmount(null);
        List<String> errors = validationEngine.validate(req);
        assertThat(errors).anyMatch(e -> e.contains("amount"));
    }

    @Test
    void shouldFailWhenAmountIsZero() {
        TransactionRequest req = validRequest();
        req.setAmount(BigDecimal.ZERO);
        List<String> errors = validationEngine.validate(req);
        assertThat(errors).anyMatch(e -> e.contains("amount must be greater than 0"));
    }

    @Test
    void shouldFailWhenAmountIsNegative() {
        TransactionRequest req = validRequest();
        req.setAmount(new BigDecimal("-100"));
        List<String> errors = validationEngine.validate(req);
        assertThat(errors).anyMatch(e -> e.contains("amount must be greater than 0"));
    }

    @Test
    void shouldFailWhenTaxRateIsNull() {
        TransactionRequest req = validRequest();
        req.setTaxRate(null);
        List<String> errors = validationEngine.validate(req);
        assertThat(errors).anyMatch(e -> e.contains("taxRate"));
    }

    @Test
    void shouldFailWhenTaxRateIsNegative() {
        TransactionRequest req = validRequest();
        req.setTaxRate(new BigDecimal("-0.05"));
        List<String> errors = validationEngine.validate(req);
        assertThat(errors).anyMatch(e -> e.contains("taxRate"));
    }

    @Test
    void shouldFailWhenTransactionTypeIsNull() {
        TransactionRequest req = validRequest();
        req.setTransactionType(null);
        List<String> errors = validationEngine.validate(req);
        assertThat(errors).anyMatch(e -> e.contains("transactionType"));
    }

    @Test
    void shouldAccumulateMultipleErrors() {
        TransactionRequest req = new TransactionRequest();
        // All fields null / invalid
        List<String> errors = validationEngine.validate(req);
        assertThat(errors.size()).isGreaterThanOrEqualTo(4);
    }

    @Test
    void shouldPassWithZeroTaxRate() {
        TransactionRequest req = validRequest();
        req.setTaxRate(BigDecimal.ZERO);
        List<String> errors = validationEngine.validate(req);
        assertThat(errors).isEmpty();
    }
}
