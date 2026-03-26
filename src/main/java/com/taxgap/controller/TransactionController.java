package com.taxgap.controller;

import com.taxgap.dto.request.BatchTransactionRequest;
import com.taxgap.dto.response.BatchUploadResponse;
import com.taxgap.dto.response.TransactionResponse;
import com.taxgap.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/batch")
    public ResponseEntity<BatchUploadResponse> uploadBatch(
            @Valid @RequestBody BatchTransactionRequest request) {
        return ResponseEntity.ok(transactionService.processBatch(request));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAll() {

        return ResponseEntity.ok(transactionService.getAll());
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable String transactionId) {
        return ResponseEntity.ok(transactionService.getById(transactionId));
    }
}
