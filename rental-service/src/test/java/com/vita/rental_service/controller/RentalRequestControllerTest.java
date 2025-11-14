package com.vita.rental_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vita.rental_service.TestUtils;
import com.vita.rental_service.domain.entity.Rental;
import com.vita.rental_service.domain.entity.RentalRequest;
import com.vita.rental_service.domain.enums.RequestStatus;
import com.vita.rental_service.dto.request.CreateRentalRequestDto;
import com.vita.rental_service.dto.request.UpdateRequestStatusRequest;
import com.vita.rental_service.repository.RentalRepository;
import com.vita.rental_service.repository.RentalRequestRepository;
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
@DisplayName("RentalRequestController Integration Tests")
class RentalRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private RentalRequestRepository requestRepository;

    private Rental testRental;
    private RentalRequest testRequest;
    private CreateRentalRequestDto createRequestDto;

    @BeforeEach
    void setUp() {
        requestRepository.deleteAll();
        rentalRepository.deleteAll();

        testRental = TestUtils.createTestRental();
        testRental.setOwnerId(TestUtils.TEST_OWNER_ID);
        testRental = rentalRepository.save(testRental);

        testRequest = TestUtils.createTestRentalRequest(testRental.getId(), TestUtils.TEST_USER_ID);
        
        createRequestDto = TestUtils.createValidRentalRequestDto(testRental.getId());
    }

    // ==================== CREATE REQUEST TESTS ====================

    @Test
    @DisplayName("createRequest_withValidData_shouldReturnCreated")
    void createRequest_withValidData_shouldReturnCreated() throws Exception {
        mockMvc.perform(post("/api/rentals/requests")
                        .header("X-User-Id", TestUtils.TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.rentalId").value(testRental.getId()))
                .andExpect(jsonPath("$.renterId").value(TestUtils.TEST_USER_ID))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("createRequest_withMissingUserId_shouldReturnBadRequest")
    void createRequest_withMissingUserId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/rentals/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createRequest_withNonExistentRental_shouldReturnNotFound")
    void createRequest_withNonExistentRental_shouldReturnNotFound() throws Exception {
        createRequestDto.setRentalId("non-existent-rental");

        mockMvc.perform(post("/api/rentals/requests")
                        .header("X-User-Id", TestUtils.TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("createRequest_withOwnerAsRenter_shouldReturnBadRequest")
    void createRequest_withOwnerAsRenter_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/rentals/requests")
                        .header("X-User-Id", TestUtils.TEST_OWNER_ID) // Owner trying to request their own rental
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createRequest_withDuplicatePendingRequest_shouldReturnConflict")
    void createRequest_withDuplicatePendingRequest_shouldReturnConflict() throws Exception {
        // Create first request
        testRequest = requestRepository.save(testRequest);

        // Try to create duplicate
        mockMvc.perform(post("/api/rentals/requests")
                        .header("X-User-Id", TestUtils.TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    // ==================== GET REQUEST TESTS ====================

    @Test
    @DisplayName("getRequestById_withValidId_shouldReturnRequest")
    void getRequestById_withValidId_shouldReturnRequest() throws Exception {
        RentalRequest saved = requestRepository.save(testRequest);

        mockMvc.perform(get("/api/rentals/requests/{id}", saved.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.rentalId").value(testRental.getId()))
                .andExpect(jsonPath("$.renterId").value(TestUtils.TEST_USER_ID));
    }

    @Test
    @DisplayName("getRequestById_withInvalidId_shouldReturnNotFound")
    void getRequestById_withInvalidId_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/rentals/requests/{id}", "non-existent-id"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("getMyRequests_withValidUserId_shouldReturnUserRequests")
    void getMyRequests_withValidUserId_shouldReturnUserRequests() throws Exception {
        requestRepository.save(testRequest);
        
        // Create another request by different user
        RentalRequest anotherRequest = TestUtils.createTestRentalRequest(testRental.getId(), "another-user");
        requestRepository.save(anotherRequest);

        mockMvc.perform(get("/api/rentals/requests/my")
                        .header("X-User-Id", TestUtils.TEST_USER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].renterId").value(TestUtils.TEST_USER_ID));
    }

    @Test
    @DisplayName("getRequestsByRentalId_withValidOwner_shouldReturnRequests")
    void getRequestsByRentalId_withValidOwner_shouldReturnRequests() throws Exception {
        requestRepository.save(testRequest);

        mockMvc.perform(get("/api/rentals/{rentalId}/requests", testRental.getId())
                        .header("X-User-Id", TestUtils.TEST_OWNER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rentalId").value(testRental.getId()));
    }

    @Test
    @DisplayName("getRequestsByRentalId_withUnauthorizedUser_shouldReturnForbidden")
    void getRequestsByRentalId_withUnauthorizedUser_shouldReturnForbidden() throws Exception {
        requestRepository.save(testRequest);

        mockMvc.perform(get("/api/rentals/{rentalId}/requests", testRental.getId())
                        .header("X-User-Id", "unauthorized-user"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    // ==================== UPDATE STATUS TESTS ====================

    @Test
    @DisplayName("updateRequestStatus_withValidData_shouldUpdateStatus")
    void updateRequestStatus_withValidData_shouldUpdateStatus() throws Exception {
        RentalRequest saved = requestRepository.save(testRequest);
        UpdateRequestStatusRequest statusUpdate = TestUtils.createStatusUpdateRequest(RequestStatus.ACCEPTED);

        mockMvc.perform(patch("/api/rentals/requests/{id}/status", saved.getId())
                        .header("X-User-Id", TestUtils.TEST_OWNER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("updateRequestStatus_withRejection_shouldIncludeReason")
    void updateRequestStatus_withRejection_shouldIncludeReason() throws Exception {
        RentalRequest saved = requestRepository.save(testRequest);
        UpdateRequestStatusRequest statusUpdate = TestUtils.createStatusUpdateRequest(RequestStatus.REJECTED);

        mockMvc.perform(patch("/api/rentals/requests/{id}/status", saved.getId())
                        .header("X-User-Id", TestUtils.TEST_OWNER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").value(statusUpdate.getRejectionReason()));
    }

    @Test
    @DisplayName("updateRequestStatus_withUnauthorizedUser_shouldReturnForbidden")
    void updateRequestStatus_withUnauthorizedUser_shouldReturnForbidden() throws Exception {
        RentalRequest saved = requestRepository.save(testRequest);
        UpdateRequestStatusRequest statusUpdate = TestUtils.createStatusUpdateRequest(RequestStatus.ACCEPTED);

        mockMvc.perform(patch("/api/rentals/requests/{id}/status", saved.getId())
                        .header("X-User-Id", "unauthorized-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("updateRequestStatus_withNonPendingRequest_shouldReturnBadRequest")
    void updateRequestStatus_withNonPendingRequest_shouldReturnBadRequest() throws Exception {
        testRequest.setStatus(RequestStatus.ACCEPTED);
        RentalRequest saved = requestRepository.save(testRequest);
        UpdateRequestStatusRequest statusUpdate = TestUtils.createStatusUpdateRequest(RequestStatus.REJECTED);

        mockMvc.perform(patch("/api/rentals/requests/{id}/status", saved.getId())
                        .header("X-User-Id", TestUtils.TEST_OWNER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ==================== CANCEL REQUEST TESTS ====================

    @Test
    @DisplayName("cancelRequest_withValidData_shouldCancelSuccessfully")
    void cancelRequest_withValidData_shouldCancelSuccessfully() throws Exception {
        RentalRequest saved = requestRepository.save(testRequest);

        mockMvc.perform(delete("/api/rentals/requests/{id}", saved.getId())
                        .header("X-User-Id", TestUtils.TEST_USER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("cancelRequest_withUnauthorizedUser_shouldReturnForbidden")
    void cancelRequest_withUnauthorizedUser_shouldReturnForbidden() throws Exception {
        RentalRequest saved = requestRepository.save(testRequest);

        mockMvc.perform(delete("/api/rentals/requests/{id}", saved.getId())
                        .header("X-User-Id", "unauthorized-user"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("cancelRequest_withNonPendingRequest_shouldReturnBadRequest")
    void cancelRequest_withNonPendingRequest_shouldReturnBadRequest() throws Exception {
        testRequest.setStatus(RequestStatus.ACCEPTED);
        RentalRequest saved = requestRepository.save(testRequest);

        mockMvc.perform(delete("/api/rentals/requests/{id}", saved.getId())
                        .header("X-User-Id", TestUtils.TEST_USER_ID))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}

