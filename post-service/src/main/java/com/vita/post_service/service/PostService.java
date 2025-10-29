package com.vita.post_service.service;

import com.vita.post_service.dto.request.CreatePostRequest;
import com.vita.post_service.dto.request.UpdatePostRequest;
import com.vita.post_service.entity.Post;
import com.vita.post_service.exception.UnauthorizedException;
import com.vita.post_service.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public Post createPost(CreatePostRequest request, String userId) {
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .build();

        return postRepository.save(post);
    }

    public Post getPost(String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
    }

    public Post updatePost(String postId, UpdatePostRequest request, String updatedBy, String role) {
        Post post = getPost(postId);
        
        // Check authorization: Only post author or admin can update
        boolean isAdmin = role != null && role.toUpperCase().contains("ADMIN");
        boolean isAuthor = post.getUserId() != null && post.getUserId().equals(updatedBy);
        
        if (!isAdmin && !isAuthor) {
            throw new UnauthorizedException("Only the post author or admin can update this post");
        }
        
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setUpdatedBy(updatedBy);
        post.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    public void deletePost(String postId, String userId, String role) {
        Post post = getPost(postId);
        
        // Check authorization: Only post author or admin can delete
        boolean isAdmin = role != null && role.toUpperCase().contains("ADMIN");
        boolean isAuthor = post.getUserId() != null && post.getUserId().equals(userId);
        
        if (!isAdmin && !isAuthor) {
            throw new UnauthorizedException("Only the post author or admin can delete this post");
        }
        
        postRepository.delete(post);
    }

    public java.util.List<Post> getAllPosts() {
        return postRepository.findAll();
    }
}
