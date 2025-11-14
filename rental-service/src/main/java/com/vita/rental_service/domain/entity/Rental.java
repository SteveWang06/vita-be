package com.vita.rental_service.domain.entity;

import com.vita.rental_service.domain.enums.PropertyType;
import com.vita.rental_service.domain.enums.RentalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Rental listing entity representing a property available for rent
 */
@Entity
@Table(name = "rentals", indexes = {
    @Index(name = "idx_owner_id", columnList = "owner_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_property_type", columnList = "property_type"),
    @Index(name = "idx_city", columnList = "city")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rental {
    
    @Id
    private String id;
    
    @Column(name = "owner_id", nullable = false)
    private String ownerId; // User ID from user-service
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", nullable = false, length = 20)
    private PropertyType propertyType;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price; // Monthly rent price
    
    // Location fields
    @Column(nullable = false, length = 100)
    private String city;
    
    @Column(nullable = false, length = 100)
    private String district;
    
    @Column(length = 255)
    private String address;
    
    // Property details
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer area; // Square meters
    
    // Photos - stored as comma-separated URLs
    @Column(columnDefinition = "TEXT")
    private String photos;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RentalStatus status;
    
    @Version
    private Long version; // For optimistic locking
    
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
            status = RentalStatus.PENDING_APPROVAL;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

