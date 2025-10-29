package com.vita.auth_service.mapper;

import com.vita.auth_service.dto.request.UserCreationClientRequest;
import com.vita.auth_service.dto.request.UserCreationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    UserCreationClientRequest toProfileCreationRequest(UserCreationRequest request);
}
