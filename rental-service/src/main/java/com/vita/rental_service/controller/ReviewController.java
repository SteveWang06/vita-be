package com.vita.rental_service.controller;

import com.vita.rental_service.dto.request.CreateReviewRequest;
import com.vita.rental_service.dto.response.ReviewResponse;
import com.vita.rental_service.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for review and rating operations
 */
@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @PostMapping("/{rentalId}/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable String rentalId,
            @RequestBody @Valid CreateReviewRequest request,
            @RequestHeader("X-User-Id") String userId
    ) {
        ReviewResponse response = reviewService.createReview(rentalId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{rentalId}/reviews")
    public ResponseEntity<List<ReviewResponse>> getReviews(@PathVariable String rentalId) {
        List<ReviewResponse> responses = reviewService.getReviewsByRental(rentalId);
        return ResponseEntity.ok(responses);
    }
    
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable String reviewId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        reviewService.deleteReview(reviewId, userId, role);
        return ResponseEntity.noContent().build();
    }
}

