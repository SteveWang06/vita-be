package com.vita.auth_service.mapper;

import com.vita.auth_service.dto.request.UserCreationRequest;
import com.vita.auth_service.dto.request.UserUpdateRequest;
import com.vita.auth_service.dto.response.UserResponse;
import com.vita.auth_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    @Mapping(target = "address", source = "address")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "email", source = "email")
    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
