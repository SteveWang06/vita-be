package com.vita.rental_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vita.rental_service.TestUtils;
import com.vita.rental_service.domain.entity.Rental;
import com.vita.rental_service.domain.entity.Review;
import com.vita.rental_service.dto.request.CreateReviewRequest;
import com.vita.rental_service.repository.RentalRepository;
import com.vita.rental_service.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ReviewController Integration Tests")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private Rental testRental;
    private Review testReview;
    private CreateReviewRequest createReviewRequest;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        rentalRepository.deleteAll();

        testRental = TestUtils.createTestRental();
        testRental.setOwnerId(TestUtils.TEST_OWNER_ID);
        testRental = rentalRepository.save(testRental);

        testReview = TestUtils.createTestReview(testRental.getId(), TestUtils.TEST_USER_ID);
        
        createReviewRequest = TestUtils.createValidReviewRequest();
    }

    // ==================== CREATE REVIEW TESTS ====================

    @Test
    @DisplayName("createReview_withValidData_shouldReturnCreated")
    void createReview_withValidData_shouldReturnCreated() throws Exception {
        mockMvc.perform(post("/api/rentals/{rentalId}/reviews", testRental.getId())
                        .header("X-User-Id", TestUtils.TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReviewRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.rentalId").value(testRental.getId()))
                .andExpect(jsonPath("$.reviewerId").value(TestUtils.TEST_USER_ID))
                .andExpect(jsonPath("$.rating").value(createReviewRequest.getRating()))
                .andExpect(jsonPath("$.comment").value(createReviewRequest.getComment()));
    }

    @Test
    @DisplayName("createReview_withMissingUserId_shouldReturnBadRequest")
    void createReview_withMissingUserId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/rentals/{rentalId}/reviews", testRental.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReviewRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createReview_withNonExistentRental_shouldReturnNotFound")
    void createReview_withNonExistentRental_shouldReturnNotFound() throws Exception {
        mockMvc.perform(post("/api/rentals/{rentalId}/reviews", "non-existent-rental")
                        .header("X-User-Id", TestUtils.TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReviewRequest)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("createReview_withOwnerAsReviewer_shouldReturnBadRequest")
    void createReview_withOwnerAsReviewer_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/rentals/{rentalId}/reviews", testRental.getId())
                        .header("X-User-Id", TestUtils.TEST_OWNER_ID) // Owner trying to review their own property
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReviewRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createReview_withInvalidRating_shouldReturnBadRequest")
    void createReview_withInvalidRating_shouldReturnBadRequest() throws Exception {
        createReviewRequest.setRating(6); // Invalid rating > 5

        mockMvc.perform(post("/api/rentals/{rentalId}/reviews", testRental.getId())
                        .header("X-User-Id", TestUtils.TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReviewRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createReview_withZeroRating_shouldReturnBadRequest")
    void createReview_withZeroRating_shouldReturnBadRequest() throws Exception {
        createReviewRequest.setRating(0); // Invalid rating < 1

        mockMvc.perform(post("/api/rentals/{rentalId}/reviews", testRental.getId())
                        .header("X-User-Id", TestUtils.TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReviewRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ==================== GET REVIEW TESTS ====================

    @Test
    @DisplayName("getReviewsByRentalId_withValidRentalId_shouldReturnReviewsList")
    void getReviewsByRentalId_withValidRentalId_shouldReturnReviewsList() throws Exception {
        reviewRepository.save(testReview);
        
        // Add another review
        Review review2 = TestUtils.createTestReview(testRental.getId(), "another-user");
        review2.setRating(4);
        reviewRepository.save(review2);

        mockMvc.perform(get("/api/rentals/{rentalId}/reviews", testRental.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].rentalId", everyItem(equalTo(testRental.getId()))));
    }

    @Test
    @DisplayName("getReviewsByRentalId_withNoReviews_shouldReturnEmptyList")
    void getReviewsByRentalId_withNoReviews_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/rentals/{rentalId}/reviews", testRental.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("getReviewsByRentalId_withNonExistentRental_shouldReturnNotFound")
    void getReviewsByRentalId_withNonExistentRental_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/rentals/{rentalId}/reviews", "non-existent-rental"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("getReviewById_withValidId_shouldReturnReview")
    void getReviewById_withValidId_shouldReturnReview() throws Exception {
        Review saved = reviewRepository.save(testReview);

        mockMvc.perform(get("/api/rentals/reviews/{id}", saved.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.rentalId").value(testRental.getId()))
                .andExpect(jsonPath("$.reviewerId").value(TestUtils.TEST_USER_ID));
    }

    @Test
    @DisplayName("getReviewById_withInvalidId_shouldReturnNotFound")
    void getReviewById_withInvalidId_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/rentals/reviews/{id}", "non-existent-id"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // ==================== UPDATE REVIEW TESTS ====================

    @Test
    @DisplayName("updateReview_withValidData_shouldReturnUpdatedReview")
    void updateReview_withValidData_shouldReturnUpdatedReview() throws Exception {
        Review saved = reviewRepository.save(testReview);
        
        CreateReviewRequest updateRequest = new CreateReviewRequest();
        updateRequest.setRating(4);
        updateRequest.setComment("Updated comment - still good!");

        mockMvc.perform(put("/api/rentals/reviews/{id}", saved.getId())
                        .header("X-User-Id", TestUtils.TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value(updateRequest.getComment()));
    }

    @Test
    @DisplayName("updateReview_withUnauthorizedUser_shouldReturnForbidden")
    void updateReview_withUnauthorizedUser_shouldReturnForbidden() throws Exception {
        Review saved = reviewRepository.save(testReview);
        
        CreateReviewRequest updateRequest = new CreateReviewRequest();
        updateRequest.setRating(4);
        updateRequest.setComment("Trying to update someone else's review");

        mockMvc.perform(put("/api/rentals/reviews/{id}", saved.getId())
                        .header("X-User-Id", "unauthorized-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("updateReview_withInvalidId_shouldReturnNotFound")
    void updateReview_withInvalidId_shouldReturnNotFound() throws Exception {
        CreateReviewRequest updateRequest = new CreateReviewRequest();
        updateRequest.setRating(4);
        updateRequest.setComment("Update");

        mockMvc.perform(put("/api/rentals/reviews/{id}", "non-existent-id")
                        .header("X-User-Id", TestUtils.TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("updateReview_withInvalidRating_shouldReturnBadRequest")
    void updateReview_withInvalidRating_shouldReturnBadRequest() throws Exception {
        Review saved = reviewRepository.save(testReview);
        
        CreateReviewRequest updateRequest = new CreateReviewRequest();
        updateRequest.setRating(10); // Invalid rating
        updateRequest.setComment("Update");

        mockMvc.perform(put("/api/rentals/reviews/{id}", saved.getId())
                        .header("X-User-Id", TestUtils.TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ==================== DELETE REVIEW TESTS ====================

    @Test
    @DisplayName("deleteReview_withValidData_shouldReturnNoContent")
    void deleteReview_withValidData_shouldReturnNoContent() throws Exception {
        Review saved = reviewRepository.save(testReview);

        mockMvc.perform(delete("/api/rentals/reviews/{id}", saved.getId())
                        .header("X-User-Id", TestUtils.TEST_USER_ID))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/rentals/reviews/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deleteReview_withUnauthorizedUser_shouldReturnForbidden")
    void deleteReview_withUnauthorizedUser_shouldReturnForbidden() throws Exception {
        Review saved = reviewRepository.save(testReview);

        mockMvc.perform(delete("/api/rentals/reviews/{id}", saved.getId())
                        .header("X-User-Id", "unauthorized-user"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("deleteReview_withInvalidId_shouldReturnNotFound")
    void deleteReview_withInvalidId_shouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/rentals/reviews/{id}", "non-existent-id")
                        .header("X-User-Id", TestUtils.TEST_USER_ID))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // ==================== AVERAGE RATING TESTS ====================

    @Test
    @DisplayName("getAverageRating_withMultipleReviews_shouldReturnCorrectAverage")
    void getAverageRating_withMultipleReviews_shouldReturnCorrectAverage() throws Exception {
        // Add reviews with ratings 5, 4, 3 (average = 4.0)
        testReview.setRating(5);
        reviewRepository.save(testReview);
        
        Review review2 = TestUtils.createTestReview(testRental.getId(), "user-2");
        review2.setRating(4);
        reviewRepository.save(review2);
        
        Review review3 = TestUtils.createTestReview(testRental.getId(), "user-3");
        review3.setRating(3);
        reviewRepository.save(review3);

        mockMvc.perform(get("/api/rentals/{rentalId}/reviews/average", testRental.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.0));
    }

    @Test
    @DisplayName("getAverageRating_withNoReviews_shouldReturnZero")
    void getAverageRating_withNoReviews_shouldReturnZero() throws Exception {
        mockMvc.perform(get("/api/rentals/{rentalId}/reviews/average", testRental.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(0.0));
    }

    @Test
    @DisplayName("getAverageRating_withSingleReview_shouldReturnThatRating")
    void getAverageRating_withSingleReview_shouldReturnThatRating() throws Exception {
        testReview.setRating(5);
        reviewRepository.save(testReview);

        mockMvc.perform(get("/api/rentals/{rentalId}/reviews/average", testRental.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(5.0));
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("createReview_withEmptyComment_shouldStillSucceed")
    void createReview_withEmptyComment_shouldStillSucceed() throws Exception {
        createReviewRequest.setComment(""); // Empty comment should be allowed

        mockMvc.perform(post("/api/rentals/{rentalId}/reviews", testRental.getId())
                        .header("X-User-Id", TestUtils.TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReviewRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(createReviewRequest.getRating()));
    }

    @Test
    @DisplayName("createReview_withLongComment_shouldSucceed")
    void createReview_withLongComment_shouldSucceed() throws Exception {
        String longComment = "A".repeat(500); // Long comment
        createReviewRequest.setComment(longComment);

        mockMvc.perform(post("/api/rentals/{rentalId}/reviews", testRental.getId())
                        .header("X-User-Id", TestUtils.TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReviewRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comment").value(longComment));
    }
}

