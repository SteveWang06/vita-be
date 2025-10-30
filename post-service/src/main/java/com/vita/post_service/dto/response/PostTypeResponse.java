package com.vita.post_service.dto.response;

import com.vita.post_service.entity.PostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostTypeResponse {
    private String id;
    private String name;
    private String description;
    private String code;
    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    public static PostTypeResponse fromEntity(PostType postType) {
        return PostTypeResponse.builder()
                .id(postType.getId())
                .name(postType.getName())
                .description(postType.getDescription())
                .code(postType.getCode())
                .isActive(postType.getIsActive())
                .createdBy(postType.getCreatedBy())
                .createdAt(postType.getCreatedAt())
                .updatedBy(postType.getUpdatedBy())
                .updatedAt(postType.getUpdatedAt())
                .build();
    }
}

