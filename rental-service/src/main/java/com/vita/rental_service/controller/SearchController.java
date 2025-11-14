package com.vita.rental_service.controller;

import com.vita.rental_service.domain.enums.PropertyType;
import com.vita.rental_service.dto.response.RentalResponse;
import com.vita.rental_service.dto.response.SearchCriteria;
import com.vita.rental_service.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Controller for search and filter operations
 */
@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class SearchController {
    
    private final SearchService searchService;
    
    @GetMapping("/search")
    public ResponseEntity<Page<RentalResponse>> searchRentals(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) PropertyType type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minBedrooms,
            @RequestParam(required = false) Integer minBathrooms,
            @RequestParam(required = false) Integer minArea,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword(keyword);
        criteria.setCity(city);
        criteria.setDistrict(district);
        criteria.setPropertyType(type);
        criteria.setMinPrice(minPrice);
        criteria.setMaxPrice(maxPrice);
        criteria.setMinBedrooms(minBedrooms);
        criteria.setMinBathrooms(minBathrooms);
        criteria.setMinArea(minArea);
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<RentalResponse> responses = searchService.searchRentals(criteria, pageable);
        return ResponseEntity.ok(responses);
    }
}

