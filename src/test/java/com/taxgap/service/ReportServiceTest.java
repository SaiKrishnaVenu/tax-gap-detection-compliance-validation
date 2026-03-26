package com.taxgap.service;

import com.taxgap.dto.response.CustomerTaxSummary;
import com.taxgap.dto.response.ExceptionSummaryReport;
import com.taxgap.repository.TaxExceptionRepository;
import com.taxgap.repository.TaxResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private TaxResultRepository    taxResultRepository;
    @Mock private TaxExceptionRepository exceptionRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void getCustomerTaxSummary_shouldComputeComplianceScore() {

        Object[] row = {"CUST-001",
                new BigDecimal("40000"), new BigDecimal("6800"),
                new BigDecimal("7200"), new BigDecimal("400"),
                4L, 1L};
        when(taxResultRepository.findCustomerTaxSummary()).thenReturn(Collections.singletonList(row));

        List<CustomerTaxSummary> result = reportService.getCustomerTaxSummary();

        assertThat(result).hasSize(1);
        CustomerTaxSummary summary = result.get(0);
        assertThat(summary.getCustomerId()).isEqualTo("CUST-001");
        assertThat(summary.getTotalTransactions()).isEqualTo(4L);
        assertThat(summary.getNonCompliantTransactions()).isEqualTo(1L);
        assertThat(summary.getComplianceScore()).isEqualTo(75.00);
    }

    @Test
    void getCustomerTaxSummary_shouldReturn100ScoreWhenAllCompliant() {
        Object[] row = {"CUST-002",
                new BigDecimal("10000"), new BigDecimal("1800"),
                new BigDecimal("1800"), BigDecimal.ZERO,
                2L, 0L};
        when(taxResultRepository.findCustomerTaxSummary()).thenReturn(Collections.singletonList(row));

        List<CustomerTaxSummary> result = reportService.getCustomerTaxSummary();

        assertThat(result.get(0).getComplianceScore()).isEqualTo(100.00);
    }

    @Test
    void getCustomerTaxSummary_shouldHandleZeroTransactions() {
        Object[] row = {"CUST-003",
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO,
                0L, 0L};
        when(taxResultRepository.findCustomerTaxSummary()).thenReturn(Collections.singletonList(row));

        List<CustomerTaxSummary> result = reportService.getCustomerTaxSummary();

        assertThat(result.get(0).getComplianceScore()).isEqualTo(100.0);
    }

    @Test
    void getExceptionSummary_shouldAggregateCounts() {
        when(exceptionRepository.count()).thenReturn(5L);
        when(exceptionRepository.countBySeverity()).thenReturn(List.of(
                new Object[]{"HIGH", 3L},
                new Object[]{"MEDIUM", 2L}
        ));
        when(exceptionRepository.countByCustomer()).thenReturn(List.of(
                new Object[]{"CUST-001", 4L},
                new Object[]{"CUST-002", 1L}
        ));

        ExceptionSummaryReport report = reportService.getExceptionSummary();

        assertThat(report.getTotalExceptions()).isEqualTo(5L);
        assertThat(report.getCountBySeverity()).containsEntry("HIGH", 3L);
        assertThat(report.getCountBySeverity()).containsEntry("MEDIUM", 2L);
        assertThat(report.getCountByCustomer()).containsEntry("CUST-001", 4L);
    }

    @Test
    void getExceptionSummary_shouldReturnEmptyMapsWhenNoExceptions() {
        when(exceptionRepository.count()).thenReturn(0L);
        when(exceptionRepository.countBySeverity()).thenReturn(List.of());
        when(exceptionRepository.countByCustomer()).thenReturn(List.of());

        ExceptionSummaryReport report = reportService.getExceptionSummary();

        assertThat(report.getTotalExceptions()).isZero();
        assertThat(report.getCountBySeverity()).isEmpty();
        assertThat(report.getCountByCustomer()).isEmpty();
    }

    @Test
    void getCustomerTaxSummary_shouldReturnMultipleCustomers() {
        Object[] row1 = {"CUST-001", new BigDecimal("10000"), new BigDecimal("1800"),
                new BigDecimal("1800"), BigDecimal.ZERO, 2L, 0L};
        Object[] row2 = {"CUST-002", new BigDecimal("50000"), new BigDecimal("7000"),
                new BigDecimal("9000"), new BigDecimal("2000"), 3L, 2L};

        when(taxResultRepository.findCustomerTaxSummary()).thenReturn(List.of(row1, row2));

        List<CustomerTaxSummary> result = reportService.getCustomerTaxSummary();

        assertThat(result).hasSize(2);
        assertThat(result.get(1).getComplianceScore()).isEqualTo(33.33);
    }
}
