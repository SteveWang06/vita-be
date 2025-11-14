package com.vita.rental_service.dto.request;

import com.vita.rental_service.domain.enums.RentalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request to update rental status (Admin only)
 */
@Data
public class UpdateRentalStatusRequest {
    
    @NotNull(message = "Status is required")
    private RentalStatus status;
    
    private String reason; // Optional reason for rejection
}

