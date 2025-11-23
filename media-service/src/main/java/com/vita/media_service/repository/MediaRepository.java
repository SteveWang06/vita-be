package com.vita.media_service.repository;

import com.vita.media_service.domain.entity.Media;
import com.vita.media_service.domain.enums.MediaStatus;
import com.vita.media_service.domain.enums.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, String> {

    // Find by user ID
    Page<Media> findByUserId(String userId, Pageable pageable);

    // Find by user ID and media type
    Page<Media> findByUserIdAndMediaType(String userId, MediaType mediaType, Pageable pageable);

    // Find by user ID and status
    Page<Media> findByUserIdAndStatus(String userId, MediaStatus status, Pageable pageable);

    // Find by status
    Page<Media> findByStatus(MediaStatus status, Pageable pageable);

    // Find by media type
    Page<Media> findByMediaType(MediaType mediaType, Pageable pageable);

    // Find expired pending uploads
    @Query("SELECT m FROM Media m WHERE m.status = 'PENDING' AND m.expiresAt < :now")
    List<Media> findExpiredPendingUploads(@Param("now") LocalDateTime now);

    // Calculate total file size by user
    @Query("SELECT COALESCE(SUM(m.fileSize), 0) FROM Media m WHERE m.userId = :userId AND m.status = 'COMPLETED'")
    Long calculateTotalFileSizeByUser(@Param("userId") String userId);

    // Count media by user
    long countByUserIdAndStatus(String userId, MediaStatus status);

    // Find by file key
    Optional<Media> findByFileKey(String fileKey);

    // Check if file key exists
    boolean existsByFileKey(String fileKey);
}

