package com.taxgap.engine.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxgap.domain.entity.TaxException;
import com.taxgap.domain.entity.TaxRule;
import com.taxgap.domain.entity.Transaction;
import com.taxgap.domain.enums.TransactionType;
import com.taxgap.engine.TaxRuleExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Rule: REFUND_VALIDATION
 * Raises a MEDIUM exception when a REFUND transaction exceeds maxRefundAmount.
 * Config JSON: { "maxRefundAmount": 50000 }
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefundValidationRule implements TaxRuleExecutor {

    private final ObjectMapper objectMapper;

    @Override
    public String getRuleName() {

        return "REFUND_VALIDATION";
    }

    @Override
    public Optional<TaxException> execute(Transaction transaction, TaxRule rule) {
        if (transaction.getTransactionType() != TransactionType.REFUND) {
            return Optional.empty();
        }
        try {
            JsonNode config = objectMapper.readTree(rule.getConfigJson());
            BigDecimal maxRefund = config.get("maxRefundAmount").decimalValue();

            if (transaction.getAmount() != null && transaction.getAmount().compareTo(maxRefund) > 0) {
                return Optional.of(TaxException.builder()
                        .transactionId(transaction.getTransactionId())
                        .customerId(transaction.getCustomerId())
                        .ruleName(getRuleName())
                        .severity(rule.getSeverity())
                        .message(String.format(
                                "Refund amount %.2f exceeds maximum allowed refund %.2f",
                                transaction.getAmount(), maxRefund))
                        .build());
            }
        } catch (Exception e) {
            log.error("Error executing rule {}: {}", getRuleName(), e.getMessage());
        }
        return Optional.empty();
    }
}
