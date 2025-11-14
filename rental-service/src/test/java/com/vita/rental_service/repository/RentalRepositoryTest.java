package com.vita.rental_service.repository;

import com.vita.rental_service.TestUtils;
import com.vita.rental_service.domain.entity.Rental;
import com.vita.rental_service.domain.enums.PropertyType;
import com.vita.rental_service.domain.enums.RentalStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RentalRepository Integration Tests")
class RentalRepositoryTest {

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Rental testRental1;
    private Rental testRental2;

    @BeforeEach
    void setUp() {
        testRental1 = TestUtils.createTestRental();
        testRental1.setId(null); // Let JPA generate ID
        testRental1.setOwnerId("owner-1");
        testRental1.setCity("Hanoi");
        testRental1.setDistrict("Ba Dinh");
        testRental1.setPropertyType(PropertyType.APARTMENT);
        testRental1.setPrice(new BigDecimal("10000000"));
        testRental1.setStatus(RentalStatus.AVAILABLE);

        testRental2 = TestUtils.createTestRental();
        testRental2.setId(null);
        testRental2.setOwnerId("owner-2");
        testRental2.setCity("Ho Chi Minh");
        testRental2.setDistrict("District 1");
        testRental2.setPropertyType(PropertyType.HOUSE);
        testRental2.setPrice(new BigDecimal("20000000"));
        testRental2.setStatus(RentalStatus.RENTED);
    }

    // ==================== BASIC CRUD TESTS ====================

    @Test
    @DisplayName("save_withValidRental_shouldPersistRental")
    void save_withValidRental_shouldPersistRental() {
        // Act
        Rental saved = rentalRepository.save(testRental1);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOwnerId()).isEqualTo("owner-1");
        assertThat(saved.getCity()).isEqualTo("Hanoi");
        
        Optional<Rental> found = rentalRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo(testRental1.getTitle());
    }

    @Test
    @DisplayName("findById_withValidId_shouldReturnRental")
    void findById_withValidId_shouldReturnRental() {
        // Arrange
        Rental saved = entityManager.persistAndFlush(testRental1);
        String id = saved.getId();

        // Act
        Optional<Rental> found = rentalRepository.findById(id);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(id);
        assertThat(found.get().getOwnerId()).isEqualTo("owner-1");
    }

    @Test
    @DisplayName("findById_withInvalidId_shouldReturnEmpty")
    void findById_withInvalidId_shouldReturnEmpty() {
        // Act
        Optional<Rental> found = rentalRepository.findById("non-existent-id");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("delete_withValidRental_shouldRemoveFromDatabase")
    void delete_withValidRental_shouldRemoveFromDatabase() {
        // Arrange
        Rental saved = entityManager.persistAndFlush(testRental1);
        String id = saved.getId();

        // Act
        rentalRepository.delete(saved);
        entityManager.flush();

        // Assert
        Optional<Rental> found = rentalRepository.findById(id);
        assertThat(found).isEmpty();
    }

    // ==================== CUSTOM QUERY TESTS ====================

    @Test
    @DisplayName("findByOwnerId_withValidOwnerId_shouldReturnOwnerRentals")
    void findByOwnerId_withValidOwnerId_shouldReturnOwnerRentals() {
        // Arrange
        entityManager.persistAndFlush(testRental1);
        entityManager.persistAndFlush(testRental2);

        // Act
        List<Rental> owner1Rentals = rentalRepository.findByOwnerId("owner-1");
        List<Rental> owner2Rentals = rentalRepository.findByOwnerId("owner-2");

        // Assert
        assertThat(owner1Rentals).hasSize(1);
        assertThat(owner1Rentals.get(0).getOwnerId()).isEqualTo("owner-1");
        
        assertThat(owner2Rentals).hasSize(1);
        assertThat(owner2Rentals.get(0).getOwnerId()).isEqualTo("owner-2");
    }

    // Note: The following methods are not implemented in RentalRepository:
    // - findByStatus(RentalStatus) - requires Pageable parameter
    // - findByCityAndDistrict() - not available
    // - findByPropertyType() - not available
    // - findByPriceBetween() - not available
    // Use searchRentals() for complex filtering instead

    // ==================== UPDATE TESTS ====================

    @Test
    @DisplayName("update_existingRental_shouldPersistChanges")
    void update_existingRental_shouldPersistChanges() {
        // Arrange
        Rental saved = entityManager.persistAndFlush(testRental1);
        String id = saved.getId();

        // Act
        saved.setTitle("Updated Title");
        saved.setPrice(new BigDecimal("15000000"));
        saved.setStatus(RentalStatus.INACTIVE); // Changed from UNDER_MAINTENANCE (doesn't exist)
        rentalRepository.save(saved);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Rental> updated = rentalRepository.findById(id);
        assertThat(updated).isPresent();
        assertThat(updated.get().getTitle()).isEqualTo("Updated Title");
        assertThat(updated.get().getPrice()).isEqualTo(new BigDecimal("15000000"));
        assertThat(updated.get().getStatus()).isEqualTo(RentalStatus.INACTIVE);
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("findAll_withNoData_shouldReturnEmptyList")
    void findAll_withNoData_shouldReturnEmptyList() {
        // Act
        List<Rental> rentals = rentalRepository.findAll();

        // Assert
        assertThat(rentals).isEmpty();
    }

    @Test
    @DisplayName("findByOwnerId_withNonExistentOwner_shouldReturnEmptyList")
    void findByOwnerId_withNonExistentOwner_shouldReturnEmptyList() {
        // Arrange
        entityManager.persistAndFlush(testRental1);

        // Act
        List<Rental> rentals = rentalRepository.findByOwnerId("non-existent-owner");

        // Assert
        assertThat(rentals).isEmpty();
    }
}

