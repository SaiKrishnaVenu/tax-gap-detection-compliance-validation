package com.taxgap.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxgap.domain.entity.TaxException;
import com.taxgap.domain.entity.TaxRule;
import com.taxgap.domain.entity.Transaction;
import com.taxgap.domain.enums.Severity;
import com.taxgap.domain.enums.TransactionType;
import com.taxgap.domain.enums.ValidationStatus;
import com.taxgap.engine.rules.GstSlabViolationRule;
import com.taxgap.engine.rules.HighValueTransactionRule;
import com.taxgap.engine.rules.RefundValidationRule;
import com.taxgap.repository.TaxRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleEngineTest {

    @Mock
    private TaxRuleRepository ruleRepository;

    private RuleEngine ruleEngine;
    private ObjectMapper objectMapper;

    private HighValueTransactionRule highValueRule;
    private RefundValidationRule refundRule;
    private GstSlabViolationRule gstRule;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        highValueRule = new HighValueTransactionRule(objectMapper);
        refundRule = new RefundValidationRule(objectMapper);
        gstRule = new GstSlabViolationRule(objectMapper);

        ruleEngine = new RuleEngine(
                ruleRepository,
                List.of(highValueRule, refundRule, gstRule)
        );
    }


    private Transaction buildTransaction(BigDecimal amount, BigDecimal taxRate, TransactionType type) {
        return Transaction.builder()
                .transactionId("TXN-TEST")
                .customerId("CUST-001")
                .amount(amount)
                .taxRate(taxRate)
                .reportedTax(amount.multiply(taxRate))
                .transactionType(type)
                .validationStatus(ValidationStatus.SUCCESS)
                .date(LocalDate.now())
                .build();
    }

    private TaxRule buildRule(String name, String configJson, Severity severity) {
        return TaxRule.builder()
                .ruleName(name)
                .configJson(configJson)
                .severity(severity)
                .enabled(true)
                .build();
    }



    @Test
    void shouldTriggerHighValueRule_whenAmountExceedsThreshold() {
        TaxRule rule = buildRule("HIGH_VALUE_TRANSACTION", "{\"threshold\": 100000}", Severity.HIGH);
        Transaction t = buildTransaction(new BigDecimal("150000"), new BigDecimal("0.18"), TransactionType.SALE);

        when(ruleRepository.findByEnabledTrue()).thenReturn(List.of(rule));

        List<TaxException> exceptions = ruleEngine.evaluate(t);

        assertThat(exceptions).hasSize(1);
        assertThat(exceptions.get(0).getRuleName()).isEqualTo("HIGH_VALUE_TRANSACTION");
        assertThat(exceptions.get(0).getSeverity()).isEqualTo(Severity.HIGH);
    }

    @Test
    void shouldNotTriggerHighValueRule_whenAmountBelowOrEqualThreshold() {
        TaxRule rule = buildRule("HIGH_VALUE_TRANSACTION", "{\"threshold\": 100000}", Severity.HIGH);

        Transaction t1 = buildTransaction(new BigDecimal("50000"), new BigDecimal("0.18"), TransactionType.SALE);
        Transaction t2 = buildTransaction(new BigDecimal("100000"), new BigDecimal("0.18"), TransactionType.SALE);

        when(ruleRepository.findByEnabledTrue()).thenReturn(List.of(rule));

        assertThat(ruleEngine.evaluate(t1)).isEmpty();
        assertThat(ruleEngine.evaluate(t2)).isEmpty();
    }



    @Test
    void shouldTriggerRefundRule_whenRefundExceedsLimit() {
        TaxRule rule = buildRule("REFUND_VALIDATION", "{\"maxRefundAmount\": 50000}", Severity.MEDIUM);
        Transaction t = buildTransaction(new BigDecimal("60000"), new BigDecimal("0.18"), TransactionType.REFUND);

        when(ruleRepository.findByEnabledTrue()).thenReturn(List.of(rule));

        List<TaxException> exceptions = ruleEngine.evaluate(t);

        assertThat(exceptions).hasSize(1);
        assertThat(exceptions.get(0).getRuleName()).isEqualTo("REFUND_VALIDATION");
    }

    @Test
    void shouldNotTriggerRefundRule_whenNotRefundOrWithinLimit() {
        TaxRule rule = buildRule("REFUND_VALIDATION", "{\"maxRefundAmount\": 50000}", Severity.MEDIUM);

        Transaction saleTxn = buildTransaction(new BigDecimal("60000"), new BigDecimal("0.18"), TransactionType.SALE);
        Transaction validRefund = buildTransaction(new BigDecimal("30000"), new BigDecimal("0.18"), TransactionType.REFUND);

        when(ruleRepository.findByEnabledTrue()).thenReturn(List.of(rule));

        assertThat(ruleEngine.evaluate(saleTxn)).isEmpty();
        assertThat(ruleEngine.evaluate(validRefund)).isEmpty();
    }


    @Test
    void shouldTriggerGstRule_whenAboveSlabWithLowTaxRate() {
        TaxRule rule = buildRule("GST_SLAB_VIOLATION",
                "{\"slabThreshold\": 10000, \"minTaxRate\": 0.18}", Severity.HIGH);

        Transaction t = buildTransaction(new BigDecimal("20000"), new BigDecimal("0.05"), TransactionType.SALE);

        when(ruleRepository.findByEnabledTrue()).thenReturn(List.of(rule));

        List<TaxException> exceptions = ruleEngine.evaluate(t);

        assertThat(exceptions).hasSize(1);
        assertThat(exceptions.get(0).getRuleName()).isEqualTo("GST_SLAB_VIOLATION");
    }

    @Test
    void shouldNotTriggerGstRule_whenValidConditions() {
        TaxRule rule = buildRule("GST_SLAB_VIOLATION",
                "{\"slabThreshold\": 10000, \"minTaxRate\": 0.18}", Severity.HIGH);

        Transaction correctRate = buildTransaction(new BigDecimal("20000"), new BigDecimal("0.18"), TransactionType.SALE);
        Transaction belowSlab = buildTransaction(new BigDecimal("5000"), new BigDecimal("0.05"), TransactionType.SALE);

        when(ruleRepository.findByEnabledTrue()).thenReturn(List.of(rule));

        assertThat(ruleEngine.evaluate(correctRate)).isEmpty();
        assertThat(ruleEngine.evaluate(belowSlab)).isEmpty();
    }



    @Test
    void shouldTriggerMultipleRules_whenConditionsMatch() {
        List<TaxRule> rules = List.of(
                buildRule("HIGH_VALUE_TRANSACTION", "{\"threshold\": 100000}", Severity.HIGH),
                buildRule("GST_SLAB_VIOLATION", "{\"slabThreshold\": 10000, \"minTaxRate\": 0.18}", Severity.HIGH)
        );

        Transaction t = buildTransaction(new BigDecimal("150000"), new BigDecimal("0.05"), TransactionType.SALE);

        when(ruleRepository.findByEnabledTrue()).thenReturn(rules);

        List<TaxException> exceptions = ruleEngine.evaluate(t);

        assertThat(exceptions).hasSize(2);
    }



    @Test
    void shouldReturnEmpty_whenNoActiveRules() {
        when(ruleRepository.findByEnabledTrue()).thenReturn(List.of());

        Transaction t = buildTransaction(new BigDecimal("999999"), new BigDecimal("0.01"), TransactionType.SALE);

        List<TaxException> exceptions = ruleEngine.evaluate(t);

        assertThat(exceptions).isEmpty();
    }
}