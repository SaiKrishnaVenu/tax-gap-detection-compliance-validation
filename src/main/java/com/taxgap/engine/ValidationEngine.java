package com.taxgap.engine;

import com.taxgap.dto.request.TransactionRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Component
public class ValidationEngine {

    public List<String> validate(TransactionRequest req) {
        List<String> errors = new ArrayList<>();

        // Required fields
        if (req.getTransactionId() == null || req.getTransactionId().isBlank()) {
            errors.add("transactionId is required");
        }
        if (req.getDate() == null) {
            errors.add("date is required");
        }
        if (req.getCustomerId() == null || req.getCustomerId().isBlank()) {
            errors.add("customerId is required");
        }
        if (req.getTransactionType() == null) {
            errors.add("transactionType is required (SALE / REFUND / EXPENSE)");
        }

         if (req.getAmount() == null) {
            errors.add("amount is required");
        } else if (req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("amount must be greater than 0");
        }

         if (req.getTaxRate() == null) {
            errors.add("taxRate is required");
        } else if (req.getTaxRate().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("taxRate must be >= 0");
        }

        return errors;
    }
}
