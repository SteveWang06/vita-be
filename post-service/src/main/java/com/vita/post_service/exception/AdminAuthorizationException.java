package com.vita.post_service.exception;

public class AdminAuthorizationException extends RuntimeException {
    public AdminAuthorizationException(String message) {
        super(message);
    }
    
    public AdminAuthorizationException() {
        super("This operation requires ADMIN role");
    }
}

