package com.taxgap.controller;

import com.taxgap.dto.response.AuditLogResponse;
import com.taxgap.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAll() {

        return ResponseEntity.ok(auditService.getAll());
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<List<AuditLogResponse>> getByTransaction(@PathVariable String transactionId) {
        return ResponseEntity.ok(auditService.getByTransactionId(transactionId));
    }
}
