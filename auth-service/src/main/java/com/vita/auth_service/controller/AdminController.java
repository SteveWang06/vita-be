package com.vita.auth_service.controller;

import com.vita.auth_service.dto.request.ApiResponse;
import com.vita.auth_service.entity.User;
import com.vita.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.Builder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/check")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminCheckResponse>> checkAdminExists() {
        log.info("Checking admin account status...");
        
        try {
            boolean adminExists = userRepository.existsByRoleName("ADMIN");
            String adminEmail = userRepository.findByRoleName("ADMIN")
                    .map(User::getEmail)
                    .orElse("No admin found");
            
            AdminCheckResponse response = AdminCheckResponse.builder()
                    .exists(adminExists)
                    .adminUser(AdminUserResponse.builder()
                            .username(adminEmail)
                            .email(adminEmail)
                            .build())
                    .message(adminExists ? "Admin account exists" : "No admin account found")
                    .build();
            
            return ResponseEntity.ok(ApiResponse.<AdminCheckResponse>builder()
                    .result(response)
                    .message("Admin check completed successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error checking admin account: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.<AdminCheckResponse>builder()
                    .message("Error checking admin account: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> listAdminUsers() {
        log.info("Listing all admin users...");
        
        try {
            List<AdminUserResponse> adminUsers = userRepository.findAll().stream()
                    .filter(user -> user.getRoles().stream()
                            .anyMatch(role -> "ADMIN".equals(role.getName())))
                    .map(user -> AdminUserResponse.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .enabled(user.isEnabled())
                            .role(user.getRoles().stream()
                                    .map(role -> role.getName())
                                    .collect(Collectors.joining(", ")))
                            .build())
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.<List<AdminUserResponse>>builder()
                    .result(adminUsers)
                    .message("Admin users retrieved successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error listing admin users: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.<List<AdminUserResponse>>builder()
                    .message("Error listing admin users: " + e.getMessage())
                    .build());
        }
    }

    @Data
    @Builder
    public static class AdminCheckResponse {
        private boolean exists;
        private String message;
        private AdminUserResponse adminUser;
    }

    @Data
    @Builder
    public static class AdminUserResponse {
        private String id;
        private String username;
        private String email;
        private String role;
        private boolean enabled;
    }
}
