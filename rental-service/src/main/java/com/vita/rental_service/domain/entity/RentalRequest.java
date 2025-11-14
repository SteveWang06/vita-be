package com.vita.rental_service.domain.entity;

import com.vita.rental_service.domain.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Rental request entity representing a renter's request to rent a property
 */
@Entity
@Table(name = "rental_requests", indexes = {
    @Index(name = "idx_rental_id", columnList = "rental_id"),
    @Index(name = "idx_renter_id", columnList = "renter_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalRequest {
    
    @Id
    private String id;
    
    @Column(name = "rental_id", nullable = false)
    private String rentalId; // Reference to Rental
    
    @Column(name = "renter_id", nullable = false)
    private String renterId; // User ID from user-service
    
    @Column(name = "move_in_date")
    private LocalDate moveInDate;
    
    @Column(name = "lease_duration_months")
    private Integer leaseDurationMonths;
    
    @Column(columnDefinition = "TEXT")
    private String message; // Message from renter to owner
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason; // Reason if rejected
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = RequestStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

