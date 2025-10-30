package com.vita.post_service.service;

import com.vita.post_service.dto.request.CreatePostRequest;
import com.vita.post_service.dto.request.UpdatePostRequest;
import com.vita.post_service.dto.response.PostResponse;
import com.vita.post_service.entity.Post;
import com.vita.post_service.entity.PostType;
import com.vita.post_service.exception.UnauthorizedException;
import com.vita.post_service.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostTypeService postTypeService;

    public PostResponse createPost(CreatePostRequest request, String userId) {
        // Resolve PostType if provided
        PostType postType = null;
        if (request.getPostTypeId() != null) {
            postType = postTypeService.getPostTypeEntityById(request.getPostTypeId());
        } else if (request.getPostTypeCode() != null) {
            postType = postTypeService.getPostTypeEntityByCode(request.getPostTypeCode());
        }

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .userId(userId)
                .postType(postType)
                .createdAt(LocalDateTime.now())
                .build();

        Post savedPost = postRepository.save(post);
        return PostResponse.fromEntity(savedPost);
    }

    public PostResponse getPost(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        return PostResponse.fromEntity(post);
    }
    
    private Post getPostEntity(String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
    }

    public PostResponse updatePost(String postId, UpdatePostRequest request, String updatedBy, String role) {
        Post post = getPostEntity(postId);
        
        // Check authorization: Only post author or admin can update
        boolean isAdmin = role != null && role.toUpperCase().contains("ADMIN");
        boolean isAuthor = post.getUserId() != null && post.getUserId().equals(updatedBy);
        
        if (!isAdmin && !isAuthor) {
            throw new UnauthorizedException("Only the post author or admin can update this post");
        }
        
        // Update basic fields if provided
        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        
        // Update PostType if provided
        if (request.getPostTypeId() != null) {
            PostType postType = postTypeService.getPostTypeEntityById(request.getPostTypeId());
            post.setPostType(postType);
        } else if (request.getPostTypeCode() != null) {
            PostType postType = postTypeService.getPostTypeEntityByCode(request.getPostTypeCode());
            post.setPostType(postType);
        }
        
        post.setUpdatedBy(updatedBy);
        post.setUpdatedAt(LocalDateTime.now());
        Post updatedPost = postRepository.save(post);
        return PostResponse.fromEntity(updatedPost);
    }

    public void deletePost(String postId, String userId, String role) {
        Post post = getPostEntity(postId);
        
        // Check authorization: Only post author or admin can delete
        boolean isAdmin = role != null && role.toUpperCase().contains("ADMIN");
        boolean isAuthor = post.getUserId() != null && post.getUserId().equals(userId);
        
        if (!isAdmin && !isAuthor) {
            throw new UnauthorizedException("Only the post author or admin can delete this post");
        }
        
        postRepository.delete(post);
    }

    public List<PostResponse> getAllPosts(String postTypeId, String postTypeCode) {
        List<Post> posts;
        
        // Filter by PostType if specified
        if (postTypeId != null) {
            posts = postRepository.findByPostType_Id(postTypeId);
        } else if (postTypeCode != null) {
            posts = postRepository.findByPostType_Code(postTypeCode.toUpperCase());
        } else {
            posts = postRepository.findAll();
        }
        
        return posts.stream()
                .map(PostResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
