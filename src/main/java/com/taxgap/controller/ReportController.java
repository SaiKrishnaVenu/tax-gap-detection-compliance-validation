package com.taxgap.controller;

import com.taxgap.dto.response.CustomerTaxSummary;
import com.taxgap.dto.response.ExceptionSummaryReport;
import com.taxgap.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/customer-summary")
    public ResponseEntity<List<CustomerTaxSummary>> customerSummary() {
        return ResponseEntity.ok(reportService.getCustomerTaxSummary());
    }

     @GetMapping("/exception-summary")
    public ResponseEntity<ExceptionSummaryReport> exceptionSummary() {
        return ResponseEntity.ok(reportService.getExceptionSummary());
    }
}
