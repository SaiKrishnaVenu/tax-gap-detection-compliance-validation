package com.taxgap.engine.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxgap.domain.entity.TaxException;
import com.taxgap.domain.entity.TaxRule;
import com.taxgap.domain.entity.Transaction;
import com.taxgap.engine.TaxRuleExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Rule: GST_SLAB_VIOLATION
 * Raises a HIGH exception when amount exceeds slabThreshold but taxRate < minTaxRate.
 * Config JSON: { "slabThreshold": 10000, "minTaxRate": 0.18 }
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GstSlabViolationRule implements TaxRuleExecutor {

    private final ObjectMapper objectMapper;

    @Override
    public String getRuleName() {

        return "GST_SLAB_VIOLATION";
    }

    @Override
    public Optional<TaxException> execute(Transaction transaction, TaxRule rule) {
        if (transaction.getAmount() == null || transaction.getTaxRate() == null) {
            return Optional.empty();
        }
        try {
            JsonNode config = objectMapper.readTree(rule.getConfigJson());
            BigDecimal slabThreshold = config.get("slabThreshold").decimalValue();
            BigDecimal minTaxRate    = config.get("minTaxRate").decimalValue();

            boolean aboveSlab      = transaction.getAmount().compareTo(slabThreshold) > 0;
            boolean taxRateTooLow  = transaction.getTaxRate().compareTo(minTaxRate) < 0;

            if (aboveSlab && taxRateTooLow) {
                return Optional.of(TaxException.builder()
                        .transactionId(transaction.getTransactionId())
                        .customerId(transaction.getCustomerId())
                        .ruleName(getRuleName())
                        .severity(rule.getSeverity())
                        .message(String.format(
                                "Amount %.2f exceeds GST slab threshold %.2f but taxRate %.4f is below required %.4f",
                                transaction.getAmount(), slabThreshold,
                                transaction.getTaxRate(), minTaxRate))
                        .build());
            }
        } catch (Exception e) {
            log.error("Error executing rule {}: {}", getRuleName(), e.getMessage());
        }
        return Optional.empty();
    }
}
