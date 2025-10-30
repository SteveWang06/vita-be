package com.vita.post_service.exception;

public class PostTypeNotFoundException extends RuntimeException {
    public PostTypeNotFoundException(String message) {
        super(message);
    }
    
    public PostTypeNotFoundException(String id, String field) {
        super(String.format("PostType not found with %s: %s", field, id));
    }
}

