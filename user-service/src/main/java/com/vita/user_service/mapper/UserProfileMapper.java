package com.vita.user_service.mapper;
import com.vita.user_service.dto.request.UserCreationRequest;
import com.vita.user_service.dto.response.UserResponse;
import com.vita.user_service.entity.User;

// DISABLED MapStruct - Using manual implementation instead
// @Mapper(componentModel = "spring")
public interface UserProfileMapper {

    User toUserProfile(UserCreationRequest request);

    UserResponse toUserProfileResponse(User entity);
}
