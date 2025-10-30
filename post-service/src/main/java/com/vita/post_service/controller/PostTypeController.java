package com.vita.post_service.controller;

import com.vita.post_service.dto.request.CreatePostTypeRequest;
import com.vita.post_service.dto.request.UpdatePostTypeRequest;
import com.vita.post_service.dto.response.PostTypeResponse;
import com.vita.post_service.exception.AdminAuthorizationException;
import com.vita.post_service.service.PostTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/post-types")
@RequiredArgsConstructor
public class PostTypeController {

    private final PostTypeService postTypeService;

    private void checkAdminRole(String role) {
        log.info("Checking admin role. Received role: '{}'", role);
        
        if (role == null || role.trim().isEmpty()) {
            log.warn("Role is null or empty. Access denied.");
            throw new AdminAuthorizationException("Missing role information. This operation requires ADMIN role.");
        }
        
        // Check if role contains ADMIN (handles ROLE_ADMIN, ADMIN, etc.)
        if (!role.toUpperCase().contains("ADMIN")) {
            log.warn("Role '{}' does not contain ADMIN. Access denied.", role);
            throw new AdminAuthorizationException("Insufficient permissions. This operation requires ADMIN role. Current role: " + role);
        }
        
        log.info("Admin role verified successfully for role: '{}'", role);
    }

    @PostMapping
    public ResponseEntity<PostTypeResponse> createPostType(
            @RequestBody @Valid CreatePostTypeRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        checkAdminRole(role);
        PostTypeResponse response = postTypeService.createPostType(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostTypeResponse> getPostTypeById(@PathVariable String id) {
        PostTypeResponse response = postTypeService.getPostTypeById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<PostTypeResponse> getPostTypeByCode(@PathVariable String code) {
        PostTypeResponse response = postTypeService.getPostTypeByCode(code);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PostTypeResponse>> getAllPostTypes(
            @RequestParam(required = false) Boolean isActive
    ) {
        List<PostTypeResponse> responses = postTypeService.getAllPostTypes(isActive);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostTypeResponse> updatePostType(
            @PathVariable String id,
            @RequestBody @Valid UpdatePostTypeRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        checkAdminRole(role);
        PostTypeResponse response = postTypeService.updatePostType(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePostType(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        checkAdminRole(role);
        postTypeService.deletePostType(id);
        return ResponseEntity.noContent().build();
    }
}

