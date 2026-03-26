package com.taxgap.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BatchUploadResponse {
    private int total;
    private int successCount;
    private int failureCount;
    private List<TransactionResponse> results;
}
