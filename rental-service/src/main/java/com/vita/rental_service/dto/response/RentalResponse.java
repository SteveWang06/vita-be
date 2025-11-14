package com.vita.rental_service.dto.response;

import com.vita.rental_service.domain.enums.PropertyType;
import com.vita.rental_service.domain.enums.RentalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalResponse {
    private String id;
    private String ownerId;
    private String title;
    private String description;
    private PropertyType propertyType;
    private BigDecimal price;
    private String city;
    private String district;
    private String address;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer area;
    private List<String> photoUrls; // Parsed from comma-separated string
    private RentalStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

