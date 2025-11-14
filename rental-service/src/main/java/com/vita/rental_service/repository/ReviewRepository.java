package com.vita.rental_service.repository;

import com.vita.rental_service.domain.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
    
    // Find reviews for a rental
    List<Review> findByRentalId(String rentalId);
    
    Page<Review> findByRentalId(String rentalId, Pageable pageable);
    
    // Find reviews by reviewer
    List<Review> findByReviewerId(String reviewerId);
    
    // Check if user already reviewed this rental
    Optional<Review> findByRentalIdAndReviewerId(String rentalId, String reviewerId);
    
    // Calculate average rating for a rental
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.rentalId = :rentalId")
    Double getAverageRating(@Param("rentalId") String rentalId);
    
    // Count reviews for a rental
    long countByRentalId(String rentalId);
}

