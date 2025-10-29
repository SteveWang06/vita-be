package com.vita.post_service.controller;

import com.vita.post_service.dto.request.CreatePostRequest;
import com.vita.post_service.dto.request.UpdatePostRequest;
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
        Post savedPost = postService.createPost(request, userId);
        return ResponseEntity.ok(savedPost);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<Post> getPost(@PathVariable String postId) {
        Post post = postService.getPost(postId);
        return ResponseEntity.ok(post);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<Post> updatePost(
            @PathVariable String postId,
            @RequestBody @Valid UpdatePostRequest request,
            @RequestHeader("X-User-Id") String updatedBy,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        Post updatedPost = postService.updatePost(postId, request, updatedBy, role);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable String postId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        postService.deletePost(postId, userId, role);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<java.util.List<Post>> getAllPosts() {
        java.util.List<Post> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }
}
