package com.taxgap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxgap.domain.entity.Transaction;
import com.taxgap.domain.entity.TaxResult;
import com.taxgap.domain.enums.ComplianceStatus;
import com.taxgap.domain.enums.TransactionType;
import com.taxgap.domain.enums.ValidationStatus;
import com.taxgap.dto.request.BatchTransactionRequest;
import com.taxgap.dto.request.TransactionRequest;
import com.taxgap.dto.response.BatchUploadResponse;
import com.taxgap.engine.RuleEngine;
import com.taxgap.engine.TaxGapCalculator;
import com.taxgap.engine.ValidationEngine;
import com.taxgap.repository.TaxResultRepository;
import com.taxgap.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TaxResultRepository taxResultRepository;
    @Mock
    private ValidationEngine validationEngine;
    @Mock
    private TaxGapCalculator taxGapCalculator;
    @Mock
    private RuleEngine ruleEngine;
    @Mock
    private ExceptionService exceptionService;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() throws Exception {
        var field = TransactionService.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(transactionService, new ObjectMapper().findAndRegisterModules());
    }

    private TransactionRequest validRequest() {
        TransactionRequest req = new TransactionRequest();
        req.setTransactionId("TXN-001");
        req.setDate(LocalDate.now());
        req.setCustomerId("CUST-001");
        req.setAmount(new BigDecimal("10000"));
        req.setTaxRate(new BigDecimal("0.18"));
        req.setReportedTax(new BigDecimal("1800"));
        req.setTransactionType(TransactionType.SALE);
        return req;
    }

    private TaxResult mockTaxResult(String txnId) {
        return TaxResult.builder()
                .transactionId(txnId)
                .customerId("CUST-001")
                .amount(new BigDecimal("10000"))
                .taxRate(new BigDecimal("0.18"))
                .reportedTax(new BigDecimal("1800"))
                .expectedTax(new BigDecimal("1800.0000"))
                .taxGap(new BigDecimal("0.0000"))
                .complianceStatus(ComplianceStatus.COMPLIANT)
                .build();
    }

    @Test
    void processBatch_shouldReturnSuccessForValidTransaction() {
        when(validationEngine.validate(any())).thenReturn(new ArrayList<>());
        when(transactionRepository.existsByTransactionId("TXN-001")).thenReturn(false);
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(taxGapCalculator.calculate(any())).thenReturn(mockTaxResult("TXN-001"));
        when(taxResultRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(ruleEngine.evaluate(any())).thenReturn(new ArrayList<>());
        doNothing().when(auditService).log(any(), any(), any());

        BatchTransactionRequest batch = new BatchTransactionRequest();
        batch.setTransactions(List.of(validRequest()));

        BatchUploadResponse response = transactionService.processBatch(batch);

        assertThat(response.getTotal()).isEqualTo(1);
        assertThat(response.getSuccessCount()).isEqualTo(1);
        assertThat(response.getFailureCount()).isEqualTo(0);
        assertThat(response.getResults().get(0).getValidationStatus()).isEqualTo(ValidationStatus.SUCCESS);
    }

    @Test
    void processBatch_shouldReturnFailureForInvalidTransaction() {
        when(validationEngine.validate(any()))
                .thenReturn(new ArrayList<>(List.of("amount must be greater than 0")));
        when(transactionRepository.existsByTransactionId(any())).thenReturn(false);
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doNothing().when(auditService).log(any(), any(), any());

        TransactionRequest req = validRequest();
        req.setAmount(BigDecimal.ZERO);

        BatchTransactionRequest batch = new BatchTransactionRequest();
        batch.setTransactions(List.of(req));

        BatchUploadResponse response = transactionService.processBatch(batch);

        assertThat(response.getFailureCount()).isEqualTo(1);
        assertThat(response.getSuccessCount()).isEqualTo(0);
        assertThat(response.getResults().get(0).getValidationStatus()).isEqualTo(ValidationStatus.FAILURE);
        assertThat(response.getResults().get(0).getFailureReasons())
                .contains("amount must be greater than 0");
    }

    @Test
    void processBatch_shouldRejectDuplicateTransactionId() {
        when(validationEngine.validate(any())).thenReturn(new ArrayList<>());
        when(transactionRepository.existsByTransactionId("TXN-001")).thenReturn(true);
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doNothing().when(auditService).log(any(), any(), any());

        BatchTransactionRequest batch = new BatchTransactionRequest();
        batch.setTransactions(List.of(validRequest()));

        BatchUploadResponse response = transactionService.processBatch(batch);

        assertThat(response.getFailureCount()).isEqualTo(1);
        assertThat(response.getResults().get(0).getFailureReasons()).contains("Duplicate");
    }

    @Test
    void processBatch_shouldHandleMixedResults() {
        TransactionRequest good = validRequest();
        TransactionRequest bad = validRequest();
        bad.setTransactionId("TXN-002");

        when(validationEngine.validate(good)).thenReturn(new ArrayList<>());
        when(validationEngine.validate(bad))
                .thenReturn(new ArrayList<>(List.of("amount is required")));
        when(transactionRepository.existsByTransactionId(any())).thenReturn(false);
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(taxGapCalculator.calculate(any())).thenReturn(mockTaxResult("TXN-001"));
        when(taxResultRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(ruleEngine.evaluate(any())).thenReturn(new ArrayList<>());
        doNothing().when(auditService).log(any(), any(), any());

        BatchTransactionRequest batch = new BatchTransactionRequest();
        batch.setTransactions(List.of(good, bad));

        BatchUploadResponse response = transactionService.processBatch(batch);

        assertThat(response.getTotal()).isEqualTo(2);
        assertThat(response.getSuccessCount()).isEqualTo(1);
        assertThat(response.getFailureCount()).isEqualTo(1);
    }

    @Test
    void getById_shouldReturnTransactionWithTaxResult() {
        Transaction t = Transaction.builder()
                .transactionId("TXN-001")
                .customerId("CUST-001")
                .amount(new BigDecimal("10000"))
                .taxRate(new BigDecimal("0.18"))
                .reportedTax(new BigDecimal("1800"))
                .transactionType(TransactionType.SALE)
                .validationStatus(ValidationStatus.SUCCESS)
                .build();

        when(transactionRepository.findByTransactionId("TXN-001"))
                .thenReturn(Optional.of(t));
        when(taxResultRepository.findByTransactionId("TXN-001"))
                .thenReturn(Optional.of(mockTaxResult("TXN-001")));

        var response = transactionService.getById("TXN-001");

        assertThat(response.getTransactionId()).isEqualTo("TXN-001");
        assertThat(response.getComplianceStatus()).isEqualTo(ComplianceStatus.COMPLIANT);
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(transactionRepository.findByTransactionId("MISSING"))
                .thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> transactionService.getById("MISSING"));
    }
}