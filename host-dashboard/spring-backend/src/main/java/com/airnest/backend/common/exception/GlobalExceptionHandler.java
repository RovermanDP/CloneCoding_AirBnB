package com.airnest.backend.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ApiErrorResponseFactory apiErrorResponseFactory;

    public GlobalExceptionHandler(ApiErrorResponseFactory apiErrorResponseFactory) {
        this.apiErrorResponseFactory = apiErrorResponseFactory;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
        ResourceNotFoundException exception,
        ServletWebRequest request
    ) {
        log.info("Resource not found: {}", exception.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "resource_not_found", exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(
        InvalidRequestException exception,
        ServletWebRequest request
    ) {
        log.info("Invalid request: {}", exception.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "invalid_request", exception.getMessage(), request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(
        UnauthorizedException exception,
        ServletWebRequest request
    ) {
        log.info("Unauthorized: {}", exception.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "unauthorized", exception.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(
        HttpMessageNotReadableException exception,
        ServletWebRequest request
    ) {
        log.info("Malformed request body: {}", exception.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "invalid_request", "Request body is missing or malformed.", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
        MethodArgumentNotValidException exception,
        ServletWebRequest request
    ) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            String message = fieldError.getDefaultMessage();
            if (message != null && !message.isBlank()) {
                details.putIfAbsent(fieldError.getField(), message);
            }
        }

        String message = details.isEmpty() ? "Request payload is invalid." : "Request payload validation failed.";
        log.info("Validation failed: {}", details);
        ApiErrorResponse payload = apiErrorResponseFactory.create(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR",
            message,
            request.getRequest().getRequestURI(),
            details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(payload);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
        Exception exception,
        ServletWebRequest request
    ) {
        log.error("Unexpected error: path={}", request.getRequest().getRequestURI(), exception);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "internal_server_error", "Unexpected server error.", request);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
        HttpStatus status,
        String code,
        String message,
        ServletWebRequest request
    ) {
        ApiErrorResponse payload = apiErrorResponseFactory.create(status, code, message, request.getRequest().getRequestURI());
        return ResponseEntity.status(status).body(payload);
    }
}
