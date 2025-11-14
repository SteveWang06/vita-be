package com.vita.rental_service.service;

import com.vita.rental_service.domain.entity.Review;
import com.vita.rental_service.dto.request.CreateReviewRequest;
import com.vita.rental_service.dto.response.ReviewResponse;
import com.vita.rental_service.exception.DuplicateRequestException;
import com.vita.rental_service.exception.ReviewNotFoundException;
import com.vita.rental_service.exception.UnauthorizedAccessException;
import com.vita.rental_service.mapper.ReviewMapper;
import com.vita.rental_service.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    
    @Transactional
    public ReviewResponse createReview(String rentalId, CreateReviewRequest request, String reviewerId) {
        log.info("Creating review for rental: {} by reviewer: {}", rentalId, reviewerId);
        
        // Check if user already reviewed this rental
        reviewRepository.findByRentalIdAndReviewerId(rentalId, reviewerId)
                .ifPresent(existing -> {
                    throw new DuplicateRequestException("You have already reviewed this rental");
                });
        
        Review review = reviewMapper.toEntity(request);
        review.setRentalId(rentalId);
        review.setReviewerId(reviewerId);
        
        Review savedReview = reviewRepository.save(review);
        log.info("Review created successfully with id: {}", savedReview.getId());
        
        return reviewMapper.toResponse(savedReview);
    }
    
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByRental(String rentalId) {
        log.info("Fetching reviews for rental: {}", rentalId);
        List<Review> reviews = reviewRepository.findByRentalId(rentalId);
        return reviews.stream()
                .map(reviewMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Double getAverageRating(String rentalId) {
        log.info("Calculating average rating for rental: {}", rentalId);
        Double average = reviewRepository.getAverageRating(rentalId);
        return average != null ? average : 0.0;
    }
    
    @Transactional
    public void deleteReview(String reviewId, String userId, String role) {
        log.info("Deleting review: {} by user: {}", reviewId, userId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> ReviewNotFoundException.withId(reviewId));
        
        // Only the reviewer or admin can delete
        boolean isAdmin = role != null && role.toUpperCase().contains("ADMIN");
        boolean isReviewer = review.getReviewerId().equals(userId);
        
        if (!isAdmin && !isReviewer) {
            throw new UnauthorizedAccessException("You are not authorized to delete this review");
        }
        
        reviewRepository.delete(review);
        log.info("Review deleted successfully: {}", reviewId);
    }
}

