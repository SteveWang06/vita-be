package com.vita.media_service.exception;

public class FileSizeExceededException extends RuntimeException {
    
    public FileSizeExceededException(String message) {
        super(message);
    }
}

