package com.taxgap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxgap.domain.entity.TaxException;
import com.taxgap.domain.entity.TaxResult;
import com.taxgap.domain.entity.Transaction;
import com.taxgap.domain.enums.EventType;
import com.taxgap.domain.enums.ValidationStatus;
import com.taxgap.dto.request.BatchTransactionRequest;
import com.taxgap.dto.request.TransactionRequest;
import com.taxgap.dto.response.BatchUploadResponse;
import com.taxgap.dto.response.TransactionResponse;
import com.taxgap.engine.RuleEngine;
import com.taxgap.engine.TaxGapCalculator;
import com.taxgap.engine.ValidationEngine;
import com.taxgap.repository.TaxResultRepository;
import com.taxgap.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TaxResultRepository   taxResultRepository;
    private final ValidationEngine      validationEngine;
    private final TaxGapCalculator      taxGapCalculator;
    private final RuleEngine            ruleEngine;
    private final ExceptionService      exceptionService;
    private final AuditService          auditService;
    private final ObjectMapper          objectMapper;

    @Transactional
    public BatchUploadResponse processBatch(BatchTransactionRequest batch) {
        List<TransactionResponse> results = new ArrayList<>();
        int successCount = 0, failureCount = 0;

        for (TransactionRequest req : batch.getTransactions()) {
            TransactionResponse response = processOne(req);
            results.add(response);
            if (response.getValidationStatus() == ValidationStatus.SUCCESS) successCount++;
            else failureCount++;
        }

        return BatchUploadResponse.builder()
                .total(batch.getTransactions().size())
                .successCount(successCount)
                .failureCount(failureCount)
                .results(results)
                .build();
    }

    private TransactionResponse processOne(TransactionRequest req) {
        // 1. Validate
        List<String> errors = validationEngine.validate(req);

        // Check duplicate
        if (transactionRepository.existsByTransactionId(req.getTransactionId())) {
            errors.add("Duplicate transactionId: " + req.getTransactionId());
        }

        ValidationStatus status = errors.isEmpty() ? ValidationStatus.SUCCESS : ValidationStatus.FAILURE;
        String failureReasons   = errors.isEmpty() ? null : String.join("; ", errors);

        // 2. Build and save raw transaction
        Transaction transaction = buildTransaction(req, status, failureReasons);
        try {
            transaction.setRawPayload(objectMapper.writeValueAsString(req));
        } catch (Exception ignored) {}
        transactionRepository.save(transaction);

        // 3. Audit ingestion
        auditService.log(EventType.INGESTION, req.getTransactionId(), Map.of(
                "status", status,
                "errors", errors
        ));

        if (status == ValidationStatus.FAILURE) {
            return toResponse(transaction, null);
        }

        // 4. Tax gap calculation (only for valid transactions)
        TaxResult taxResult = taxGapCalculator.calculate(transaction);
        taxResultRepository.save(taxResult);

        auditService.log(EventType.TAX_COMPUTATION, req.getTransactionId(), Map.of(
                "expectedTax",      taxResult.getExpectedTax(),
                "taxGap",           taxResult.getTaxGap(),
                "complianceStatus", taxResult.getComplianceStatus()
        ));

        // 5. Rule engine
        List<TaxException> exceptions = ruleEngine.evaluate(transaction);
        if (!exceptions.isEmpty()) {
            exceptionService.saveAll(exceptions);
        }

        for (TaxException ex : exceptions) {
            auditService.log(EventType.RULE_EXECUTION, req.getTransactionId(), Map.of(
                    "ruleName", ex.getRuleName(),
                    "severity", ex.getSeverity(),
                    "message",  ex.getMessage()
            ));
        }

        return toResponse(transaction, taxResult);
    }

    public List<TransactionResponse> getAll() {
        return transactionRepository.findAll().stream()
                .map(t -> {
                    TaxResult tr = taxResultRepository.findByTransactionId(t.getTransactionId()).orElse(null);
                    return toResponse(t, tr);
                })
                .collect(Collectors.toList());
    }

    public TransactionResponse getById(String transactionId) {
        Transaction t = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        TaxResult tr = taxResultRepository.findByTransactionId(transactionId).orElse(null);
        return toResponse(t, tr);
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private Transaction buildTransaction(TransactionRequest req, ValidationStatus status, String failureReasons) {
        return Transaction.builder()
                .transactionId(req.getTransactionId())
                .date(req.getDate())
                .customerId(req.getCustomerId())
                .amount(req.getAmount())
                .taxRate(req.getTaxRate())
                .reportedTax(req.getReportedTax())
                .transactionType(req.getTransactionType())
                .validationStatus(status)
                .failureReasons(failureReasons)
                .build();
    }

    private TransactionResponse toResponse(Transaction t, TaxResult tr) {
        return TransactionResponse.builder()
                .transactionId(t.getTransactionId())
                .date(t.getDate())
                .customerId(t.getCustomerId())
                .amount(t.getAmount())
                .taxRate(t.getTaxRate())
                .reportedTax(t.getReportedTax())
                .transactionType(t.getTransactionType() != null ? t.getTransactionType().name() : null)
                .validationStatus(t.getValidationStatus())
                .failureReasons(t.getFailureReasons())
                .expectedTax(tr != null ? tr.getExpectedTax() : null)
                .taxGap(tr != null ? tr.getTaxGap() : null)
                .complianceStatus(tr != null ? tr.getComplianceStatus() : null)
                .build();
    }
}
