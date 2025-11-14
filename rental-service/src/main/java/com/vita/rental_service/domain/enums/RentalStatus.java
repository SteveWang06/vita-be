package com.vita.rental_service.domain.enums;

/**
 * Status of a rental listing
 */
public enum RentalStatus {
    AVAILABLE,      // Available for rent
    RENTED,         // Currently rented
    INACTIVE,       // Temporarily unavailable
    PENDING_APPROVAL, // Waiting for admin approval
    REJECTED        // Rejected by admin
}

