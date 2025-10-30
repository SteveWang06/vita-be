package com.vita.post_service.dto.response;

import com.vita.post_service.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private String id;
    private String userId;
    private String title;
    private String content;
    private PostTypeResponse postType;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    public static PostResponse fromEntity(Post post) {
        PostTypeResponse postTypeResponse = null;
        if (post.getPostType() != null) {
            postTypeResponse = PostTypeResponse.fromEntity(post.getPostType());
        }

        return PostResponse.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .title(post.getTitle())
                .content(post.getContent())
                .postType(postTypeResponse)
                .createdAt(post.getCreatedAt())
                .updatedBy(post.getUpdatedBy())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}

