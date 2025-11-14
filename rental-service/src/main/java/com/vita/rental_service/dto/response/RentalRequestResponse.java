package com.vita.rental_service.dto.response;

import com.vita.rental_service.domain.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalRequestResponse {
    private String id;
    private String rentalId;
    private String renterId;
    private LocalDate moveInDate;
    private Integer leaseDurationMonths;
    private String message;
    private RequestStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

