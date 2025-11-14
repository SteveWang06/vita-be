package com.vita.rental_service.exception;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(String message) {
        super(message);
    }
    
    public static ReviewNotFoundException withId(String id) {
        return new ReviewNotFoundException(String.format("Review not found with id: %s", id));
    }
}

