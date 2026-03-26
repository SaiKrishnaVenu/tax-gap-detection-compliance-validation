package com.taxgap.dto;

import com.taxgap.dto.response.BatchUploadResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatchUploadResponseTest {

    @Test
    void shouldBuildBatchUploadResponse() {

        BatchUploadResponse res = BatchUploadResponse.builder()
                .total(10)
                .successCount(8)
                .failureCount(2)
                .build();

        assertThat(res.getTotal()).isEqualTo(10);
        assertThat(res.getSuccessCount()).isEqualTo(8);
        assertThat(res.getFailureCount()).isEqualTo(2);
    }
}