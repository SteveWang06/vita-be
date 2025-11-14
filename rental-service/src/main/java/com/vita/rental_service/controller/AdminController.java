package com.vita.rental_service.controller;

import com.vita.rental_service.domain.enums.RentalStatus;
import com.vita.rental_service.dto.request.UpdateRentalStatusRequest;
import com.vita.rental_service.dto.response.RentalResponse;
import com.vita.rental_service.exception.UnauthorizedAccessException;
import com.vita.rental_service.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for admin-only operations
 */
@Slf4j
@RestController
@RequestMapping("/rentals/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final AdminService adminService;
    
    private void checkAdminRole(String role) {
        log.info("========================================");
        log.info("ADMIN ROLE CHECK");
        log.info("Received role: '{}'", role);
        log.info("Role is null: {}", role == null);
        if (role != null) {
            log.info("Role uppercase: '{}'", role.toUpperCase());
            log.info("Contains ADMIN: {}", role.toUpperCase().contains("ADMIN"));
        }
        log.info("========================================");
        
        if (role == null || !role.toUpperCase().contains("ADMIN")) {
            log.error("ADMIN ROLE CHECK FAILED! Role: '{}'", role);
            throw new UnauthorizedAccessException("This operation requires ADMIN role");
        }
        
        log.info("ADMIN ROLE CHECK PASSED!");
    }
    
    @GetMapping
    public ResponseEntity<Page<RentalResponse>> getAllRentals(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        checkAdminRole(role);
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<RentalResponse> responses = adminService.getAllRentals(pageable);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<RentalResponse>> getRentalsByStatus(
            @PathVariable RentalStatus status,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        checkAdminRole(role);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<RentalResponse> responses = adminService.getRentalsByStatus(status, pageable);
        return ResponseEntity.ok(responses);
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<RentalResponse> updateRentalStatus(
            @PathVariable String id,
            @RequestBody @Valid UpdateRentalStatusRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        checkAdminRole(role);
        
        RentalResponse response = adminService.updateRentalStatus(id, request);
        return ResponseEntity.ok(response);
    }
}

