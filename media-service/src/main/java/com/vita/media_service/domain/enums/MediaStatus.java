package com.vita.media_service.domain.enums;

public enum MediaStatus {
    PENDING,      // Presigned URL generated, waiting for upload
    COMPLETED,    // Upload confirmed and file exists in S3
    FAILED,       // Upload failed or expired
    DELETED       // File has been deleted
}

