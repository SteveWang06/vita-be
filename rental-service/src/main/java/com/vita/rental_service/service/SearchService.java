package com.vita.rental_service.service;

import com.vita.rental_service.domain.entity.Rental;
import com.vita.rental_service.dto.response.RentalResponse;
import com.vita.rental_service.dto.response.SearchCriteria;
import com.vita.rental_service.mapper.RentalMapper;
import com.vita.rental_service.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    
    @Transactional(readOnly = true)
    public Page<RentalResponse> searchRentals(SearchCriteria criteria, Pageable pageable) {
        log.info("Searching rentals with criteria: {}", criteria);
        
        Page<Rental> rentals = rentalRepository.searchRentals(
            criteria.getKeyword(),
            criteria.getCity(),
            criteria.getPropertyType(),
            criteria.getMinPrice(),
            criteria.getMaxPrice(),
            pageable
        );
        
        return rentals.map(rentalMapper::toResponse);
    }
}

