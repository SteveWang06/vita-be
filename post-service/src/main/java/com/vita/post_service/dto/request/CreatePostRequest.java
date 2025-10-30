package com.vita.post_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePostRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    // PostType can be specified by ID or Code (one of them)
    private String postTypeId;
    private String postTypeCode;
}
