package com.vita.auth_service.service;


import com.vita.auth_service.constant.PredefinedRole;
import com.vita.auth_service.dto.request.UserCreationRequest;
import com.vita.auth_service.dto.request.UserUpdateRequest;
import com.vita.auth_service.dto.response.UserResponse;
import com.vita.auth_service.entity.Role;
import com.vita.auth_service.entity.User;
import com.vita.auth_service.exception.AppException;
import com.vita.auth_service.exception.ErrorCode;
import com.vita.auth_service.mapper.ProfileMapper;
import com.vita.auth_service.mapper.UserMapper;
import com.vita.auth_service.repository.RoleRepository;
import com.vita.auth_service.repository.UserRepository;
import com.vita.auth_service.repository.httpclient.UserClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    @Autowired(required = false)
    UserClient userClient;
    @Autowired(required = false)
    ProfileMapper profileMapper;
    
    @NonFinal
    boolean userServiceEnabled = true; // Default value
    
    @Value("${app.services.user-service-enabled:true}")
    public void setUserServiceEnabled(boolean userServiceEnabled) {
        this.userServiceEnabled = userServiceEnabled;
    }

    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findByName(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);

        user = userRepository.save(user);
        
        if (userServiceEnabled && userClient != null && profileMapper != null) {
            try {
                var profileRequest = profileMapper.toProfileCreationRequest(request);
                profileRequest.setUserId(user.getId());
                userClient.createUser(profileRequest);
                log.info("User profile created successfully in user-service for user: {}", user.getUsername());
            } catch (Exception e) {
                log.warn("Failed to create user profile in user-service for user: {}. Error: {}", user.getUsername(), e.getMessage());
            }
        } else {
            log.info("User service integration disabled or dependencies not available, skipping profile creation for user: {}", user.getUsername());
        }

        return userMapper.toUserResponse(user);
    }

    public UserResponse createAdminUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findByName(PredefinedRole.ADMIN_ROLE).ifPresent(roles::add);

        user.setRoles(roles);

        user = userRepository.save(user);
        
        if (userServiceEnabled && userClient != null && profileMapper != null) {
            try {
                var profileRequest = profileMapper.toProfileCreationRequest(request);
                profileRequest.setUserId(user.getId());
                userClient.createUser(profileRequest);
                log.info("Admin user profile created successfully in user-service for user: {}", user.getUsername());
            } catch (Exception e) {
                log.warn("Failed to create admin user profile in user-service for user: {}. Error: {}", user.getUsername(), e.getMessage());
            }
        } else {
            log.info("User service integration disabled or dependencies not available, skipping profile creation for admin user: {}", user.getUsername());
        }

        return userMapper.toUserResponse(user);
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        User user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);
        
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        log.info("In method get Users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(String id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }
}
