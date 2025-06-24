package com.vita.post_service.service;

import com.vita.post_service.dto.request.CreatePostRequest;
import com.vita.post_service.entity.Post;
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
}
