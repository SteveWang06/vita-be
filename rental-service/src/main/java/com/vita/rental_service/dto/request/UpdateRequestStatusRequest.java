package com.vita.rental_service.dto.request;

import com.vita.rental_service.domain.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRequestStatusRequest {
    
    @NotNull(message = "Status is required")
    private RequestStatus status;
    
    private String rejectionReason; // Required if status is REJECTED
}

