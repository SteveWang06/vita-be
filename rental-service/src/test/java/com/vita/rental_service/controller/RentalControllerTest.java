package com.vita.rental_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vita.rental_service.TestUtils;
import com.vita.rental_service.domain.entity.Rental;
import com.vita.rental_service.domain.enums.RentalStatus;
import com.vita.rental_service.dto.request.CreateRentalRequest;
import com.vita.rental_service.dto.request.UpdateRentalRequest;
import com.vita.rental_service.dto.request.UpdateRentalStatusRequest;
import com.vita.rental_service.repository.RentalRepository;
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
@DisplayName("RentalController Integration Tests")
class RentalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RentalRepository rentalRepository;

    private Rental testRental;
    private CreateRentalRequest createRequest;

    @BeforeEach
    void setUp() {
        rentalRepository.deleteAll();
        
        testRental = TestUtils.createTestRental();
        testRental.setOwnerId(TestUtils.TEST_OWNER_ID);
        
        createRequest = TestUtils.createValidRentalRequest();
    }

    // ==================== CREATE RENTAL TESTS ====================

    @Test
    @DisplayName("createRental_withValidData_shouldReturnCreated")
    void createRental_withValidData_shouldReturnCreated() throws Exception {
        mockMvc.perform(post("/api/rentals")
                        .header("X-User-Id", TestUtils.TEST_OWNER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(createRequest.getTitle()))
                .andExpect(jsonPath("$.ownerId").value(TestUtils.TEST_OWNER_ID))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("createRental_withMissingUserId_shouldReturnBadRequest")
    void createRental_withMissingUserId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createRental_withInvalidData_shouldReturnBadRequest")
    void createRental_withInvalidData_shouldReturnBadRequest() throws Exception {
        CreateRentalRequest invalidRequest = new CreateRentalRequest();
        invalidRequest.setTitle(""); // Invalid - blank

        mockMvc.perform(post("/api/rentals")
                        .header("X-User-Id", TestUtils.TEST_OWNER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ==================== GET RENTAL TESTS ====================

    @Test
    @DisplayName("getRentalById_withValidId_shouldReturnRental")
    void getRentalById_withValidId_shouldReturnRental() throws Exception {
        Rental saved = rentalRepository.save(testRental);

        mockMvc.perform(get("/api/rentals/{id}", saved.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value(saved.getTitle()))
                .andExpect(jsonPath("$.ownerId").value(TestUtils.TEST_OWNER_ID));
    }

    @Test
    @DisplayName("getRentalById_withInvalidId_shouldReturnNotFound")
    void getRentalById_withInvalidId_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/rentals/{id}", "non-existent-id"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("getAllRentals_shouldReturnRentalsList")
    void getAllRentals_shouldReturnRentalsList() throws Exception {
        rentalRepository.save(testRental);
        Rental rental2 = TestUtils.createTestRental();
        rental2.setOwnerId("another-owner");
        rentalRepository.save(rental2);

        mockMvc.perform(get("/api/rentals"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("getMyRentals_withValidUserId_shouldReturnUserRentals")
    void getMyRentals_withValidUserId_shouldReturnUserRentals() throws Exception {
        rentalRepository.save(testRental);
        Rental rental2 = TestUtils.createTestRental();
        rental2.setOwnerId("another-owner");
        rentalRepository.save(rental2);

        mockMvc.perform(get("/api/rentals/my")
                        .header("X-User-Id", TestUtils.TEST_OWNER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ownerId").value(TestUtils.TEST_OWNER_ID));
    }

    // ==================== UPDATE RENTAL TESTS ====================

    @Test
    @DisplayName("updateRental_withValidData_shouldReturnUpdatedRental")
    void updateRental_withValidData_shouldReturnUpdatedRental() throws Exception {
        Rental saved = rentalRepository.save(testRental);
        UpdateRentalRequest updateRequest = TestUtils.createValidUpdateRequest();

        mockMvc.perform(put("/api/rentals/{id}", saved.getId())
                        .header("X-User-Id", TestUtils.TEST_OWNER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value(updateRequest.getTitle()));
    }

    @Test
    @DisplayName("updateRental_withUnauthorizedUser_shouldReturnForbidden")
    void updateRental_withUnauthorizedUser_shouldReturnForbidden() throws Exception {
        Rental saved = rentalRepository.save(testRental);
        UpdateRentalRequest updateRequest = TestUtils.createValidUpdateRequest();

        mockMvc.perform(put("/api/rentals/{id}", saved.getId())
                        .header("X-User-Id", "unauthorized-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("updateRental_withInvalidId_shouldReturnNotFound")
    void updateRental_withInvalidId_shouldReturnNotFound() throws Exception {
        UpdateRentalRequest updateRequest = TestUtils.createValidUpdateRequest();

        mockMvc.perform(put("/api/rentals/{id}", "non-existent-id")
                        .header("X-User-Id", TestUtils.TEST_OWNER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // ==================== UPDATE STATUS TESTS ====================

    @Test
    @DisplayName("updateRentalStatus_withValidData_shouldUpdateStatus")
    void updateRentalStatus_withValidData_shouldUpdateStatus() throws Exception {
        Rental saved = rentalRepository.save(testRental);
        UpdateRentalStatusRequest statusRequest = new UpdateRentalStatusRequest();
        statusRequest.setStatus(RentalStatus.RENTED);

        mockMvc.perform(patch("/api/rentals/{id}/status", saved.getId())
                        .header("X-User-Id", TestUtils.TEST_OWNER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RENTED"));
    }

    @Test
    @DisplayName("updateRentalStatus_withUnauthorizedUser_shouldReturnForbidden")
    void updateRentalStatus_withUnauthorizedUser_shouldReturnForbidden() throws Exception {
        Rental saved = rentalRepository.save(testRental);
        UpdateRentalStatusRequest statusRequest = new UpdateRentalStatusRequest();
        statusRequest.setStatus(RentalStatus.RENTED);

        mockMvc.perform(patch("/api/rentals/{id}/status", saved.getId())
                        .header("X-User-Id", "unauthorized-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    // ==================== DELETE RENTAL TESTS ====================

    @Test
    @DisplayName("deleteRental_withValidData_shouldReturnNoContent")
    void deleteRental_withValidData_shouldReturnNoContent() throws Exception {
        Rental saved = rentalRepository.save(testRental);

        mockMvc.perform(delete("/api/rentals/{id}", saved.getId())
                        .header("X-User-Id", TestUtils.TEST_OWNER_ID))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/rentals/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deleteRental_withUnauthorizedUser_shouldReturnForbidden")
    void deleteRental_withUnauthorizedUser_shouldReturnForbidden() throws Exception {
        Rental saved = rentalRepository.save(testRental);

        mockMvc.perform(delete("/api/rentals/{id}", saved.getId())
                        .header("X-User-Id", "unauthorized-user"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("deleteRental_withInvalidId_shouldReturnNotFound")
    void deleteRental_withInvalidId_shouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/rentals/{id}", "non-existent-id")
                        .header("X-User-Id", TestUtils.TEST_OWNER_ID))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}

