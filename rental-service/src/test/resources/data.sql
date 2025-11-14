-- Sample test data for integration tests
-- This file is optional and will be loaded automatically by Spring Boot Test

-- Note: The tests are designed to work with or without this file
-- Each test creates its own test data in @BeforeEach methods

-- Uncomment below if you want pre-populated test data

/*
-- Sample Rentals
INSERT INTO rentals (id, owner_id, title, description, property_type, price, city, district, address, bedrooms, bathrooms, area, status, created_at)
VALUES
('test-rental-1', 'owner-123', 'Modern Apartment in District 1', 'Beautiful 2BR apartment', 'APARTMENT', 12000000, 'Ho Chi Minh', 'District 1', '123 Nguyen Hue', 2, 2, 85, 'AVAILABLE', NOW()),
('test-rental-2', 'owner-456', 'Cozy House in Hanoi', 'Spacious 3BR house', 'HOUSE', 15000000, 'Hanoi', 'Ba Dinh', '456 Giang Vo', 3, 2, 120, 'AVAILABLE', NOW());

-- Sample Rental Requests
INSERT INTO rental_requests (id, rental_id, renter_id, move_in_date, lease_duration_months, message, status, created_at)
VALUES
('test-request-1', 'test-rental-1', 'renter-789', '2025-02-01', 12, 'I am interested', 'PENDING', NOW());

-- Sample Reviews
INSERT INTO reviews (id, rental_id, reviewer_id, rating, comment, created_at)
VALUES
('test-review-1', 'test-rental-1', 'reviewer-111', 5, 'Excellent property!', NOW());
*/

