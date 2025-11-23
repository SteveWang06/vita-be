package com.vita.media_service.service;

import com.vita.media_service.config.FileUploadConfig;
import com.vita.media_service.domain.entity.Media;
import com.vita.media_service.domain.enums.MediaStatus;
import com.vita.media_service.domain.enums.MediaType;
import com.vita.media_service.dto.request.PresignedUrlRequest;
import com.vita.media_service.dto.response.MediaResponse;
import com.vita.media_service.dto.response.PageResponse;
import com.vita.media_service.dto.response.PresignedUrlResponse;
import com.vita.media_service.exception.*;
import com.vita.media_service.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final MediaRepository mediaRepository;
    private final FileUploadConfig fileUploadConfig;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.presigned-url-expiration}")
    private int presignedUrlExpiration;

    @Value("${aws.s3.base-url}")
    private String s3BaseUrl;

    private static final long USER_QUOTA = 1024L * 1024L * 1024L; // 1GB per user

    /**
     * Generate presigned URL for direct upload to S3
     */
    @Transactional
    public PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request, String userId) {
        log.info("Generating presigned URL for user: {} - file: {}", userId, request.getFileName());

        // 1. Validate file type
        MediaType mediaType = MediaType.fromMimeType(request.getFileType());
        if (mediaType == null) {
            throw new InvalidFileTypeException("Unsupported file type: " + request.getFileType());
        }

        validateFileType(request.getFileType(), mediaType);

        // 2. Validate file size
        validateFileSize(request.getFileSize(), mediaType);

        // 3. Check user quota
        checkUserQuota(userId, request.getFileSize());

        // 4. Generate unique file key
        String fileKey = generateFileKey(userId, request.getFileName(), mediaType);

        // 5. Create presigned URL
        Map<String, String> metadata = new HashMap<>();
        metadata.put("user-id", userId);
        metadata.put("original-filename", request.getFileName());
        metadata.put("media-type", mediaType.name());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileKey)
            .contentType(request.getFileType())
            .contentLength(request.getFileSize())
            .metadata(metadata)
            .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
            .putObjectRequest(putObjectRequest)
            .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        String uploadUrl = presignedRequest.url().toString();

        // 6. Save pending upload to database
        String fileId = UUID.randomUUID().toString();
        Media media = Media.builder()
            .id(fileId)
            .userId(userId)
            .fileName(request.getFileName())
            .fileKey(fileKey)
            .fileType(request.getFileType())
            .fileSize(request.getFileSize())
            .mediaType(mediaType)
            .status(MediaStatus.PENDING)
            .uploadUrl(uploadUrl)
            .publicUrl(s3BaseUrl + "/" + fileKey)
            .expiresAt(LocalDateTime.now().plusSeconds(presignedUrlExpiration))
            .build();

        mediaRepository.save(media);

        log.info("Generated presigned URL - fileId: {}, key: {}", fileId, fileKey);

        // 7. Return response
        return PresignedUrlResponse.builder()
            .fileId(fileId)
            .uploadUrl(uploadUrl)
            .expiresIn(presignedUrlExpiration)
            .fileKey(fileKey)
            .build();
    }

    /**
     * Confirm upload completion
     */
    @Transactional
    public MediaResponse confirmUpload(String fileId, String userId) {
        log.info("Confirming upload - fileId: {}, user: {}", fileId, userId);

        // 1. Get media record
        Media media = mediaRepository.findById(fileId)
            .orElseThrow(() -> MediaNotFoundException.withId(fileId));

        // 2. Verify ownership
        if (!media.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("Not authorized to confirm this upload");
        }

        // 3. Check if already confirmed
        if (media.getStatus() == MediaStatus.COMPLETED) {
            log.info("Upload already confirmed - fileId: {}", fileId);
            return toMediaResponse(media);
        }

        // 4. Check if expired
        if (media.getExpiresAt() != null && media.getExpiresAt().isBefore(LocalDateTime.now())) {
            media.setStatus(MediaStatus.FAILED);
            mediaRepository.save(media);
            throw new InvalidFileTypeException("Upload URL has expired");
        }

        // 5. Verify file exists in S3
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(media.getFileKey())
                .build();

            HeadObjectResponse response = s3Client.headObject(headObjectRequest);

            // 6. Update media record
            media.setStatus(MediaStatus.COMPLETED);
            media.setCompletedAt(LocalDateTime.now());
            mediaRepository.save(media);

            log.info("Upload confirmed successfully - fileId: {}", fileId);

            return toMediaResponse(media);

        } catch (NoSuchKeyException e) {
            log.error("File not found in S3: {}", media.getFileKey());
            throw new FileNotFoundInS3Exception("File not found in S3: " + media.getFileKey());
        }
    }

    /**
     * Get media by ID
     */
    public MediaResponse getMediaById(String fileId) {
        log.info("Getting media by id: {}", fileId);

        Media media = mediaRepository.findById(fileId)
            .orElseThrow(() -> MediaNotFoundException.withId(fileId));

        return toMediaResponse(media);
    }

    /**
     * Get user's media with pagination
     */
    public PageResponse<MediaResponse> getUserMedia(
            String userId, 
            MediaType mediaType, 
            int page, 
            int size) {
        
        log.info("Getting user media - user: {}, type: {}, page: {}, size: {}", 
            userId, mediaType, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<Media> mediaPage;
        if (mediaType != null) {
            mediaPage = mediaRepository.findByUserIdAndMediaType(userId, mediaType, pageable);
        } else {
            mediaPage = mediaRepository.findByUserId(userId, pageable);
        }

        return toPageResponse(mediaPage);
    }

    /**
     * Delete media
     */
    @Transactional
    public void deleteMedia(String fileId, String userId, String role) {
        log.info("Deleting media - fileId: {}, user: {}, role: {}", fileId, userId, role);

        Media media = mediaRepository.findById(fileId)
            .orElseThrow(() -> MediaNotFoundException.withId(fileId));

        // Check authorization
        if (!media.getUserId().equals(userId) && !"ADMIN".equals(role)) {
            throw new UnauthorizedAccessException("Not authorized to delete this file");
        }

        // Delete from S3 if status is COMPLETED
        if (media.getStatus() == MediaStatus.COMPLETED) {
            try {
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(media.getFileKey())
                    .build();

                s3Client.deleteObject(deleteRequest);
                log.info("Deleted file from S3: {}", media.getFileKey());
            } catch (Exception e) {
                log.error("Failed to delete file from S3: {}", media.getFileKey(), e);
            }
        }

        // Delete from database
        media.setStatus(MediaStatus.DELETED);
        mediaRepository.save(media);

        log.info("Media deleted successfully - fileId: {}", fileId);
    }

    /**
     * Get all media (admin only)
     */
    public PageResponse<MediaResponse> getAllMedia(int page, int size) {
        log.info("Getting all media - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Media> mediaPage = mediaRepository.findAll(pageable);

        return toPageResponse(mediaPage);
    }

    /**
     * Clean up expired pending uploads
     */
    @Transactional
    public void cleanupExpiredUploads() {
        log.info("Starting cleanup of expired uploads");

        var expiredMedia = mediaRepository.findExpiredPendingUploads(LocalDateTime.now());
        
        for (Media media : expiredMedia) {
            media.setStatus(MediaStatus.FAILED);
            mediaRepository.save(media);
            log.info("Marked expired upload as FAILED - fileId: {}", media.getId());
        }

        log.info("Cleanup completed - {} expired uploads marked as FAILED", expiredMedia.size());
    }

    // ========== PRIVATE HELPER METHODS ==========

    private void validateFileType(String mimeType, MediaType mediaType) {
        boolean isValid = false;

        if (mediaType == MediaType.IMAGE) {
            isValid = fileUploadConfig.getAllowedImageTypes().contains(mimeType);
        } else if (mediaType == MediaType.VIDEO) {
            isValid = fileUploadConfig.getAllowedVideoTypes().contains(mimeType);
        }

        if (!isValid) {
            throw new InvalidFileTypeException(
                "File type not allowed: " + mimeType + ". Allowed types: " +
                (mediaType == MediaType.IMAGE ? 
                    fileUploadConfig.getAllowedImageTypes() : 
                    fileUploadConfig.getAllowedVideoTypes())
            );
        }
    }

    private void validateFileSize(Long fileSize, MediaType mediaType) {
        long maxSize = mediaType == MediaType.IMAGE ? 
            fileUploadConfig.getMaxSizeImage() : 
            fileUploadConfig.getMaxSizeVideo();

        if (fileSize > maxSize) {
            throw new FileSizeExceededException(
                "File size exceeds limit of " + formatFileSize(maxSize) + 
                ". Current size: " + formatFileSize(fileSize)
            );
        }
    }

    private void checkUserQuota(String userId, Long additionalSize) {
        Long currentUsage = mediaRepository.calculateTotalFileSizeByUser(userId);
        
        if (currentUsage + additionalSize > USER_QUOTA) {
            throw new QuotaExceededException(
                "User quota exceeded. Current: " + formatFileSize(currentUsage) + 
                ", Quota: " + formatFileSize(USER_QUOTA)
            );
        }
    }

    private String generateFileKey(String userId, String fileName, MediaType mediaType) {
        String fileExtension = getFileExtension(fileName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String uniqueId = UUID.randomUUID().toString();

        return String.format(
            "uploads/%s/%s/%s/%s.%s",
            mediaType.getType(),
            userId,
            timestamp,
            uniqueId,
            fileExtension
        );
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1) : "bin";
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private MediaResponse toMediaResponse(Media media) {
        return MediaResponse.builder()
            .fileId(media.getId())
            .fileName(media.getFileName())
            .fileUrl(media.getPublicUrl())
            .thumbnailUrl(media.getThumbnailUrl())
            .fileType(media.getFileType())
            .fileSize(media.getFileSize())
            .mediaType(media.getMediaType())
            .status(media.getStatus())
            .uploadedAt(media.getCompletedAt())
            .createdAt(media.getCreatedAt())
            .build();
    }

    private PageResponse<MediaResponse> toPageResponse(Page<Media> mediaPage) {
        return PageResponse.<MediaResponse>builder()
            .content(mediaPage.getContent().stream()
                .map(this::toMediaResponse)
                .toList())
            .page(mediaPage.getNumber())
            .size(mediaPage.getSize())
            .totalElements(mediaPage.getTotalElements())
            .totalPages(mediaPage.getTotalPages())
            .last(mediaPage.isLast())
            .build();
    }
}

