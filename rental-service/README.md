# Rental Service

House and apartment rental management service for the Vita platform.

## Overview

The rental-service handles all operations related to house and apartment rentals. It allows users to post rental listings, search for available properties, send rental requests, and leave reviews.

## Features

### 1. Rental Listing Management
- Create, read, update, and delete rental listings
- Support multiple property types (apartment, house, studio, villa, etc.)
- Attach photos, descriptions, and pricing information
- Owner-only access control for listings

### 2. Rental Request Management
- Send rental requests to property owners
- Track request status (pending, accepted, rejected, canceled)
- Prevent duplicate requests
- Status update notifications

### 3. Search & Filter
- Advanced search with multiple criteria
- Filter by location, price range, property type
- Pagination and sorting support
- Full-text search on title and description

### 4. Review & Rating
- Add reviews and ratings (1-5 stars) for rentals
- View all reviews for a property
- One review per user per rental
- Review ownership validation

### 5. Admin Management
- View all listings with pagination
- Approve or reject pending listings
- Change listing status
- Moderate content

## Technology Stack

- **Java 21**
- **Spring Boot 3.5.0**
- **Spring Data JPA** with Hibernate
- **MySQL** database
- **MapStruct** for DTO mapping
- **Lombok** for boilerplate reduction
- **Jakarta Validation** for input validation

## Project Structure

```
rental-service/
├── src/main/java/com/vita/rental_service/
│   ├── RentalServiceApplication.java
│   ├── config/           # Configuration classes
│   ├── controller/       # REST controllers
│   │   ├── RentalController.java
│   │   ├── RentalRequestController.java
│   │   ├── SearchController.java
│   │   ├── ReviewController.java
│   │   └── AdminController.java
│   ├── service/          # Business logic
│   │   ├── RentalService.java
│   │   ├── RentalRequestService.java
│   │   ├── SearchService.java
│   │   ├── ReviewService.java
│   │   └── AdminService.java
│   ├── repository/       # Data access
│   │   ├── RentalRepository.java
│   │   ├── RentalRequestRepository.java
│   │   └── ReviewRepository.java
│   ├── domain/
│   │   ├── entity/       # JPA entities
│   │   │   ├── Rental.java
│   │   │   ├── RentalRequest.java
│   │   │   └── Review.java
│   │   └── enums/        # Enumerations
│   │       ├── RentalStatus.java
│   │       ├── RequestStatus.java
│   │       └── PropertyType.java
│   ├── dto/              # Data Transfer Objects
│   │   ├── request/
│   │   └── response/
│   ├── mapper/           # MapStruct mappers
│   │   ├── RentalMapper.java
│   │   ├── RentalRequestMapper.java
│   │   └── ReviewMapper.java
│   ├── exception/        # Custom exceptions
│   └── util/             # Utility classes
└── src/main/resources/
    └── application.yml   # Configuration

```

## Database Schema

### Tables

#### rentals
- `id` (VARCHAR, PK)
- `owner_id` (VARCHAR, FK to user-service)
- `title` (VARCHAR)
- `description` (TEXT)
- `property_type` (ENUM)
- `price` (DECIMAL)
- `city`, `district`, `address` (VARCHAR)
- `bedrooms`, `bathrooms`, `area` (INT)
- `photos` (TEXT, comma-separated URLs)
- `status` (ENUM)
- `version` (BIGINT, for optimistic locking)
- `created_at`, `updated_at` (TIMESTAMP)

#### rental_requests
- `id` (VARCHAR, PK)
- `rental_id` (VARCHAR, FK)
- `renter_id` (VARCHAR, FK to user-service)
- `move_in_date` (DATE)
- `lease_duration_months` (INT)
- `message` (TEXT)
- `status` (ENUM)
- `rejection_reason` (TEXT)
- `created_at`, `updated_at` (TIMESTAMP)

#### reviews
- `id` (VARCHAR, PK)
- `rental_id` (VARCHAR, FK)
- `reviewer_id` (VARCHAR, FK to user-service)
- `rating` (INT, 1-5)
- `comment` (TEXT)
- `created_at`, `updated_at` (TIMESTAMP)

## API Endpoints

All endpoints are accessed through API Gateway at `http://localhost:8080/api/v1/rentals/...`

### Rental Listing Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/rentals` | Create a new rental listing | Yes |
| GET | `/api/rentals/{id}` | Get rental details | No |
| GET | `/api/rentals/user/{userId}` | Get all listings by owner | No |
| PUT | `/api/rentals/{id}` | Update a rental listing | Yes (Owner/Admin) |
| DELETE | `/api/rentals/{id}` | Delete a rental listing | Yes (Owner/Admin) |

### Rental Request Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/rentals/{rentalId}/requests` | Send a rental request | Yes |
| GET | `/api/rentals/requests/{requestId}` | Get request details | Yes |
| GET | `/api/rentals/user/{userId}/requests` | Get all requests by user | Yes |
| PATCH | `/api/rentals/requests/{requestId}/status` | Update request status | Yes |

### Search & Filter Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/rentals/search` | Search rentals with filters | No |

**Query Parameters:**
- `keyword` - Search in title and description
- `city` - Filter by city
- `district` - Filter by district
- `type` - Property type (APARTMENT, HOUSE, STUDIO, etc.)
- `minPrice`, `maxPrice` - Price range
- `minBedrooms`, `minBathrooms`, `minArea` - Property specifications
- `page`, `size` - Pagination
- `sortBy`, `sortDir` - Sorting (default: createdAt, DESC)

**Example:**
```
GET /api/rentals/search?city=Hanoi&minPrice=5000000&maxPrice=15000000&type=APARTMENT&page=0&size=10&sortBy=price&sortDir=ASC
```

### Review & Rating Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/rentals/{rentalId}/reviews` | Add a review | Yes |
| GET | `/api/rentals/{rentalId}/reviews` | Get all reviews for rental | No |
| DELETE | `/api/rentals/reviews/{reviewId}` | Delete a review | Yes (Reviewer/Admin) |

### Admin Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/admin/rentals` | Get all rentals (paginated) | Yes (Admin) |
| GET | `/api/admin/rentals/status/{status}` | Get rentals by status | Yes (Admin) |
| PATCH | `/api/admin/rentals/{id}/status` | Update rental status | Yes (Admin) |

## Running the Service

### Prerequisites
- Java 21
- Maven 3.8+
- MySQL 8.0+

### Setup

1. **Create Database:**
```sql
CREATE DATABASE rental_service_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **Update Configuration:**

Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/rental_service_db
    username: your_username
    password: your_password
```

3. **Build and Run:**
```bash
cd rental-service
mvn clean install
mvn spring-boot:run
```

The service will start on port `8084`.

## Example Requests

### 1. Create Rental Listing

```bash
POST http://localhost:8080/api/v1/rentals
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "title": "Modern 2BR Apartment in District 1",
  "description": "Beautiful apartment with city view, fully furnished",
  "propertyType": "APARTMENT",
  "price": 12000000,
  "city": "Ho Chi Minh",
  "district": "District 1",
  "address": "123 Nguyen Hue Street",
  "bedrooms": 2,
  "bathrooms": 2,
  "area": 85,
  "photos": "http://example.com/photo1.jpg,http://example.com/photo2.jpg"
}
```

### 2. Search Rentals

```bash
GET http://localhost:8080/api/v1/rentals/search?city=Ho%20Chi%20Minh&minPrice=5000000&maxPrice=15000000&type=APARTMENT&page=0&size=10
```

### 3. Send Rental Request

```bash
POST http://localhost:8080/api/v1/rentals/{rentalId}/requests
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "moveInDate": "2025-02-01",
  "leaseDurationMonths": 12,
  "message": "Hello, I'm interested in renting your apartment"
}
```

### 4. Add Review

```bash
POST http://localhost:8080/api/v1/rentals/{rentalId}/reviews
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "rating": 5,
  "comment": "Great apartment, highly recommended!"
}
```

## Testing

The rental-service includes comprehensive test coverage with **104 tests** covering all layers.

### Test Suite Overview

| Category | Count | Coverage |
|----------|-------|----------|
| **Unit Tests** (Service layer) | 42 | Business logic, authorization, validations |
| **Integration Tests** (Controllers) | 51 | REST endpoints, HTTP status codes |
| **Integration Tests** (Repository) | 11 | Data persistence, custom queries |
| **Total** | **104** | **≥80% code coverage** |

### Test Classes

```
✓ RentalServiceTest           - 14 tests
✓ RentalRequestServiceTest     - 15 tests
✓ ReviewServiceTest            - 13 tests
✓ RentalRepositoryTest         - 11 tests
✓ RentalControllerTest         - 12 tests
✓ RentalRequestControllerTest  - 11 tests
✓ SearchControllerTest         - 13 tests
✓ ReviewControllerTest         - 15 tests
```

### Running Tests

```bash
# Run all tests
./mvnw clean test

# Run specific test class
./mvnw test -Dtest=RentalServiceTest

# Run with coverage report
./mvnw clean test jacoco:report
```

### Test Documentation

For detailed test documentation, see:
- [TEST_QUICKSTART.md](./TEST_QUICKSTART.md) - Quick commands and setup
- [TEST_DOCUMENTATION.md](./TEST_DOCUMENTATION.md) - Complete test specifications

### Test Stack

- **JUnit 5** - Testing framework
- **Mockito** - Mocking dependencies
- **Spring Boot Test** - Integration testing
- **MockMvc** - Controller endpoint testing
- **AssertJ** - Fluent assertions
- **H2 Database** - In-memory test database

## Error Handling

The service returns appropriate HTTP status codes:

- `200 OK` - Success
- `201 Created` - Resource created
- `204 No Content` - Success with no response body
- `400 Bad Request` - Validation error
- `403 Forbidden` - Unauthorized access
- `404 Not Found` - Resource not found
- `409 Conflict` - Duplicate request
- `500 Internal Server Error` - Server error

Error response format:
```json
{
  "message": "Error description"
}
```

Validation error format:
```json
{
  "field1": "Error message 1",
  "field2": "Error message 2"
}
```

## Authorization

The service integrates with API Gateway for authentication. Required headers:
- `Authorization: Bearer {JWT_TOKEN}` - For authenticated requests
- `X-User-Id` - Automatically added by API Gateway
- `X-User-Role` - Automatically added by API Gateway

## Future Enhancements

- Integration with payment-service for deposits and transactions
- Integration with recommendation-service for personalized suggestions
- Advanced analytics and reporting
- Fraud detection
- Real-time notifications
- Geolocation-based search
- Image upload and management
- Booking calendar integration

## Testing

Run tests:
```bash
mvn test
```

## Contributing

Follow the project's coding style guide (`CODING_STYLE.md`) and git workflow (`PUSH_CODE_TO_GITHUB_RULES.md`).

## License

Copyright © 2025 Vita Platform

