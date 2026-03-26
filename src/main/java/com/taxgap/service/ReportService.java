package com.taxgap.service;

import com.taxgap.dto.response.CustomerTaxSummary;
import com.taxgap.dto.response.ExceptionSummaryReport;
import com.taxgap.repository.TaxExceptionRepository;
import com.taxgap.repository.TaxResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TaxResultRepository   taxResultRepository;
    private final TaxExceptionRepository exceptionRepository;

    public List<CustomerTaxSummary> getCustomerTaxSummary() {
        // Native aggregation query — efficient on real SQL DB
        List<Object[]> rows = taxResultRepository.findCustomerTaxSummary();

        return rows.stream().map(row -> {
            String customerId          = (String)  row[0];
            BigDecimal totalAmount     = toBD(row[1]);
            BigDecimal totalReported   = toBD(row[2]);
            BigDecimal totalExpected   = toBD(row[3]);
            BigDecimal totalGap        = toBD(row[4]);
            long totalTxns             = toLong(row[5]);
            long nonCompliant          = toLong(row[6]);

            double complianceScore = totalTxns == 0 ? 100.0
                    : 100.0 - ((double) nonCompliant / totalTxns * 100.0);

            return CustomerTaxSummary.builder()
                    .customerId(customerId)
                    .totalAmount(totalAmount)
                    .totalReportedTax(totalReported)
                    .totalExpectedTax(totalExpected)
                    .totalTaxGap(totalGap)
                    .totalTransactions(totalTxns)
                    .nonCompliantTransactions(nonCompliant)
                    .complianceScore(BigDecimal.valueOf(complianceScore)
                            .setScale(2, RoundingMode.HALF_UP).doubleValue())
                    .build();
        }).collect(Collectors.toList());
    }

    public ExceptionSummaryReport getExceptionSummary() {
        long total = exceptionRepository.count();

        Map<String, Long> bySeverity = new LinkedHashMap<>();
        exceptionRepository.countBySeverity()
                .forEach(row -> bySeverity.put(row[0].toString(), (Long) row[1]));

        Map<String, Long> byCustomer = new LinkedHashMap<>();
        exceptionRepository.countByCustomer()
                .forEach(row -> byCustomer.put(row[0].toString(), (Long) row[1]));

        return ExceptionSummaryReport.builder()
                .totalExceptions(total)
                .countBySeverity(bySeverity)
                .countByCustomer(byCustomer)
                .build();
    }

    private BigDecimal toBD(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal bd) return bd;
        return new BigDecimal(val.toString());
    }

    private long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Long l) return l;
        return Long.parseLong(val.toString());
    }
}
