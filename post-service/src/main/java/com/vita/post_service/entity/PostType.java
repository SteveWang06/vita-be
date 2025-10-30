package com.vita.post_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostType {
    @Id
    private String id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private Boolean isActive;

    private String createdBy;
    private LocalDateTime createdAt;
    
    private String updatedBy;
    private LocalDateTime updatedAt;

    @PrePersist
    private void generateId() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}

