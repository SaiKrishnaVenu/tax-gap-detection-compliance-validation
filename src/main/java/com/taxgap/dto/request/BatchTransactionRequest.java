package com.taxgap.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchTransactionRequest {
    @NotEmpty(message = "Transaction list must not be empty")
    @Valid
    private List<TransactionRequest> transactions;
}
