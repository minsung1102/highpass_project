package com.example.highpass_backend.eception;

import com.example.highpass_backend.dto.etc.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.builder()
                        .status(errorCode.getStatus().value())
                        .code(errorCode.getCode())
                        .message(exception.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .filter(errorMessage -> errorMessage != null && !errorMessage.isBlank())
                .orElse(ErrorCode.INVALID_INPUT.getMessage());

        return buildErrorResponse(ErrorCode.INVALID_INPUT, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        return buildErrorResponse(ErrorCode.INVALID_INPUT, exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        return buildErrorResponse(ErrorCode.INVALID_INPUT, resolveDataIntegrityMessage(exception));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String message = exception.getReason() == null || exception.getReason().isBlank()
                ? status.getReasonPhrase()
                : exception.getReason();

        ErrorCode errorCode = resolveResponseStatusCode(status);
        return ResponseEntity
                .status(status)
                .body(ErrorResponse.builder()
                        .status(status.value())
                        .code(errorCode.getCode())
                        .message(message)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        return buildErrorResponse(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(ErrorCode errorCode, String message) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.builder()
                        .status(errorCode.getStatus().value())
                        .code(errorCode.getCode())
                        .message(message == null || message.isBlank() ? errorCode.getMessage() : message)
                        .build());
    }

    private ErrorCode resolveResponseStatusCode(HttpStatus status) {
        return switch (status) {
            case UNAUTHORIZED -> ErrorCode.UNAUTHORIZED;
            case FORBIDDEN -> ErrorCode.FORBIDDEN;
            case NOT_FOUND -> ErrorCode.NOT_FOUND;
            case CONFLICT -> ErrorCode.DUPLICATE_RESOURCE;
            default -> ErrorCode.BUSINESS_RULE_VIOLATION;
        };
    }

    private String resolveDataIntegrityMessage(DataIntegrityViolationException exception) {
        Throwable rootCause = exception.getMostSpecificCause();
        String rawMessage = rootCause == null ? exception.getMessage() : rootCause.getMessage();
        if (rawMessage == null) {
            return "데이터 저장 중 제약 조건 오류가 발생했습니다.";
        }

        String normalized = rawMessage.toLowerCase();
        if (normalized.contains("nickname")) {
            return "이미 사용 중인 닉네임입니다.";
        }

        return "데이터 저장 중 제약 조건 오류가 발생했습니다.";
    }
}
