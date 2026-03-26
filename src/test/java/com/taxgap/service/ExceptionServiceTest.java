package com.taxgap.service;

import com.taxgap.domain.entity.TaxException;
import com.taxgap.domain.enums.Severity;
import com.taxgap.dto.response.ExceptionResponse;
import com.taxgap.repository.TaxExceptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExceptionServiceTest {

    @Mock
    private TaxExceptionRepository exceptionRepository;

    @InjectMocks
    private ExceptionService exceptionService;

    private TaxException buildException(String txnId, String customerId, Severity severity, String ruleName) {
        return TaxException.builder()
                .id(1L)
                .transactionId(txnId)
                .customerId(customerId)
                .ruleName(ruleName)
                .severity(severity)
                .message("Test exception message")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAll_shouldReturnAllExceptions() {
        when(exceptionRepository.findAll()).thenReturn(List.of(
                buildException("TXN-001", "CUST-001", Severity.HIGH, "HIGH_VALUE_TRANSACTION"),
                buildException("TXN-002", "CUST-002", Severity.MEDIUM, "REFUND_VALIDATION")
        ));

        List<ExceptionResponse> result = exceptionService.getAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void getByCustomer_shouldFilterByCustomerId() {
        when(exceptionRepository.findByCustomerId("CUST-001")).thenReturn(
                List.of(buildException("TXN-001", "CUST-001", Severity.HIGH, "HIGH_VALUE_TRANSACTION"))
        );

        List<ExceptionResponse> result = exceptionService.getByCustomer("CUST-001");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo("CUST-001");
    }

    @Test
    void getBySeverity_shouldFilterBySeverity() {
        when(exceptionRepository.findBySeverity(Severity.HIGH)).thenReturn(
                List.of(buildException("TXN-001", "CUST-001", Severity.HIGH, "HIGH_VALUE_TRANSACTION"))
        );

        List<ExceptionResponse> result = exceptionService.getBySeverity(Severity.HIGH);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSeverity()).isEqualTo(Severity.HIGH);
    }

    @Test
    void filter_withCustomerAndSeverity_shouldCallCombinedQuery() {
        when(exceptionRepository.findByCustomerIdAndSeverity("CUST-001", Severity.HIGH)).thenReturn(
                List.of(buildException("TXN-001", "CUST-001", Severity.HIGH, "HIGH_VALUE_TRANSACTION"))
        );

        List<ExceptionResponse> result = exceptionService.filter("CUST-001", Severity.HIGH, null);

        assertThat(result).hasSize(1);
        verify(exceptionRepository).findByCustomerIdAndSeverity("CUST-001", Severity.HIGH);
    }

    @Test
    void filter_withNoParams_shouldReturnAll() {
        when(exceptionRepository.findAll()).thenReturn(List.of(
                buildException("TXN-001", "CUST-001", Severity.LOW, "GST_SLAB_VIOLATION")
        ));

        List<ExceptionResponse> result = exceptionService.filter(null, null, null);

        assertThat(result).hasSize(1);
        verify(exceptionRepository).findAll();
    }

    @Test
    void saveAll_shouldPersistExceptions() {
        List<TaxException> exceptions = List.of(
                buildException("TXN-001", "CUST-001", Severity.HIGH, "HIGH_VALUE_TRANSACTION")
        );
        exceptionService.saveAll(exceptions);
        verify(exceptionRepository).saveAll(exceptions);
    }

    @Test
    void filter_withRuleNameOnly_shouldCallByRuleName() {
        when(exceptionRepository.findByRuleName("GST_SLAB_VIOLATION")).thenReturn(
                List.of(buildException("TXN-001", "CUST-001", Severity.HIGH, "GST_SLAB_VIOLATION"))
        );

        List<ExceptionResponse> result = exceptionService.filter(null, null, "GST_SLAB_VIOLATION");

        assertThat(result).hasSize(1);
        verify(exceptionRepository).findByRuleName("GST_SLAB_VIOLATION");
    }
}
