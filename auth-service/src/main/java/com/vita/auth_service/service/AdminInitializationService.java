package com.vita.auth_service.service;

import com.vita.auth_service.entity.Role;
import com.vita.auth_service.entity.User;
import com.vita.auth_service.repository.RoleRepository;
import com.vita.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminInitializationService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@vita.com}")
    private String defaultAdminEmail;

    @Value("${app.admin.password:admin123}")
    private String defaultAdminPassword;

    @Value("${app.admin.role:ADMIN}")
    private String adminRoleName;

    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("Starting admin account initialization...");

            // Check and create ADMIN role if not exists
            if (!roleRepository.existsByName(adminRoleName)) {
                log.info("Creating ADMIN role...");
                Role adminRole = Role.builder()
                        .name(adminRoleName)
                        .description("Administrator role with full access")
                        .build();
                roleRepository.save(adminRole);
                log.info("ADMIN role created successfully");
            }

            // Check and create admin user if not exists
            if (!userRepository.existsByUsername(defaultAdminEmail)) {
                log.info("Creating admin user...");
                
                Role adminRole = roleRepository.findByName(adminRoleName)
                        .orElseThrow(() -> new IllegalArgumentException("ADMIN role not found"));

                User adminUser = User.builder()
                        .username(defaultAdminEmail)
                        .email(defaultAdminEmail)
                        .password(passwordEncoder.encode(defaultAdminPassword))
                        .enabled(true)
                        .roles(Set.of(adminRole))
                        .build();

                User savedUser = userRepository.save(adminUser);
                log.info("Admin user created successfully with ID: {} and username: {}", 
                        savedUser.getId(), savedUser.getUsername());
                
                // Log thông tin đăng nhập (không log password)
                log.info("Admin account ready - Username: {}, Email: {}, Role: {}", 
                        defaultAdminEmail, defaultAdminEmail, adminRoleName);
            } else {
                log.info("Admin user already exists, skipping creation");
            }
            
            log.info("Admin account initialization completed successfully");
        } catch (Exception e) {
            log.error("Failed to initialize admin account: {}", e.getMessage(), e);
        }
    }
}
