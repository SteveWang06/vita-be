package com.vita.rental_service.dto.request;

import com.vita.rental_service.domain.enums.PropertyType;
import com.vita.rental_service.domain.enums.RentalStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateRentalRequest {
    
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    private String description;
    
    private PropertyType propertyType;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;
    
    @Size(max = 100, message = "District must not exceed 100 characters")
    private String district;
    
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
    
    @Min(value = 0, message = "Bedrooms must be non-negative")
    private Integer bedrooms;
    
    @Min(value = 0, message = "Bathrooms must be non-negative")
    private Integer bathrooms;
    
    @Min(value = 1, message = "Area must be at least 1 square meter")
    private Integer area;
    
    private String photos;
    
    private RentalStatus status; // Allow owner to change status
}

