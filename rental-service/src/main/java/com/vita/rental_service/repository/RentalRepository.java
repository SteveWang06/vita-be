package com.vita.rental_service.repository;

import com.vita.rental_service.domain.entity.Rental;
import com.vita.rental_service.domain.enums.PropertyType;
import com.vita.rental_service.domain.enums.RentalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, String>, JpaSpecificationExecutor<Rental> {
    
    // Find rentals by owner
    List<Rental> findByOwnerId(String ownerId);
    
    Page<Rental> findByOwnerId(String ownerId, Pageable pageable);
    
    // Find rentals by status
    Page<Rental> findByStatus(RentalStatus status, Pageable pageable);
    
    // Find rentals by city and status
    Page<Rental> findByCityAndStatus(String city, RentalStatus status, Pageable pageable);
    
    // Find available rentals in a city
    @Query("SELECT r FROM Rental r WHERE r.city = :city AND r.status = 'AVAILABLE'")
    Page<Rental> findAvailableRentalsInCity(@Param("city") String city, Pageable pageable);
    
    // Search with complex criteria
    @Query("SELECT r FROM Rental r WHERE " +
           "(:keyword IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:city IS NULL OR r.city = :city) " +
           "AND (:propertyType IS NULL OR r.propertyType = :propertyType) " +
           "AND (:minPrice IS NULL OR r.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR r.price <= :maxPrice) " +
           "AND r.status = 'AVAILABLE'")
    Page<Rental> searchRentals(
        @Param("keyword") String keyword,
        @Param("city") String city,
        @Param("propertyType") PropertyType propertyType,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable
    );
}

