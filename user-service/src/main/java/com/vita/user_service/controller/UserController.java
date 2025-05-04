package com.vita.user_service.controller;

import com.vita.user_service.dto.request.UserCreationRequest;
import com.vita.user_service.dto.response.UserResponse;
import com.vita.user_service.entity.User;
import com.vita.user_service.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    private UserService userService;



    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserCreationRequest userCreationRequest) {
        UserResponse userResponse = userService.createUser(userCreationRequest);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String id, @RequestBody User updatedUser) {
        UserResponse updatedUserResponse = userService.updateUser(id, updatedUser);
        return ResponseEntity.ok(updatedUserResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
