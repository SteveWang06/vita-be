package com.vita.media_service.controller;

import com.vita.media_service.domain.enums.MediaType;
import com.vita.media_service.dto.request.PresignedUrlRequest;
import com.vita.media_service.dto.response.ApiResponse;
import com.vita.media_service.dto.response.MediaResponse;
import com.vita.media_service.dto.response.PageResponse;
import com.vita.media_service.dto.response.PresignedUrlResponse;
import com.vita.media_service.service.MediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@Slf4j
public class MediaController {

    private final MediaService mediaService;

    /**
     * Request presigned URL for file upload
     * POST /media/upload-request
     */
    @PostMapping("/upload-request")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> requestUploadUrl(
            @Valid @RequestBody PresignedUrlRequest request,
            @RequestHeader("X-User-Id") String userId) {

        log.info("Upload request from user: {} - file: {} ({})", 
            userId, request.getFileName(), request.getFileType());

        PresignedUrlResponse response = mediaService.generatePresignedUrl(request, userId);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.<PresignedUrlResponse>builder()
                .code(1000)
                .message("Presigned URL generated successfully")
                .result(response)
                .build());
    }

    /**
     * Confirm upload completion
     * POST /media/{fileId}/confirm
     */
    @PostMapping("/{fileId}/confirm")
    public ResponseEntity<ApiResponse<MediaResponse>> confirmUpload(
            @PathVariable String fileId,
            @RequestHeader("X-User-Id") String userId) {

        log.info("Confirm upload request - fileId: {}, user: {}", fileId, userId);

        MediaResponse response = mediaService.confirmUpload(fileId, userId);

        return ResponseEntity.ok(
            ApiResponse.<MediaResponse>builder()
                .code(1000)
                .message("Upload confirmed successfully")
                .result(response)
                .build()
        );
    }

    /**
     * Get media by ID
     * GET /media/{fileId}
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<ApiResponse<MediaResponse>> getMedia(@PathVariable String fileId) {

        log.info("Get media request - fileId: {}", fileId);

        MediaResponse response = mediaService.getMediaById(fileId);

        return ResponseEntity.ok(
            ApiResponse.<MediaResponse>builder()
                .code(1000)
                .result(response)
                .build()
        );
    }

    /**
     * Get user's media with pagination
     * GET /media?type=IMAGE&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MediaResponse>>> getUserMedia(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) MediaType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Get user media - user: {}, type: {}, page: {}, size: {}", 
            userId, type, page, size);

        PageResponse<MediaResponse> response = mediaService.getUserMedia(userId, type, page, size);

        return ResponseEntity.ok(
            ApiResponse.<PageResponse<MediaResponse>>builder()
                .code(1000)
                .result(response)
                .build()
        );
    }

    /**
     * Delete media
     * DELETE /media/{fileId}
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteMedia(
            @PathVariable String fileId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        log.info("Delete media request - fileId: {}, user: {}, role: {}", fileId, userId, role);

        mediaService.deleteMedia(fileId, userId, role);

        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .code(1000)
                .message("Media deleted successfully")
                .build()
        );
    }

    /**
     * Get all media (Admin only)
     * GET /media/admin?page=0&size=20
     */
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<PageResponse<MediaResponse>>> getAllMedia(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Get all media (admin) - page: {}, size: {}", page, size);

        // Simple role check (API Gateway should handle this)
        if (!"ADMIN".equals(role)) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.<PageResponse<MediaResponse>>builder()
                    .code(403)
                    .message("This operation requires ADMIN role")
                    .build());
        }

        PageResponse<MediaResponse> response = mediaService.getAllMedia(page, size);

        return ResponseEntity.ok(
            ApiResponse.<PageResponse<MediaResponse>>builder()
                .code(1000)
                .result(response)
                .build()
        );
    }

    /**
     * Cleanup expired uploads (Admin only)
     * POST /media/admin/cleanup
     */
    @PostMapping("/admin/cleanup")
    public ResponseEntity<ApiResponse<Void>> cleanupExpiredUploads(
            @RequestHeader("X-User-Role") String role) {

        log.info("Cleanup expired uploads (admin)");

        if (!"ADMIN".equals(role)) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.<Void>builder()
                    .code(403)
                    .message("This operation requires ADMIN role")
                    .build());
        }

        mediaService.cleanupExpiredUploads();

        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .code(1000)
                .message("Cleanup completed successfully")
                .build()
        );
    }
}

