package com.vita.rental_service.repository;

import com.vita.rental_service.domain.entity.RentalRequest;
import com.vita.rental_service.domain.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRequestRepository extends JpaRepository<RentalRequest, String> {
    
    // Find requests by renter
    List<RentalRequest> findByRenterId(String renterId);
    
    Page<RentalRequest> findByRenterId(String renterId, Pageable pageable);
    
    // Find requests for a specific rental
    List<RentalRequest> findByRentalId(String rentalId);
    
    Page<RentalRequest> findByRentalId(String rentalId, Pageable pageable);
    
    // Find requests by status
    List<RentalRequest> findByStatus(RequestStatus status);
    
    // Check if renter already has a pending request for this rental
    Optional<RentalRequest> findByRentalIdAndRenterIdAndStatus(
        String rentalId, 
        String renterId, 
        RequestStatus status
    );
    
    // Check if rental has any accepted requests
    boolean existsByRentalIdAndStatus(String rentalId, RequestStatus status);
}

