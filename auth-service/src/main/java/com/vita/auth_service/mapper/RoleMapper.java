package com.vita.auth_service.mapper;

import com.vita.auth_service.dto.request.RoleRequest;
import com.vita.auth_service.dto.response.RoleResponse;
import com.vita.auth_service.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
