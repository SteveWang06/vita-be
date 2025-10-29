package com.vita.post_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePostRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String content;
}

