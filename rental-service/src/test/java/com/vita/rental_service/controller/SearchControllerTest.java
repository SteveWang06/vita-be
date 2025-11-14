package com.vita.rental_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vita.rental_service.TestUtils;
import com.vita.rental_service.domain.entity.Rental;
import com.vita.rental_service.domain.enums.PropertyType;
import com.vita.rental_service.domain.enums.RentalStatus;
import com.vita.rental_service.repository.RentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("SearchController Integration Tests")
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RentalRepository rentalRepository;

    private Rental hanoiApartment;
    private Rental hcmHouse;
    private Rental hanoiVilla;

    @BeforeEach
    void setUp() {
        rentalRepository.deleteAll();

        // Hanoi Apartment - 10M
        hanoiApartment = TestUtils.createTestRental();
        hanoiApartment.setCity("Hanoi");
        hanoiApartment.setDistrict("Ba Dinh");
        hanoiApartment.setPropertyType(PropertyType.APARTMENT);
        hanoiApartment.setPrice(new BigDecimal("10000000"));
        hanoiApartment.setBedrooms(2);
        hanoiApartment.setStatus(RentalStatus.AVAILABLE);
        rentalRepository.save(hanoiApartment);

        // HCM House - 20M
        hcmHouse = TestUtils.createTestRental();
        hcmHouse.setCity("Ho Chi Minh");
        hcmHouse.setDistrict("District 1");
        hcmHouse.setPropertyType(PropertyType.HOUSE);
        hcmHouse.setPrice(new BigDecimal("20000000"));
        hcmHouse.setBedrooms(3);
        hcmHouse.setStatus(RentalStatus.AVAILABLE);
        rentalRepository.save(hcmHouse);

        // Hanoi Villa - 30M
        hanoiVilla = TestUtils.createTestRental();
        hanoiVilla.setCity("Hanoi");
        hanoiVilla.setDistrict("Cau Giay");
        hanoiVilla.setPropertyType(PropertyType.VILLA);
        hanoiVilla.setPrice(new BigDecimal("30000000"));
        hanoiVilla.setBedrooms(4);
        hanoiVilla.setStatus(RentalStatus.AVAILABLE);
        rentalRepository.save(hanoiVilla);
    }

    // ==================== SEARCH BY CITY TESTS ====================

    @Test
    @DisplayName("searchRentals_byCity_shouldReturnMatchingRentals")
    void searchRentals_byCity_shouldReturnMatchingRentals() throws Exception {
        mockMvc.perform(get("/api/rentals/search")
                        .param("city", "Hanoi"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].city", everyItem(equalTo("Hanoi"))));
    }

    @Test
    @DisplayName("searchRentals_byCityAndDistrict_shouldReturnMatchingRentals")
    void searchRentals_byCityAndDistrict_shouldReturnMatchingRentals() throws Exception {
        mockMvc.perform(get("/api/rentals/search")
                        .param("city", "Hanoi")
                        .param("district", "Ba Dinh"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].city").value("Hanoi"))
                .andExpect(jsonPath("$[0].district").value("Ba Dinh"));
    }

    // ==================== SEARCH BY PROPERTY TYPE TESTS ====================

    @Test
    @DisplayName("searchRentals_byPropertyType_shouldReturnMatchingRentals")
    void searchRentals_byPropertyType_shouldReturnMatchingRentals() throws Exception {
        mockMvc.perform(get("/api/rentals/search")
                        .param("propertyType", "APARTMENT"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].propertyType").value("APARTMENT"));
    }

    // ==================== SEARCH BY PRICE RANGE TESTS ====================

    @Test
    @DisplayName("searchRentals_byPriceRange_shouldReturnMatchingRentals")
    void searchRentals_byPriceRange_shouldReturnMatchingRentals() throws Exception {
        mockMvc.perform(get("/api/rentals/search")
                        .param("minPrice", "15000000")
                        .param("maxPrice", "25000000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].price").value(20000000));
    }

    @Test
    @DisplayName("searchRentals_byMinPriceOnly_shouldReturnMatchingRentals")
    void searchRentals_byMinPriceOnly_shouldReturnMatchingRentals() throws Exception {
        mockMvc.perform(get("/api/rentals/search")
                        .param("minPrice", "20000000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2))); // 20M and 30M
    }

    @Test
    @DisplayName("searchRentals_byMaxPriceOnly_shouldReturnMatchingRentals")
    void searchRentals_byMaxPriceOnly_shouldReturnMatchingRentals() throws Exception {
        mockMvc.perform(get("/api/rentals/search")
                        .param("maxPrice", "20000000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2))); // 10M and 20M
    }

    // ==================== SEARCH BY BEDROOMS TESTS ====================

    @Test
    @DisplayName("searchRentals_byMinBedrooms_shouldReturnMatchingRentals")
    void searchRentals_byMinBedrooms_shouldReturnMatchingRentals() throws Exception {
        mockMvc.perform(get("/api/rentals/search")
                        .param("minBedrooms", "3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2))); // 3BR and 4BR
    }

    @Test
    @DisplayName("searchRentals_byMaxBedrooms_shouldReturnMatchingRentals")
    void searchRentals_byMaxBedrooms_shouldReturnMatchingRentals() throws Exception {
        mockMvc.perform(get("/api/rentals/search")
                        .param("maxBedrooms", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1))); // 2BR only
    }

    // ==================== COMBINED SEARCH TESTS ====================

    @Test
    @DisplayName("searchRentals_withMultipleCriteria_shouldReturnMatchingRentals")
    void searchRentals_withMultipleCriteria_shouldReturnMatchingRentals() throws Exception {
        mockMvc.perform(get("/api/rentals/search")
                        .param("city", "Hanoi")
                        .param("propertyType", "APARTMENT")
                        .param("minPrice", "5000000")
                        .param("maxPrice", "15000000")
                        .param("minBedrooms", "2")
                        .param("maxBedrooms", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].city").value("Hanoi"))
                .andExpect(jsonPath("$[0].propertyType").value("APARTMENT"))
                .andExpect(jsonPath("$[0].bedrooms").value(2));
    }

    @Test
    @DisplayName("searchRentals_withNoMatchingCriteria_shouldReturnEmptyList")
    void searchRentals_withNoMatchingCriteria_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/rentals/search")
                        .param("city", "Da Nang")
                        .param("propertyType", "STUDIO"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== SEARCH WITHOUT PARAMETERS TESTS ====================

    @Test
    @DisplayName("searchRentals_withoutParameters_shouldReturnAllAvailableRentals")
    void searchRentals_withoutParameters_shouldReturnAllAvailableRentals() throws Exception {
        mockMvc.perform(get("/api/rentals/search"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    // ==================== SEARCH BY STATUS TESTS ====================

    @Test
    @DisplayName("searchRentals_shouldOnlyReturnAvailableRentals")
    void searchRentals_shouldOnlyReturnAvailableRentals() throws Exception {
        // Set one rental to RENTED
        hanoiApartment.setStatus(RentalStatus.RENTED);
        rentalRepository.save(hanoiApartment);

        mockMvc.perform(get("/api/rentals/search")
                        .param("city", "Hanoi"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1))) // Only the villa should be returned
                .andExpect(jsonPath("$[0].propertyType").value("VILLA"));
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("searchRentals_withInvalidPriceRange_shouldReturnBadRequest")
    void searchRentals_withInvalidPriceRange_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/rentals/search")
                        .param("minPrice", "30000000")
                        .param("maxPrice", "10000000")) // min > max
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("searchRentals_withInvalidBedroomRange_shouldReturnBadRequest")
    void searchRentals_withInvalidBedroomRange_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/rentals/search")
                        .param("minBedrooms", "5")
                        .param("maxBedrooms", "2")) // min > max
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("searchRentals_withNegativePrice_shouldReturnBadRequest")
    void searchRentals_withNegativePrice_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/rentals/search")
                        .param("minPrice", "-1000000"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}

