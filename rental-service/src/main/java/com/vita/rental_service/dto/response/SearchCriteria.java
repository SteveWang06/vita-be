package com.vita.rental_service.dto.response;

import com.vita.rental_service.domain.enums.PropertyType;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Search criteria for filtering rentals
 */
@Data
public class SearchCriteria {
    private String keyword;
    private String city;
    private String district;
    private PropertyType propertyType;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer minBedrooms;
    private Integer minBathrooms;
    private Integer minArea;
}

