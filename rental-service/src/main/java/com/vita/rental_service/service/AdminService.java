package com.vita.rental_service.service;

import com.vita.rental_service.domain.enums.RentalStatus;
import com.vita.rental_service.dto.request.UpdateRentalStatusRequest;
import com.vita.rental_service.dto.response.RentalResponse;
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
public class AdminService {
    
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    private final RentalService rentalService;
    
    @Transactional(readOnly = true)
    public Page<RentalResponse> getAllRentals(Pageable pageable) {
        log.info("Admin fetching all rentals");
        return rentalRepository.findAll(pageable)
                .map(rentalMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<RentalResponse> getRentalsByStatus(RentalStatus status, Pageable pageable) {
        log.info("Admin fetching rentals with status: {}", status);
        return rentalRepository.findByStatus(status, pageable)
                .map(rentalMapper::toResponse);
    }
    
    @Transactional
    public RentalResponse updateRentalStatus(String rentalId, UpdateRentalStatusRequest request) {
        log.info("Admin updating rental status: {} to {}", rentalId, request.getStatus());
        return rentalService.updateRentalStatus(rentalId, request.getStatus());
    }
}

