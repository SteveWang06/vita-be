package com.vita.rental_service.exception;

public class RentalNotFoundException extends RuntimeException {
    public RentalNotFoundException(String message) {
        super(message);
    }
    
    public RentalNotFoundException(String id, String field) {
        super(String.format("Rental not found with %s: %s", field, id));
    }
}

