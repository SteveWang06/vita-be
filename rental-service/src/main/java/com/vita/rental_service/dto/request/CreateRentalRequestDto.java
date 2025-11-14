package com.vita.rental_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateRentalRequestDto {
    
    @NotBlank(message = "Rental ID is required")
    private String rentalId;
    
    @Future(message = "Move-in date must be in the future")
    private LocalDate moveInDate;
    
    @Min(value = 1, message = "Lease duration must be at least 1 month")
    @Max(value = 60, message = "Lease duration must not exceed 60 months")
    private Integer leaseDurationMonths;
    
    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    private String message;
}

