package com.vita.media_service.exception;

public class QuotaExceededException extends RuntimeException {
    
    public QuotaExceededException(String message) {
        super(message);
    }
}

