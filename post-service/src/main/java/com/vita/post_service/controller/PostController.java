package com.vita.post_service.controller;

import com.vita.post_service.dto.request.CreatePostRequest;
import com.vita.post_service.entity.Post;
import com.vita.post_service.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<Post> createPost(
            @RequestBody @Valid CreatePostRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        // Nếu bạn cần quyền (role) để quyết định ai được post, kiểm tra tại đây:
        // if (!"USER".equals(role)) throw new AccessDeniedException("You are not allowed");

        Post savedPost = postService.createPost(request, userId);
        return ResponseEntity.ok(savedPost);
    }
}
