package com.vita.post_service.exception;

public class DuplicatePostTypeException extends RuntimeException {
    public DuplicatePostTypeException(String message) {
        super(message);
    }
    
    public DuplicatePostTypeException(String field, String value) {
        super(String.format("PostType with %s '%s' already exists", field, value));
    }
}

