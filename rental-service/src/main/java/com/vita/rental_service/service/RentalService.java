package com.vita.rental_service.service;

import com.vita.rental_service.domain.entity.Rental;
import com.vita.rental_service.domain.enums.RentalStatus;
import com.vita.rental_service.dto.request.CreateRentalRequest;
import com.vita.rental_service.dto.request.UpdateRentalRequest;
import com.vita.rental_service.dto.response.RentalResponse;
import com.vita.rental_service.exception.RentalNotFoundException;
import com.vita.rental_service.exception.UnauthorizedAccessException;
import com.vita.rental_service.mapper.RentalMapper;
import com.vita.rental_service.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalService {
    
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    
    @Transactional
    public RentalResponse createRental(CreateRentalRequest request, String ownerId) {
        log.info("Creating rental for owner: {}", ownerId);
        
        Rental rental = rentalMapper.toEntity(request);
        rental.setOwnerId(ownerId);
        rental.setStatus(RentalStatus.PENDING_APPROVAL); // Requires admin approval
        
        Rental savedRental = rentalRepository.save(rental);
        log.info("Rental created successfully with id: {}", savedRental.getId());
        
        return rentalMapper.toResponse(savedRental);
    }
    
    @Transactional(readOnly = true)
    public Page<RentalResponse> getAllRentals(Pageable pageable) {
        log.info("Fetching all available rentals with pagination: page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        // Only return AVAILABLE rentals (approved and available for rent)
        Page<Rental> rentals = rentalRepository.findByStatus(RentalStatus.AVAILABLE, pageable);
        return rentals.map(rentalMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public RentalResponse getRentalById(String id) {
        Rental rental = findRentalOrThrow(id);
        return rentalMapper.toResponse(rental);
    }
    
    @Transactional(readOnly = true)
    public List<RentalResponse> getRentalsByOwner(String ownerId) {
        log.info("Fetching rentals for owner: {}", ownerId);
        List<Rental> rentals = rentalRepository.findByOwnerId(ownerId);
        return rentals.stream()
                .map(rentalMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public RentalResponse updateRental(String id, UpdateRentalRequest request, String userId, String role) {
        log.info("Updating rental: {} by user: {}", id, userId);
        
        Rental rental = findRentalOrThrow(id);
        
        // Check authorization: only owner or admin can update
        checkOwnership(rental, userId, role);
        
        rentalMapper.updateEntityFromDto(request, rental);
        Rental updatedRental = rentalRepository.save(rental);
        
        log.info("Rental updated successfully: {}", id);
        return rentalMapper.toResponse(updatedRental);
    }
    
    @Transactional
    public void deleteRental(String id, String userId, String role) {
        log.info("Deleting rental: {} by user: {}", id, userId);
        
        Rental rental = findRentalOrThrow(id);
        
        // Check authorization: only owner or admin can delete
        checkOwnership(rental, userId, role);
        
        rentalRepository.delete(rental);
        log.info("Rental deleted successfully: {}", id);
    }
    
    private Rental findRentalOrThrow(String id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new RentalNotFoundException(id, "id"));
    }
    
    private void checkOwnership(Rental rental, String userId, String role) {
        boolean isAdmin = role != null && role.toUpperCase().contains("ADMIN");
        boolean isOwner = rental.getOwnerId().equals(userId);
        
        if (!isAdmin && !isOwner) {
            throw new UnauthorizedAccessException("You are not authorized to perform this action on this rental");
        }
    }
    
    // Helper method for admin service
    @Transactional
    public RentalResponse updateRentalStatus(String id, RentalStatus status) {
        Rental rental = findRentalOrThrow(id);
        rental.setStatus(status);
        Rental updatedRental = rentalRepository.save(rental);
        return rentalMapper.toResponse(updatedRental);
    }
}

