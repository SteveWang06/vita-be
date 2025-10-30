package com.vita.post_service.dto.request;

import lombok.Data;

@Data
public class UpdatePostRequest {
    private String title;
    private String content;
    
    // PostType can be updated by ID or Code (one of them)
    private String postTypeId;
    private String postTypeCode;
}

