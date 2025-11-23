package com.vita.media_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresignedUrlResponse {
    
    private String fileId;
    private String uploadUrl;
    private Integer expiresIn;
    private String fileKey;
}

