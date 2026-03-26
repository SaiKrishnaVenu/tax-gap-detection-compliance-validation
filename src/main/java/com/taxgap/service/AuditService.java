package com.taxgap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxgap.domain.entity.AuditLog;
import com.taxgap.domain.enums.EventType;
import com.taxgap.dto.response.AuditLogResponse;
import com.taxgap.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public void log(EventType eventType, String transactionId, Map<String, Object> details) {
        try {
            String json = objectMapper.writeValueAsString(details);
            AuditLog entry = AuditLog.builder()
                    .eventType(eventType)
                    .transactionId(transactionId)
                    .detailJson(json)
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to write audit log for txn {}: {}", transactionId, e.getMessage());
        }
    }

    public List<AuditLogResponse> getAll() {
        return auditLogRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AuditLogResponse> getByTransactionId(String transactionId) {
        return auditLogRepository.findByTransactionId(transactionId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .eventType(log.getEventType())
                .transactionId(log.getTransactionId())
                .detailJson(log.getDetailJson())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
