package com.vita.post_service.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdatePostTypeRequest {
    private String name;

    private String description;

    @Pattern(regexp = "^[A-Z_]+$", message = "Code must contain only uppercase letters and underscores")
    private String code;

    private Boolean isActive;
}

