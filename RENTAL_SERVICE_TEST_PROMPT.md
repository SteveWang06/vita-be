You are a senior Java backend engineer.
The project is a Spring Boot microservice named `rental-service`, responsible for managing rental listings and rental requests.

✅ Your task:
Write comprehensive **unit tests** and **integration tests** for this service.

---

### ✅ Test Requirements

1. **Frameworks**
   - Use JUnit 5
   - Use Mockito for mocking dependencies
   - Use Spring Boot Test for integration tests
   - Use MockMvc for testing controllers

2. **Scope**
   Write tests for the following layers:
   - `Controller` → test all REST endpoints (`/api/rentals`, `/api/rentals/search`, `/api/rentals/{id}`, etc.)
   - `Service` → test business logic (create, update, delete rental, send request)
   - `Repository` → test basic CRUD operations with H2 in-memory database

3. **Test Naming Convention**
   Follow the `methodName_condition_expectedBehavior` pattern.
   Example:  
   `createRental_withValidData_shouldReturnSavedRental()`

4. **Coverage Goals**
   - Minimum 80% code coverage
   - Include both positive and negative test cases (validation errors, not found, unauthorized, etc.)

5. **Integration Test Setup**
   - Use `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`
   - Use `@AutoConfigureMockMvc`
   - Preload test data with `data.sql`
   - Assert responses using `MockMvc` and `jsonPath`

6. **Directory Structure**
   Follow standard Maven test structure:
src/
├── main/java/com/vita/rental_service/...
└── test/java/com/vita/rental_service/
├── controller/
├── service/
└── repository/

7. **Additional Tasks**
- Generate a sample `application-test.yml` for testing profile.
- Use `@ActiveProfiles("test")` in all integration tests.
- Add a `TestUtils` class for common methods (e.g., mock DTOs).

---

### Example test classes to generate
- `RentalControllerTest.java`
- `RentalServiceTest.java`
- `RentalRepositoryTest.java`
- `RentalRequestControllerTest.java`
- `RentalRequestServiceTest.java`

---
Focus only on the `rental-service` folder. Ignore other services like auth-service or user-service.
