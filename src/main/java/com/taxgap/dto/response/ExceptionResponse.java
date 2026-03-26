package com.taxgap.dto.response;

import com.taxgap.domain.enums.Severity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ExceptionResponse {
    private Long id;
    private String transactionId;
    private String customerId;
    private String ruleName;
    private Severity severity;
    private String message;
    private LocalDateTime createdAt;
}
