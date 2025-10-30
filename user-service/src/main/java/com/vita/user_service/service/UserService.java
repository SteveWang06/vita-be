package com.vita.user_service.service;

import com.vita.user_service.entity.User;
import com.vita.user_service.repository.UserRepository;
import com.vita.user_service.dto.request.UserCreationRequest;
import com.vita.user_service.dto.response.UserResponse;
import com.vita.user_service.mapper.UserProfileMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final UserProfileMapper userProfileMapper;

    @Autowired
    public UserService(UserRepository userRepository, UserProfileMapper userProfileMapper) {
        this.userRepository = userRepository;
        this.userProfileMapper = userProfileMapper;
    }

    public UserResponse createUser(UserCreationRequest userCreationRequest) {

        User user = userProfileMapper.toUserProfile(userCreationRequest);
        User savedUser = userRepository.save(user);
        return userProfileMapper.toUserProfileResponse(savedUser);
    }

    public UserResponse updateUser(String id, User updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(updatedUser.getUsername());
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            user.setEmail(updatedUser.getEmail());
            user.setPhone(updatedUser.getPhone());
            user.setAddress(updatedUser.getAddress());
            user.setDob(updatedUser.getDob());
            User savedUser = userRepository.save(user);
            return userProfileMapper.toUserProfileResponse(savedUser);
        }).orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }

    public Optional<UserResponse> getUserById(String id) {
        return userRepository.findById(id)
                .map(userProfileMapper::toUserProfileResponse);
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userProfileMapper::toUserProfileResponse)
                .toList();
    }

    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }
}
