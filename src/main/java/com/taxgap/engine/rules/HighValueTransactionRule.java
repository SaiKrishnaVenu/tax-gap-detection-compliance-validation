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
 * Rule: HIGH_VALUE_TRANSACTION
 * Raises a HIGH exception when transaction amount exceeds the configured threshold.
 * Config JSON: { "threshold": 100000 }
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HighValueTransactionRule implements TaxRuleExecutor {

    private final ObjectMapper objectMapper;

    @Override
    public String getRuleName() {

        return "HIGH_VALUE_TRANSACTION";
    }

    @Override
    public Optional<TaxException> execute(Transaction transaction, TaxRule rule) {
        try {
            JsonNode config = objectMapper.readTree(rule.getConfigJson());
            BigDecimal threshold = config.get("threshold").decimalValue();

            if (transaction.getAmount() != null && transaction.getAmount().compareTo(threshold) > 0) {
                return Optional.of(TaxException.builder()
                        .transactionId(transaction.getTransactionId())
                        .customerId(transaction.getCustomerId())
                        .ruleName(getRuleName())
                        .severity(rule.getSeverity())
                        .message(String.format(
                                "Transaction amount %.2f exceeds high-value threshold %.2f",
                                transaction.getAmount(), threshold))
                        .build());
            }
        } catch (Exception e) {
            log.error("Error executing rule {}: {}", getRuleName(), e.getMessage());
        }
        return Optional.empty();
    }
}
