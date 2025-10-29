# Java Spring Boot Microservice Coding Style Guide

This style guide defines coding standards for all microservices in the project.  
All generated code must strictly follow this document.

---

## 1. Project Structure & Package Organization

- Follow **Domain-Driven Design (DDD)** inspired package structure:

com.<company>.<service>
├── config # Spring configuration, security, beans
├── controller # REST controllers, minimal logic
├── service # Business logic, transactional boundaries
├── repository # Interfaces for DB access
├── domain # Core domain models (entities, enums, value objects)
├── dto # Data Transfer Objects for API
├── mapper # MapStruct or manual mappers
├── exception # Custom exceptions & global handlers
└── util # Utility classes/helpers

- Keep layers **loosely coupled** and **highly cohesive**.

---

## 2. Naming Conventions

- **Classes & Interfaces**: PascalCase, meaningful and specific (`UserService`, `OrderRepository`).
- **Methods & Variables**: camelCase, descriptive but concise (`calculateTotalPrice`, `userRepository`).
- **Constants**: UPPER_SNAKE_CASE (`MAX_LOGIN_ATTEMPTS`).
- **Packages**: lowercase, no underscores (`com.example.auth.service`).
- **DTOs**: suffix with `Request` or `Response` (`UserCreateRequest`, `UserDetailsResponse`).
- Avoid abbreviations unless widely accepted.

---

## 3. Coding Practices

- Favor **immutability** where possible; use `final` for variables and method parameters.
- Keep methods **short (< 30 lines)** and focused on a single responsibility.
- Use **Java 8+ features**: Streams, Optionals, functional interfaces for clarity and brevity.
- Avoid premature optimization; write clear and maintainable code first.
- Use meaningful logging, do **not log sensitive data** (e.g., passwords, tokens).
- Document public methods with **Javadoc**, focusing on *why* not *what*.
- Use **Lombok** judiciously (`@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`) to reduce boilerplate.

---

## 4. Exception Handling

- Define **custom exceptions** extending `RuntimeException` for domain-specific errors.
- Use **global exception handler** (`@RestControllerAdvice`) to map exceptions to meaningful HTTP responses.
- Never expose internal errors or stack traces to clients.
- Log exceptions with sufficient context for troubleshooting.

---

## 5. Transactions & Concurrency

- Use `@Transactional` at service layer; annotate read-only operations with `@Transactional(readOnly = true)`.
- Avoid long-running transactions.
- Handle concurrent updates with **optimistic locking** (`@Version`).
- Design for **idempotency** where applicable (e.g., create operations).

---

## 6. Security

- Hash passwords using `BCryptPasswordEncoder` or better.
- Secure sensitive endpoints with **role-based access control (RBAC)**.
- Validate all inputs rigorously; prefer declarative validation (`javax.validation`).
- Never expose sensitive information in logs or error messages.
- Use HTTPS in all environments.

---

## 7. REST API Design

- Follow **RESTful principles** strictly.
- Use **meaningful resource names** in plural (`/api/users`).
- Support pagination, filtering, and sorting where needed.
- Use appropriate HTTP status codes.
- Support HATEOAS if project scale requires.
- Use DTOs exclusively in API layer; never expose entities.
- Use **OpenAPI/Swagger** for documentation.

---

## 8. Testing

- Write **unit tests** for all business logic.
- Write **integration tests** for repository and controller layers.
- Use **testcontainers** for integration tests with real DB where feasible.
- Follow **Arrange-Act-Assert** pattern in tests.
- Aim for high coverage but prioritize critical paths and complex logic.

---

## 9. Code Formatting & Style

- Follow **Google Java Style Guide** or equivalent consistently.
- Use **4 spaces** for indentation; no tabs.
- Line length max 120 characters.
- Braces `{}` always on same line as control statement.
- Blank lines to separate logical blocks of code.
- Use meaningful variable names, avoid one-letter names except for indices in loops.

---

## 10. Documentation & Comments

- Comments should explain *why* a code block exists, not *what* it does.
- Keep comments up-to-date.
- Use **README.md** in each module explaining module purpose and usage.
- Use **JavaDoc** for public APIs and complex methods.

---

## 11. Dependencies & Configuration

- Declare all dependencies with explicit versions in `pom.xml` or `build.gradle`.
- Avoid unnecessary dependencies.
- Externalize configuration in `application.yml` or environment variables.
- Use Spring Profiles for environment-specific configs.

---

## 12. Build & Deployment

- Use **multi-stage Docker builds** to reduce image size.
- Use CI/CD pipelines with static code analysis and tests on every commit.
- Use feature toggles to control releases.
- Log structured logs (JSON format) for easier analysis.

---

## 13. Logging

- Use SLF4J with Logback.
- Log at appropriate levels (`DEBUG`, `INFO`, `WARN`, `ERROR`).
- Correlate logs with **request IDs** for traceability.
- Do not log sensitive information.

---

## 14. Code Reviews

- Encourage peer reviews focusing on code quality, readability, and security.
- Use automated linters and formatters.
- Ensure tests cover new features or bug fixes.

---

_End of Coding Style Guide_
