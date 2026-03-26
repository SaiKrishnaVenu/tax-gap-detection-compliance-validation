package com.taxgap.service;

import com.taxgap.domain.entity.TaxException;
import com.taxgap.domain.enums.Severity;
import com.taxgap.dto.response.ExceptionResponse;
import com.taxgap.repository.TaxExceptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExceptionService {

    private final TaxExceptionRepository exceptionRepository;

    public void saveAll(List<TaxException> exceptions) {
        exceptionRepository.saveAll(exceptions);
    }

    public List<ExceptionResponse> getAll() {
        return exceptionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ExceptionResponse> getByCustomer(String customerId) {
        return exceptionRepository.findByCustomerId(customerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ExceptionResponse> getBySeverity(Severity severity) {
        return exceptionRepository.findBySeverity(severity).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ExceptionResponse> getByRuleName(String ruleName) {
        return exceptionRepository.findByRuleName(ruleName).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ExceptionResponse> filter(String customerId, Severity severity, String ruleName) {
        if (customerId != null && severity != null) {
            return exceptionRepository.findByCustomerIdAndSeverity(customerId, severity)
                    .stream().map(this::toResponse).collect(Collectors.toList());
        }
        if (customerId != null && ruleName != null) {
            return exceptionRepository.findByCustomerIdAndRuleName(customerId, ruleName)
                    .stream().map(this::toResponse).collect(Collectors.toList());
        }
        if (customerId != null) return getByCustomer(customerId);
        if (severity   != null) return getBySeverity(severity);
        if (ruleName   != null) return getByRuleName(ruleName);
        return getAll();
    }

    private ExceptionResponse toResponse(TaxException e) {
        return ExceptionResponse.builder()
                .id(e.getId())
                .transactionId(e.getTransactionId())
                .customerId(e.getCustomerId())
                .ruleName(e.getRuleName())
                .severity(e.getSeverity())
                .message(e.getMessage())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
