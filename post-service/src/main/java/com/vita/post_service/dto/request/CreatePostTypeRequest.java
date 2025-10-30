package com.vita.post_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreatePostTypeRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotBlank(message = "Code is required")
    @Pattern(regexp = "^[A-Z_]+$", message = "Code must contain only uppercase letters and underscores")
    private String code;
}

