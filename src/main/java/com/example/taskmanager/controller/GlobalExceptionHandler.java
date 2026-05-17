package com.example.taskmanager.controller;

import com.anthropic.errors.AnthropicException;
import com.anthropic.errors.AnthropicIoException;
import com.anthropic.errors.AnthropicServiceException;
import com.example.taskmanager.exception.ClaudeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ClaudeParseException.class)
    public ResponseEntity<Map<String, Object>> handleClaudeParse(ClaudeParseException ex) {
        return buildError(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    @ExceptionHandler(AnthropicServiceException.class)
    public ResponseEntity<Map<String, Object>> handleAnthropicService(AnthropicServiceException ex) {
        log.error("Anthropic API error (status {}): {}", ex.statusCode(), ex.getMessage());
        return buildError(HttpStatus.BAD_GATEWAY,
                "Anthropic API error " + ex.statusCode() + ": " + ex.getMessage());
    }

    @ExceptionHandler(AnthropicIoException.class)
    public ResponseEntity<Map<String, Object>> handleAnthropicIo(AnthropicIoException ex) {
        log.error("Anthropic network error: {}", ex.getMessage());
        return buildError(HttpStatus.BAD_GATEWAY, "Could not reach Anthropic API: " + ex.getMessage());
    }

    @ExceptionHandler(AnthropicException.class)
    public ResponseEntity<Map<String, Object>> handleAnthropic(AnthropicException ex) {
        log.error("Anthropic SDK error: {}", ex.getMessage(), ex);
        return buildError(HttpStatus.BAD_GATEWAY, "Anthropic error: " + ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildError(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}