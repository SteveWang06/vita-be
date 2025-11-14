package com.vita.rental_service.exception;

public class DuplicateRequestException extends RuntimeException {
    public DuplicateRequestException(String message) {
        super(message);
    }
    
    public DuplicateRequestException() {
        super("You already have a pending request for this rental");
    }
}

