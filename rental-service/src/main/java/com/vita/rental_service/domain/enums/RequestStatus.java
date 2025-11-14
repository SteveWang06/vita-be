package com.vita.rental_service.domain.enums;

/**
 * Status of a rental request
 */
public enum RequestStatus {
    PENDING,    // Waiting for lessor response
    ACCEPTED,   // Accepted by lessor
    REJECTED,   // Rejected by lessor
    CANCELED    // Canceled by renter
}

