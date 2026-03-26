package com.taxgap.controller;

import com.taxgap.domain.enums.Severity;
import com.taxgap.dto.response.ExceptionResponse;
import com.taxgap.service.ExceptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exceptions")
@RequiredArgsConstructor
public class ExceptionController {

    private final ExceptionService exceptionService;


    @GetMapping
    public ResponseEntity<List<ExceptionResponse>> getExceptions(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) String ruleName) {
        return ResponseEntity.ok(exceptionService.filter(customerId, severity, ruleName));
    }
}
