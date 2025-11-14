package com.vita.rental_service.service;

import com.vita.rental_service.domain.entity.RentalRequest;
import com.vita.rental_service.domain.enums.RequestStatus;
import com.vita.rental_service.dto.request.CreateRentalRequestDto;
import com.vita.rental_service.dto.request.UpdateRequestStatusRequest;
import com.vita.rental_service.dto.response.RentalRequestResponse;
import com.vita.rental_service.exception.DuplicateRequestException;
import com.vita.rental_service.exception.RentalRequestNotFoundException;
import com.vita.rental_service.exception.UnauthorizedAccessException;
import com.vita.rental_service.mapper.RentalRequestMapper;
import com.vita.rental_service.repository.RentalRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalRequestService {
    
    private final RentalRequestRepository rentalRequestRepository;
    private final RentalRequestMapper rentalRequestMapper;
    
    @Transactional
    public RentalRequestResponse createRentalRequest(CreateRentalRequestDto request, String renterId) {
        log.info("Creating rental request for rental: {} by renter: {}", request.getRentalId(), renterId);
        
        // Check if renter already has a pending request for this rental
        rentalRequestRepository.findByRentalIdAndRenterIdAndStatus(
                request.getRentalId(), renterId, RequestStatus.PENDING
        ).ifPresent(existing -> {
            throw new DuplicateRequestException();
        });
        
        RentalRequest rentalRequest = rentalRequestMapper.toEntity(request);
        rentalRequest.setRenterId(renterId);
        rentalRequest.setStatus(RequestStatus.PENDING);
        
        RentalRequest savedRequest = rentalRequestRepository.save(rentalRequest);
        log.info("Rental request created successfully with id: {}", savedRequest.getId());
        
        return rentalRequestMapper.toResponse(savedRequest);
    }
    
    @Transactional(readOnly = true)
    public RentalRequestResponse getRentalRequestById(String id) {
        RentalRequest rentalRequest = findRequestOrThrow(id);
        return rentalRequestMapper.toResponse(rentalRequest);
    }
    
    @Transactional(readOnly = true)
    public List<RentalRequestResponse> getRequestsByRenter(String renterId) {
        log.info("Fetching requests for renter: {}", renterId);
        List<RentalRequest> requests = rentalRequestRepository.findByRenterId(renterId);
        return requests.stream()
                .map(rentalRequestMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public RentalRequestResponse updateRequestStatus(
            String requestId, 
            UpdateRequestStatusRequest statusRequest, 
            String userId,
            String role
    ) {
        log.info("Updating request status: {} by user: {}", requestId, userId);
        
        RentalRequest rentalRequest = findRequestOrThrow(requestId);
        
        // Only the renter can cancel, only admin/owner can accept/reject
        boolean isRenter = rentalRequest.getRenterId().equals(userId);
        boolean isAdmin = role != null && role.toUpperCase().contains("ADMIN");
        
        if (statusRequest.getStatus() == RequestStatus.CANCELED && !isRenter) {
            throw new UnauthorizedAccessException("Only the renter can cancel this request");
        }
        
        if ((statusRequest.getStatus() == RequestStatus.ACCEPTED || 
             statusRequest.getStatus() == RequestStatus.REJECTED) && !isAdmin) {
            throw new UnauthorizedAccessException("Only admin can accept or reject requests");
        }
        
        rentalRequest.setStatus(statusRequest.getStatus());
        if (statusRequest.getRejectionReason() != null) {
            rentalRequest.setRejectionReason(statusRequest.getRejectionReason());
        }
        
        RentalRequest updatedRequest = rentalRequestRepository.save(rentalRequest);
        log.info("Request status updated successfully: {}", requestId);
        
        return rentalRequestMapper.toResponse(updatedRequest);
    }
    
    private RentalRequest findRequestOrThrow(String id) {
        return rentalRequestRepository.findById(id)
                .orElseThrow(() -> RentalRequestNotFoundException.withId(id));
    }
}

