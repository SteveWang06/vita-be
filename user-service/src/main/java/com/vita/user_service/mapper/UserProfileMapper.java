package com.vita.user_service.mapper;
import com.vita.user_service.dto.request.UserCreationRequest;
import com.vita.user_service.dto.response.UserResponse;
import com.vita.user_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface UserProfileMapper {


    @Mapping(target = "id", ignore = true) // id được generate nên bỏ qua
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "dob", target = "dob")
    User toUserProfile(UserCreationRequest request);



    @Mapping(source = "username", target = "username")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "dob", target = "dob")
    UserResponse toUserProfileResponse(User entity);
}
