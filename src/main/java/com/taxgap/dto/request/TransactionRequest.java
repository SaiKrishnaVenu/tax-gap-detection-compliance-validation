package com.taxgap.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.taxgap.domain.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequest {

    @NotBlank(message = "transactionId is required")
    private String transactionId;

    @NotNull(message = "date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotBlank(message = "customerId is required")
    private String customerId;

    private BigDecimal amount;
    private BigDecimal taxRate;
    private BigDecimal reportedTax;

    @NotNull(message = "transactionType is required")
    private TransactionType transactionType;
}
