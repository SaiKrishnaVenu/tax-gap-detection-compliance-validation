package com.taxgap.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleRuntimeException() {
        RuntimeException ex = new RuntimeException("error");

        var response = handler.handleRuntime(ex);

        assertThat(response).isNotNull();
    }
}