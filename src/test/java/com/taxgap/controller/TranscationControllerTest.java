package com.taxgap.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxgap.dto.request.BatchTransactionRequest;
import com.taxgap.dto.request.TransactionRequest;
import com.taxgap.dto.response.BatchUploadResponse;
import com.taxgap.security.JwtAuthFilter;
import com.taxgap.security.JwtUtils;
import com.taxgap.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = TransactionController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthFilter.class}
        )
)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService service;


    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JwtUtils jwtUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();


    private BatchTransactionRequest buildRequest() {
        TransactionRequest txn = new TransactionRequest();
        txn.setTransactionId("TXN1");
        txn.setCustomerId("CUST1");

        BatchTransactionRequest req = new BatchTransactionRequest();
        req.setTransactions(List.of(txn));

        return req;
    }


    @Test
    void shouldUploadTransactionsSuccessfully() throws Exception {

        when(service.processBatch(any()))
                .thenReturn(BatchUploadResponse.builder()
                        .total(1)
                        .successCount(1)
                        .failureCount(0)
                        .build());

        mockMvc.perform(post("/transactions/upload")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void shouldHandleEmptyRequest() throws Exception {

        BatchTransactionRequest req = new BatchTransactionRequest();
        req.setTransactions(List.of());

        when(service.processBatch(any()))
                .thenReturn(BatchUploadResponse.builder()
                        .total(0)
                        .successCount(0)
                        .failureCount(0)
                        .build());

        mockMvc.perform(post("/transactions/upload")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }





    @Test
    void shouldHandleServiceException() throws Exception {

        when(service.processBatch(any()))
                .thenThrow(new RuntimeException("Something went wrong"));

        mockMvc.perform(post("/transactions/upload")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isInternalServerError());
    }
}