package com.vita.rental_service.exception;

public class RentalRequestNotFoundException extends RuntimeException {
    public RentalRequestNotFoundException(String message) {
        super(message);
    }
    
    public static RentalRequestNotFoundException withId(String id) {
        return new RentalRequestNotFoundException(String.format("Rental request not found with id: %s", id));
    }
}

