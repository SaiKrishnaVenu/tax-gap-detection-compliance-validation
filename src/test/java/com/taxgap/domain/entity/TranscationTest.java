package com.taxgap.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionTest {

    @Test
    void shouldBuildTransaction() {

        Transaction t = Transaction.builder()
                .transactionId("TXN1")
                .customerId("CUST1")
                .amount(new BigDecimal("1000"))
                .taxRate(new BigDecimal("0.18"))
                .reportedTax(new BigDecimal("180"))
                .date(LocalDate.now())
                .build();

        assertThat(t.getTransactionId()).isEqualTo("TXN1");
        assertThat(t.getCustomerId()).isEqualTo("CUST1");
        assertThat(t.getAmount()).isEqualTo(new BigDecimal("1000"));
    }

    @Test
    void shouldSetAndGetValues() {

        Transaction t = new Transaction();
        t.setTransactionId("TXN2");
        t.setCustomerId("CUST2");

        assertThat(t.getTransactionId()).isEqualTo("TXN2");
        assertThat(t.getCustomerId()).isEqualTo("CUST2");
    }
}