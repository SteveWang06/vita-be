package com.vita.auth_service.configuration;


import com.vita.auth_service.constant.PredefinedRole;
import com.vita.auth_service.dto.request.UserCreationRequest;
import com.vita.auth_service.entity.Role;
import com.vita.auth_service.entity.User;
import com.vita.auth_service.mapper.ProfileMapper;
import com.vita.auth_service.repository.RoleRepository;
import com.vita.auth_service.repository.UserRepository;
import com.vita.auth_service.repository.httpclient.UserClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Configuration
@EnableConfigurationProperties(AdminProperties.class)
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;
    UserClient userClient;
    ProfileMapper profileMapper;
    AdminProperties adminProperties;

    @Bean
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            String adminUsername = adminProperties.getUsername();
            String adminPassword = adminProperties.getPassword();

            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                roleRepository.save(Role.builder()
                        .name(PredefinedRole.USER_ROLE)
                        .description("User role")
                        .build());

                Role adminRole = roleRepository.save(Role.builder()
                        .name(PredefinedRole.ADMIN_ROLE)
                        .description("Admin role")
                        .build());

                var roles = new HashSet<Role>();
                roles.add(adminRole);

                User user = User.builder()
                        .username(adminUsername)
                        .password(passwordEncoder.encode(adminPassword))
                        .roles(roles)
                        .build();

                user = userRepository.save(user);

                log.warn("Admin user created with default password. Please change it immediately.");

                UserCreationRequest profileData = UserCreationRequest.builder()
                        .username(adminUsername)
                        .firstName("Admin")
                        .lastName("System")
                        .email("admin@vita.com")
                        .build();

                var clientRequest = profileMapper.toProfileCreationRequest(profileData);
                clientRequest.setUserId(user.getId());
                userClient.createUser(clientRequest);
            }

            log.info("Application initialization completed.");
        };
    }
}
