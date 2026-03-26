package com.taxgap.engine;

import com.taxgap.domain.entity.TaxResult;
import com.taxgap.domain.entity.Transaction;
import com.taxgap.domain.enums.ComplianceStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class TaxGapCalculator {

    private static final BigDecimal TOLERANCE = new BigDecimal("1.00");

    public TaxResult calculate(Transaction transaction) {
        BigDecimal amount      = transaction.getAmount();
        BigDecimal taxRate     = transaction.getTaxRate();
        BigDecimal reportedTax = transaction.getReportedTax();

        BigDecimal expectedTax = amount.multiply(taxRate).setScale(4, RoundingMode.HALF_UP);


        if (reportedTax == null) {
            return TaxResult.builder()
                    .transactionId(transaction.getTransactionId())
                    .customerId(transaction.getCustomerId())
                    .amount(amount)
                    .taxRate(taxRate)
                    .reportedTax(null)
                    .expectedTax(expectedTax)
                    .taxGap(expectedTax)
                    .complianceStatus(ComplianceStatus.NON_COMPLIANT)
                    .build();
        }

        BigDecimal taxGap = expectedTax.subtract(reportedTax).setScale(4, RoundingMode.HALF_UP);
        ComplianceStatus status = determineStatus(taxGap);

        return TaxResult.builder()
                .transactionId(transaction.getTransactionId())
                .customerId(transaction.getCustomerId())
                .amount(amount)
                .taxRate(taxRate)
                .reportedTax(reportedTax)
                .expectedTax(expectedTax)
                .taxGap(taxGap)
                .complianceStatus(status)
                .build();
    }

    private ComplianceStatus determineStatus(BigDecimal taxGap) {
        if (taxGap.abs().compareTo(TOLERANCE) <= 0)
            return ComplianceStatus.COMPLIANT;
        if (taxGap.compareTo(TOLERANCE)         >  0)
            return ComplianceStatus.UNDERPAID;
        return ComplianceStatus.OVERPAID;
    }
}
