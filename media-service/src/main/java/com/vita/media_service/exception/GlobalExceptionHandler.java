package com.vita.media_service.exception;

import com.vita.media_service.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MediaNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaNotFoundException(MediaNotFoundException ex) {
        log.error("Media not found: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.<Void>builder()
                .code(404)
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidFileTypeException(InvalidFileTypeException ex) {
        log.error("Invalid file type: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.<Void>builder()
                .code(400)
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(FileSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileSizeExceededException(FileSizeExceededException ex) {
        log.error("File size exceeded: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.<Void>builder()
                .code(400)
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(FileNotFoundInS3Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleFileNotFoundInS3Exception(FileNotFoundInS3Exception ex) {
        log.error("File not found in S3: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.<Void>builder()
                .code(404)
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedAccessException(UnauthorizedAccessException ex) {
        log.error("Unauthorized access: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.<Void>builder()
                .code(403)
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleQuotaExceededException(QuotaExceededException ex) {
        log.error("Quota exceeded: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .body(ApiResponse.<Void>builder()
                .code(429)
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("Validation failed: {}", errors);

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.<Map<String, String>>builder()
                .code(400)
                .message("Validation failed")
                .result(errors)
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.<Void>builder()
                .code(500)
                .message("An unexpected error occurred: " + ex.getMessage())
                .build());
    }
}

