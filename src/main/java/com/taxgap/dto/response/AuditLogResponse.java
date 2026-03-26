package com.taxgap.dto.response;

import com.taxgap.domain.enums.EventType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogResponse {
    private Long id;
    private EventType eventType;
    private String transactionId;
    private String detailJson;
    private LocalDateTime createdAt;
}
