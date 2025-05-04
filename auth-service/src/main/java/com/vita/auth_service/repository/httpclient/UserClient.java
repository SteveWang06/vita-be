package com.vita.auth_service.repository.httpclient;

import com.vita.auth_service.dto.request.UserCreationClientRequest;
import com.vita.auth_service.dto.response.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@FeignClient(name = "user-service", url = "${app.services.user}")
public interface UserClient {
    @PostMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    UserProfileResponse createUser(@RequestBody UserCreationClientRequest request);
}
