package com.vita.user_service.mapper;

import com.vita.user_service.dto.request.UserCreationRequest;
import com.vita.user_service.dto.response.UserResponse;
import com.vita.user_service.entity.User;
import org.springframework.stereotype.Component;

@Component("userProfileMapper")
public class UserProfileMapperManual implements UserProfileMapper {

    @Override
    public User toUserProfile(UserCreationRequest request) {
        if (request == null) {
            return null;
        }

        return User.builder()
            .userId(request.getUserId())
            .username(request.getUsername())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .address(request.getAddress())
            .dob(request.getDob())
            .build();
    }

    @Override
    public UserResponse toUserProfileResponse(User entity) {
        if (entity == null) {
            return null;
        }

        return UserResponse.builder()
            .id(entity.getId())
            .username(entity.getUsername())
            .firstName(entity.getFirstName())
            .lastName(entity.getLastName())
            .email(entity.getEmail())
            .phone(entity.getPhone())
            .address(entity.getAddress())
            .dob(entity.getDob())
            .build();
    }
}

