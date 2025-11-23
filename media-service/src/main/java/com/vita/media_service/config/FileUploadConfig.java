package com.vita.media_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "file.upload")
@Data
public class FileUploadConfig {
    
    private Long maxSizeImage;
    private Long maxSizeVideo;
    private List<String> allowedImageTypes;
    private List<String> allowedVideoTypes;
}

