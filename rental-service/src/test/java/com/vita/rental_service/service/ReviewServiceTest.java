package com.vita.rental_service.service;

import com.vita.rental_service.TestUtils;
import com.vita.rental_service.domain.entity.Rental;
import com.vita.rental_service.domain.entity.Review;
import com.vita.rental_service.dto.request.CreateReviewRequest;
import com.vita.rental_service.dto.response.ReviewResponse;
import com.vita.rental_service.exception.RentalNotFoundException;
import com.vita.rental_service.exception.ReviewNotFoundException;
import com.vita.rental_service.exception.UnauthorizedAccessException;
import com.vita.rental_service.mapper.ReviewMapper;
import com.vita.rental_service.repository.RentalRepository;
import com.vita.rental_service.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService Unit Tests")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewService reviewService;

    private Rental testRental;
    private Review testReview;
    private ReviewResponse testReviewResponse;
    private CreateReviewRequest createReviewRequest;

    @BeforeEach
    void setUp() {
        testRental = TestUtils.createTestRental();
        testReview = TestUtils.createTestReview(TestUtils.TEST_RENTAL_ID, TestUtils.TEST_USER_ID);
        testReviewResponse = TestUtils.createTestReviewResponse();
        createReviewRequest = TestUtils.createValidReviewRequest();
    }

    // ==================== CREATE REVIEW TESTS ====================

    @Test
    @DisplayName("createReview_withValidData_shouldReturnSavedReview")
    void createReview_withValidData_shouldReturnSavedReview() {
        // Arrange
        when(rentalRepository.findById(TestUtils.TEST_RENTAL_ID)).thenReturn(Optional.of(testRental));
        when(reviewMapper.toEntity(any(CreateReviewRequest.class))).thenReturn(testReview);
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(reviewMapper.toResponse(any(Review.class))).thenReturn(testReviewResponse);

        // Act
        ReviewResponse result = reviewService.createReview(
                TestUtils.TEST_RENTAL_ID, 
                createReviewRequest, 
                TestUtils.TEST_USER_ID
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getReviewerId()).isEqualTo(TestUtils.TEST_USER_ID);
        assertThat(result.getRating()).isEqualTo(5);
        
        verify(rentalRepository).findById(TestUtils.TEST_RENTAL_ID);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("createReview_withNonExistentRental_shouldThrowNotFoundException")
    void createReview_withNonExistentRental_shouldThrowNotFoundException() {
        // Arrange
        when(rentalRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reviewService.createReview(
                "invalid-rental-id", 
                createReviewRequest, 
                TestUtils.TEST_USER_ID
        ))
                .isInstanceOf(RentalNotFoundException.class);
        
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("createReview_withInvalidRating_shouldThrowException")
    void createReview_withInvalidRating_shouldThrowException() {
        // Arrange
        createReviewRequest.setRating(6); // Invalid rating > 5
        when(rentalRepository.findById(TestUtils.TEST_RENTAL_ID)).thenReturn(Optional.of(testRental));

        // Act & Assert
        assertThatThrownBy(() -> reviewService.createReview(
                TestUtils.TEST_RENTAL_ID, 
                createReviewRequest, 
                TestUtils.TEST_USER_ID
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rating must be between 1 and 5");
        
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("createReview_withOwnerAsReviewer_shouldThrowException")
    void createReview_withOwnerAsReviewer_shouldThrowException() {
        // Arrange
        when(rentalRepository.findById(TestUtils.TEST_RENTAL_ID)).thenReturn(Optional.of(testRental));

        // Act & Assert
        assertThatThrownBy(() -> reviewService.createReview(
                TestUtils.TEST_RENTAL_ID, 
                createReviewRequest, 
                TestUtils.TEST_OWNER_ID // Owner trying to review their own property
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot review your own rental");
        
        verify(reviewRepository, never()).save(any());
    }

    // ==================== GET REVIEW TESTS ====================

    @Test
    @DisplayName("getReviewsByRental_withValidRentalId_shouldReturnReviewsList")
    void getReviewsByRental_withValidRentalId_shouldReturnReviewsList() {
        // Arrange
        when(rentalRepository.findById(TestUtils.TEST_RENTAL_ID)).thenReturn(Optional.of(testRental));
        List<Review> reviews = Arrays.asList(testReview);
        when(reviewRepository.findByRentalId(TestUtils.TEST_RENTAL_ID))
                .thenReturn(reviews);
        when(reviewMapper.toResponse(any(Review.class))).thenReturn(testReviewResponse);

        // Act
        List<ReviewResponse> result = reviewService.getReviewsByRental(TestUtils.TEST_RENTAL_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        
        verify(rentalRepository).findById(TestUtils.TEST_RENTAL_ID);
        verify(reviewRepository).findByRentalId(TestUtils.TEST_RENTAL_ID);
    }

    @Test
    @DisplayName("getReviewsByRental_withNonExistentRental_shouldThrowNotFoundException")
    void getReviewsByRental_withNonExistentRental_shouldThrowNotFoundException() {
        // Arrange
        when(rentalRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reviewService.getReviewsByRental("invalid-rental-id"))
                .isInstanceOf(RentalNotFoundException.class);
        
        verify(reviewRepository, never()).findByRentalId(anyString());
    }

    // Note: getReviewById() and updateReview() are not implemented in ReviewService
    // Reviews can only be retrieved by rental ID, and cannot be updated after creation
    // This is a common pattern for review systems to maintain data integrity

    // ==================== DELETE REVIEW TESTS ====================

    @Test
    @DisplayName("deleteReview_withValidData_shouldDeleteSuccessfully")
    void deleteReview_withValidData_shouldDeleteSuccessfully() {
        // Arrange
        when(reviewRepository.findById(testReview.getId())).thenReturn(Optional.of(testReview));
        doNothing().when(reviewRepository).delete(testReview);

        // Act
        reviewService.deleteReview(testReview.getId(), TestUtils.TEST_USER_ID, null);

        // Assert
        verify(reviewRepository).findById(testReview.getId());
        verify(reviewRepository).delete(testReview);
    }

    @Test
    @DisplayName("deleteReview_withUnauthorizedUser_shouldThrowException")
    void deleteReview_withUnauthorizedUser_shouldThrowException() {
        // Arrange
        when(reviewRepository.findById(testReview.getId())).thenReturn(Optional.of(testReview));

        // Act & Assert
        assertThatThrownBy(() -> reviewService.deleteReview(
                testReview.getId(), 
                "unauthorized-user",
                null
        ))
                .isInstanceOf(UnauthorizedAccessException.class);
        
        verify(reviewRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteReview_withNonExistentReview_shouldThrowNotFoundException")
    void deleteReview_withNonExistentReview_shouldThrowNotFoundException() {
        // Arrange
        when(reviewRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reviewService.deleteReview(
                "invalid-id", 
                TestUtils.TEST_USER_ID,
                null
        ))
                .isInstanceOf(ReviewNotFoundException.class);
        
        verify(reviewRepository, never()).delete(any());
    }

    // ==================== AVERAGE RATING TESTS ====================

    @Test
    @DisplayName("getAverageRating_withReviews_shouldReturnCorrectAverage")
    void getAverageRating_withReviews_shouldReturnCorrectAverage() {
        // Arrange
        when(reviewRepository.getAverageRating(TestUtils.TEST_RENTAL_ID))
                .thenReturn(4.5);

        // Act
        Double result = reviewService.getAverageRating(TestUtils.TEST_RENTAL_ID);

        // Assert
        assertThat(result).isEqualTo(4.5);
        
        verify(reviewRepository).getAverageRating(TestUtils.TEST_RENTAL_ID);
    }

    @Test
    @DisplayName("getAverageRating_withNoReviews_shouldReturnZero")
    void getAverageRating_withNoReviews_shouldReturnZero() {
        // Arrange
        when(reviewRepository.getAverageRating(TestUtils.TEST_RENTAL_ID))
                .thenReturn(null);

        // Act
        Double result = reviewService.getAverageRating(TestUtils.TEST_RENTAL_ID);

        // Assert
        assertThat(result).isEqualTo(0.0);
        
        verify(reviewRepository).getAverageRating(TestUtils.TEST_RENTAL_ID);
    }
}

