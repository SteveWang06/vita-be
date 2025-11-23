package com.vita.media_service.dto.response;

import com.vita.media_service.domain.enums.MediaStatus;
import com.vita.media_service.domain.enums.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaResponse {
    
    private String fileId;
    private String fileName;
    private String fileUrl;
    private String thumbnailUrl;
    private String fileType;
    private Long fileSize;
    private MediaType mediaType;
    private MediaStatus status;
    private LocalDateTime uploadedAt;
    private LocalDateTime createdAt;
}

