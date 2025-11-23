package com.vita.media_service.exception;

public class FileNotFoundInS3Exception extends RuntimeException {
    
    public FileNotFoundInS3Exception(String message) {
        super(message);
    }
}

