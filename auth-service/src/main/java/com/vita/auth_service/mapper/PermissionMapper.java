package com.vita.auth_service.mapper;


import com.vita.auth_service.dto.request.PermissionRequest;
import com.vita.auth_service.dto.response.PermissionResponse;
import com.vita.auth_service.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
