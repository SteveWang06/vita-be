package com.vita.rental_service;

import com.vita.rental_service.domain.entity.Rental;
import com.vita.rental_service.domain.entity.RentalRequest;
import com.vita.rental_service.domain.entity.Review;
import com.vita.rental_service.domain.enums.PropertyType;
import com.vita.rental_service.domain.enums.RentalStatus;
import com.vita.rental_service.domain.enums.RequestStatus;
import com.vita.rental_service.dto.request.*;
import com.vita.rental_service.dto.response.RentalRequestResponse;
import com.vita.rental_service.dto.response.RentalResponse;
import com.vita.rental_service.dto.response.ReviewResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

/**
 * Utility class for common test helpers
 */
public class TestUtils {
    
    // Test constants
    public static final String TEST_USER_ID = "test-user-123";
    public static final String TEST_OWNER_ID = "owner-456";
    public static final String TEST_RENTAL_ID = "rental-789";
    public static final String ADMIN_ROLE = "ADMIN";
    public static final String USER_ROLE = "USER";
    
    // ==================== Entity Builders ====================
    
    public static Rental createTestRental() {
        return Rental.builder()
                .id(UUID.randomUUID().toString())
                .ownerId(TEST_OWNER_ID)
                .title("Modern Apartment in District 1")
                .description("Beautiful 2BR apartment with city view")
                .propertyType(PropertyType.APARTMENT)
                .price(new BigDecimal("12000000"))
                .city("Ho Chi Minh")
                .district("District 1")
                .address("123 Nguyen Hue Street")
                .bedrooms(2)
                .bathrooms(2)
                .area(85)
                .photos("http://example.com/photo1.jpg,http://example.com/photo2.jpg")
                .status(RentalStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    public static RentalRequest createTestRentalRequest(String rentalId, String renterId) {
        return RentalRequest.builder()
                .id(UUID.randomUUID().toString())
                .rentalId(rentalId)
                .renterId(renterId)
                .moveInDate(LocalDate.now().plusDays(30))
                .leaseDurationMonths(12)
                .message("I'm interested in renting this property")
                .status(RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    public static Review createTestReview(String rentalId, String reviewerId) {
        return Review.builder()
                .id(UUID.randomUUID().toString())
                .rentalId(rentalId)
                .reviewerId(reviewerId)
                .rating(5)
                .comment("Excellent property!")
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    // ==================== Request DTO Builders ====================
    
    public static CreateRentalRequest createValidRentalRequest() {
        CreateRentalRequest request = new CreateRentalRequest();
        request.setTitle("Test Rental Property");
        request.setDescription("A nice property for testing");
        request.setPropertyType(PropertyType.APARTMENT);
        request.setPrice(new BigDecimal("10000000"));
        request.setCity("Hanoi");
        request.setDistrict("Ba Dinh");
        request.setAddress("456 Test Street");
        request.setBedrooms(2);
        request.setBathrooms(1);
        request.setArea(70);
        request.setPhotos("http://test.com/photo1.jpg");
        return request;
    }
    
    public static UpdateRentalRequest createValidUpdateRequest() {
        UpdateRentalRequest request = new UpdateRentalRequest();
        request.setTitle("Updated Title");
        request.setPrice(new BigDecimal("15000000"));
        request.setBedrooms(3);
        return request;
    }
    
    public static CreateRentalRequestDto createValidRentalRequestDto(String rentalId) {
        CreateRentalRequestDto dto = new CreateRentalRequestDto();
        dto.setRentalId(rentalId);
        dto.setMoveInDate(LocalDate.now().plusDays(30));
        dto.setLeaseDurationMonths(12);
        dto.setMessage("Test rental request message");
        return dto;
    }
    
    public static UpdateRequestStatusRequest createStatusUpdateRequest(RequestStatus status) {
        UpdateRequestStatusRequest request = new UpdateRequestStatusRequest();
        request.setStatus(status);
        if (status == RequestStatus.REJECTED) {
            request.setRejectionReason("Not suitable at this time");
        }
        return request;
    }
    
    public static CreateReviewRequest createValidReviewRequest() {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setRating(5);
        request.setComment("Great property, highly recommended!");
        return request;
    }
    
    // ==================== Response DTO Builders ====================
    
    public static RentalResponse createTestRentalResponse() {
        return RentalResponse.builder()
                .id(TEST_RENTAL_ID)
                .ownerId(TEST_OWNER_ID)
                .title("Test Rental")
                .description("Test Description")
                .propertyType(PropertyType.APARTMENT)
                .price(new BigDecimal("10000000"))
                .city("Hanoi")
                .district("Ba Dinh")
                .bedrooms(2)
                .bathrooms(1)
                .area(70)
                .photoUrls(Arrays.asList("http://test.com/photo1.jpg"))
                .status(RentalStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    public static RentalRequestResponse createTestRequestResponse() {
        return RentalRequestResponse.builder()
                .id(UUID.randomUUID().toString())
                .rentalId(TEST_RENTAL_ID)
                .renterId(TEST_USER_ID)
                .moveInDate(LocalDate.now().plusDays(30))
                .leaseDurationMonths(12)
                .message("Test message")
                .status(RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    public static ReviewResponse createTestReviewResponse() {
        return ReviewResponse.builder()
                .id(UUID.randomUUID().toString())
                .rentalId(TEST_RENTAL_ID)
                .reviewerId(TEST_USER_ID)
                .rating(5)
                .comment("Great!")
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    // ==================== Validation Test Cases ====================
    
    public static CreateRentalRequest createInvalidRentalRequest() {
        CreateRentalRequest request = new CreateRentalRequest();
        // Missing required fields - will fail validation
        request.setTitle(""); // Invalid - blank
        request.setPrice(new BigDecimal("-1")); // Invalid - negative
        return request;
    }
}

