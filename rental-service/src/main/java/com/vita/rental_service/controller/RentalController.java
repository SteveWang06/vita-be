package com.vita.rental_service.controller;

import com.vita.rental_service.dto.request.CreateRentalRequest;
import com.vita.rental_service.dto.request.UpdateRentalRequest;
import com.vita.rental_service.dto.response.RentalResponse;
import com.vita.rental_service.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for rental listing operations
 */
@Slf4j
@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {
    
    private final RentalService rentalService;
    
    @GetMapping
    public ResponseEntity<Page<RentalResponse>> getAllRentals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<RentalResponse> responses = rentalService.getAllRentals(pageable);
        return ResponseEntity.ok(responses);
    }
    
    @PostMapping
    public ResponseEntity<RentalResponse> createRental(
            @RequestBody @Valid CreateRentalRequest request,
            @RequestHeader("X-User-Id") String userId
    ) {
        log.info("========================================");
        log.info("CREATE RENTAL REQUEST RECEIVED");
        log.info("UserId: {}", userId);
        log.info("Request: title={}, city={}, price={}", 
                request.getTitle(), request.getCity(), request.getPrice());
        log.info("========================================");
        
        RentalResponse response = rentalService.createRental(request, userId);
        
        log.info("Rental created successfully with ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RentalResponse> getRental(@PathVariable String id) {
        RentalResponse response = rentalService.getRentalById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RentalResponse>> getRentalsByOwner(@PathVariable String userId) {
        List<RentalResponse> responses = rentalService.getRentalsByOwner(userId);
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RentalResponse> updateRental(
            @PathVariable String id,
            @RequestBody @Valid UpdateRentalRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        RentalResponse response = rentalService.updateRental(id, request, userId, role);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRental(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        rentalService.deleteRental(id, userId, role);
        return ResponseEntity.noContent().build();
    }
}

