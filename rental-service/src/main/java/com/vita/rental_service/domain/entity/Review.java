package com.vita.rental_service.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Review entity representing a review and rating for a rental property
 */
@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_rental_id", columnList = "rental_id"),
    @Index(name = "idx_reviewer_id", columnList = "reviewer_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    
    @Id
    private String id;
    
    @Column(name = "rental_id", nullable = false)
    private String rentalId; // Reference to Rental
    
    @Column(name = "reviewer_id", nullable = false)
    private String reviewerId; // User ID who wrote the review
    
    @Column(nullable = false)
    private Integer rating; // 1-5 stars
    
    @Column(columnDefinition = "TEXT")
    private String comment;
    
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
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

