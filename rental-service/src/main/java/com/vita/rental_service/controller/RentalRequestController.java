package com.vita.rental_service.controller;

import com.vita.rental_service.dto.request.CreateRentalRequestDto;
import com.vita.rental_service.dto.request.UpdateRequestStatusRequest;
import com.vita.rental_service.dto.response.RentalRequestResponse;
import com.vita.rental_service.service.RentalRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for rental request operations
 */
@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalRequestController {
    
    private final RentalRequestService rentalRequestService;
    
    @PostMapping("/{rentalId}/requests")
    public ResponseEntity<RentalRequestResponse> sendRentalRequest(
            @PathVariable String rentalId,
            @RequestBody @Valid CreateRentalRequestDto request,
            @RequestHeader("X-User-Id") String userId
    ) {
        request.setRentalId(rentalId); // Set from path variable
        RentalRequestResponse response = rentalRequestService.createRentalRequest(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/requests/{requestId}")
    public ResponseEntity<RentalRequestResponse> getRentalRequest(@PathVariable String requestId) {
        RentalRequestResponse response = rentalRequestService.getRentalRequestById(requestId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}/requests")
    public ResponseEntity<List<RentalRequestResponse>> getRequestsByUser(@PathVariable String userId) {
        List<RentalRequestResponse> responses = rentalRequestService.getRequestsByRenter(userId);
        return ResponseEntity.ok(responses);
    }
    
    @PatchMapping("/requests/{requestId}/status")
    public ResponseEntity<RentalRequestResponse> updateRequestStatus(
            @PathVariable String requestId,
            @RequestBody @Valid UpdateRequestStatusRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        RentalRequestResponse response = rentalRequestService.updateRequestStatus(
                requestId, request, userId, role
        );
        return ResponseEntity.ok(response);
    }
}

