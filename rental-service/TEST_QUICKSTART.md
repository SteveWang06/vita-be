# 🧪 Rental Service - Test Quick Start

## 📦 Prerequisites

- Java 21+
- Maven 3.6+ (hoặc sử dụng `./mvnw`)

## 🚀 Run Tests

### 1️⃣ Chạy tất cả tests (104 tests)
```bash
cd rental-service
./mvnw clean test
```

### 2️⃣ Chạy chỉ Unit Tests (42 tests)
```bash
./mvnw test -Dtest=*ServiceTest
```

### 3️⃣ Chạy chỉ Integration Tests (62 tests)
```bash
./mvnw test -Dtest=*ControllerTest,*RepositoryTest
```

### 4️⃣ Chạy specific test class
```bash
# Service layer
./mvnw test -Dtest=RentalServiceTest
./mvnw test -Dtest=RentalRequestServiceTest
./mvnw test -Dtest=ReviewServiceTest

# Controller layer
./mvnw test -Dtest=RentalControllerTest
./mvnw test -Dtest=SearchControllerTest
./mvnw test -Dtest=ReviewControllerTest

# Repository layer
./mvnw test -Dtest=RentalRepositoryTest
```

### 5️⃣ Chạy single test method
```bash
./mvnw test -Dtest=RentalServiceTest#createRental_withValidData_shouldReturnSavedRental
```

## 📊 View Coverage Report

```bash
./mvnw clean test jacoco:report
open target/site/jacoco/index.html
```

## ✅ Expected Output

```
[INFO] Tests run: 104, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## 📋 Test Summary

| Category | Count | Description |
|----------|-------|-------------|
| **Unit Tests** | 42 | Service layer business logic |
| **Integration Tests** | 62 | Controllers + Repository |
| **Total** | 104 | Complete coverage |

### Breakdown by Test Class

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

## 🔍 What's Being Tested

### ✅ CRUD Operations
- Create, Read, Update, Delete for all entities
- Error handling for invalid data
- Not found scenarios

### ✅ Authorization
- Owner-only operations
- User-specific data access
- Unauthorized access prevention

### ✅ Business Logic
- Rental request lifecycle
- Review rating validation (1-5)
- Duplicate request prevention
- Status transitions

### ✅ Search & Filtering
- Search by city, district, property type
- Price range filtering
- Bedroom count filtering
- Combined multi-criteria search

### ✅ Edge Cases
- Empty results
- Invalid input ranges
- Null values
- Duplicate operations

## 🛠️ Test Configuration

- **Test Database**: H2 in-memory (auto-configured)
- **Test Profile**: `application-test.yml` activated
- **Transaction Management**: Auto-rollback after each test
- **Port**: Random (prevents conflicts)

## 📁 Test Files Location

```
src/test/java/com/vita/rental_service/
├── TestUtils.java                    # Test helpers
├── service/                          # Unit tests
│   ├── RentalServiceTest.java
│   ├── RentalRequestServiceTest.java
│   └── ReviewServiceTest.java
├── repository/                       # Integration tests
│   └── RentalRepositoryTest.java
└── controller/                       # Integration tests
    ├── RentalControllerTest.java
    ├── RentalRequestControllerTest.java
    ├── SearchControllerTest.java
    └── ReviewControllerTest.java
```

## 🐛 Troubleshooting

### ❌ Tests fail with "Connection refused"
**Solution**: Tests sử dụng H2 in-memory database, không cần MySQL running.

### ❌ Tests fail with "Port already in use"
**Solution**: Tests sử dụng random port (`server.port: 0` in test config).

### ❌ Maven wrapper không executable
**Solution**: 
```bash
chmod +x mvnw
./mvnw clean test
```

### ❌ H2 dependency not found
**Solution**: Đã được thêm vào `pom.xml` với scope `test`.

## 📚 Additional Documentation

Xem chi tiết đầy đủ tại: [TEST_DOCUMENTATION.md](./TEST_DOCUMENTATION.md)

---

**Quick Test Command**:
```bash
./mvnw clean test
```

Expected: ✅ **104 tests passed** in ~30-60 seconds

