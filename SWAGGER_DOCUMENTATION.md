# Swagger/OpenAPI Documentation - Vita Backend

Complete guide to access and use Swagger UI for all microservices.

---

## 📋 Overview

All Vita Backend microservices now have **Swagger UI** integrated for interactive API documentation and testing.

**Benefits:**
- ✅ Interactive API testing directly in browser
- ✅ Automatic API documentation
- ✅ Request/Response examples
- ✅ JWT authentication support
- ✅ Try out APIs without Postman

---

## 🚀 Quick Access Links

### Direct Service URLs (when services are running)

| Service | Swagger UI | OpenAPI JSON |
|---------|-----------|--------------|
| **API Gateway** | http://localhost:8080/swagger-ui.html | http://localhost:8080/api-docs |
| **Auth Service** | http://localhost:8082/auth/swagger-ui.html | http://localhost:8082/auth/api-docs |
| **User Service** | http://localhost:8081/user/swagger-ui.html | http://localhost:8081/user/api-docs |
| **Post Service** | http://localhost:8083/post/swagger-ui.html | http://localhost:8083/post/api-docs |
| **Rental Service** | http://localhost:8084/swagger-ui.html | http://localhost:8084/api-docs |
| **Media Service** | http://localhost:8085/swagger-ui.html | http://localhost:8085/api-docs |

---

## 🔐 How to Test APIs with Authentication

### Step 1: Get JWT Token

1. Navigate to **Auth Service Swagger**: http://localhost:8082/auth/swagger-ui.html
2. Find `POST /user/token` endpoint
3. Click **"Try it out"**
4. Enter credentials:
   ```json
   {
     "username": "admin@vita.com",
     "password": "admin123"
   }
   ```
5. Click **"Execute"**
6. Copy the `token` from the response

### Step 2: Authorize

1. Click the **"Authorize"** button (🔓 icon) at the top right
2. Paste your JWT token (without "Bearer" prefix)
3. Click **"Authorize"**
4. Click **"Close"**

### Step 3: Test Protected APIs

Now all APIs will automatically include your JWT token in requests!

---

## 📚 Service Documentation

### 0. API Gateway

**Port:** 8080  
**Context Path:** `/` (no context path)  
**Swagger UI:** http://localhost:8080/swagger-ui.html

**Description:**
API Gateway is the **centralized entry point** for all microservices. It provides:
- ✅ **Unified API documentation** for all services
- ✅ **Centralized routing** to backend services
- ✅ **Authentication & Authorization** via JWT
- ✅ **Request/Response logging**
- ✅ **Rate limiting & DDoS protection**

**Note:** API Gateway Swagger UI shows gateway-level documentation. For detailed service-specific APIs, use individual service Swagger UIs.

**Key Features:**
- Routes all `/api/v1/**` requests to appropriate services
- Handles JWT token validation
- Forwards user context (X-User-Id, X-User-Role) to services
- Provides health check endpoints

---

### 1. Auth Service

**Port:** 8082  
**Context Path:** `/auth`  
**Swagger UI:** http://localhost:8082/auth/swagger-ui.html

**Key Endpoints:**
- `POST /user/token` - Login and get JWT token
- `POST /user/introspect` - Validate JWT token
- `POST /user/logout` - Logout
- `POST /user/refresh` - Refresh JWT token
- `POST /user/register` - Register new user

**Authentication:** Not required for login/register endpoints

---

### 2. User Service

**Port:** 8081  
**Context Path:** `/user`  
**Swagger UI:** http://localhost:8081/user/swagger-ui.html

**Key Endpoints:**
- `GET /` - Get all users (Admin only)
- `GET /{userId}` - Get user by ID
- `PUT /{userId}` - Update user
- `DELETE /{userId}` - Delete user

**Authentication:** Required (JWT token)

---

### 3. Post Service

**Port:** 8083  
**Context Path:** `/post`  
**Swagger UI:** http://localhost:8083/post/swagger-ui.html

**Key Endpoints:**
- `GET /` - Get all posts (public)
- `GET /{id}` - Get post by ID (public)
- `POST /` - Create new post
- `PUT /{id}` - Update post
- `DELETE /{id}` - Delete post
- `POST /types` - Create post type
- `GET /types` - Get all post types

**Authentication:** 
- GET endpoints: Public (no token required)
- POST/PUT/DELETE: Required (JWT token)

---

### 4. Rental Service

**Port:** 8084  
**Context Path:** `/` (no context path)  
**Swagger UI:** http://localhost:8084/swagger-ui.html

**Key Endpoints:**

#### Rental Management
- `GET /rentals` - Get all rentals (public)
- `GET /rentals/{id}` - Get rental by ID (public)
- `POST /rentals` - Create rental listing
- `PUT /rentals/{id}` - Update rental
- `DELETE /rentals/{id}` - Delete rental

#### Search & Filter
- `POST /rentals/search` - Search rentals (public)
- `GET /rentals/search/suggestions` - Get search suggestions

#### Rental Requests
- `POST /rentals/{rentalId}/requests` - Send rental request
- `GET /rentals/requests/my-requests` - Get user's requests
- `GET /rentals/requests/received` - Get received requests (owner)
- `PATCH /rentals/requests/{requestId}/status` - Update request status

#### Reviews
- `POST /rentals/{rentalId}/reviews` - Create review
- `GET /rentals/{rentalId}/reviews` - Get rental reviews (public)
- `GET /rentals/{rentalId}/rating` - Get average rating (public)
- `DELETE /rentals/reviews/{reviewId}` - Delete review

#### Admin
- `GET /rentals/admin` - Get all rentals (Admin only)
- `PATCH /rentals/admin/{rentalId}/status` - Update rental status (Admin only)

**Authentication:**
- GET endpoints: Public (no token required)
- POST/PUT/PATCH/DELETE: Required (JWT token)
- Admin endpoints: Required (JWT token + ADMIN role)

---

### 5. Media Service

**Port:** 8085  
**Context Path:** `/` (no context path)  
**Swagger UI:** http://localhost:8085/swagger-ui.html

**Key Endpoints:**

#### Upload Management
- `POST /media/upload-request` - Request presigned URL
- `POST /media/{fileId}/confirm` - Confirm upload

#### Media Management
- `GET /media/{fileId}` - Get media by ID
- `GET /media` - Get user's media (with filters)
- `DELETE /media/{fileId}` - Delete media

#### Admin
- `GET /media/admin` - Get all media (Admin only)
- `POST /media/admin/cleanup` - Cleanup expired uploads (Admin only)

**Authentication:** All endpoints require JWT token

---

## 🎯 Testing Workflow

### Example: Create a Rental Listing

1. **Get JWT Token** (Auth Service):
   - POST `/auth/user/token`
   - Use credentials: `admin@vita.com` / `admin123`
   - Copy the token

2. **Authorize** in Rental Service Swagger:
   - Click 🔓 "Authorize" button
   - Paste token
   - Click "Authorize"

3. **Create Rental** (Rental Service):
   - POST `/rentals`
   - Click "Try it out"
   - Fill in the request body:
     ```json
     {
       "title": "Beautiful 2BR Apartment",
       "description": "Modern apartment in city center",
       "price": 1500.00,
       "propertyType": "APARTMENT",
       "address": "123 Main St",
       "city": "Ho Chi Minh City",
       "district": "District 1",
       "bedrooms": 2,
       "bathrooms": 1,
       "area": 80,
       "availableFrom": "2024-01-15T00:00:00"
     }
     ```
   - Click "Execute"
   - Check the response

4. **View Rental** (Public - no auth needed):
   - GET `/rentals/{id}`
   - Use the ID from step 3
   - Click "Try it out" → "Execute"

---

## 🌐 Access via API Gateway

All services can also be accessed through API Gateway:

| Service | API Gateway URL |
|---------|----------------|
| Auth Service | http://localhost:8080/api/v1/auth/* |
| User Service | http://localhost:8080/api/v1/user/* |
| Post Service | http://localhost:8080/api/v1/post/* |
| Rental Service | http://localhost:8080/api/v1/rentals/* |
| Media Service | http://localhost:8080/api/v1/media/* |

**Note:** Swagger UI should be accessed directly from each service, not through API Gateway.

---

## 📝 API Documentation Features

### Request Examples
Every endpoint shows:
- ✅ Required parameters
- ✅ Optional parameters
- ✅ Request body schema
- ✅ Example values

### Response Examples
Every endpoint shows:
- ✅ Success responses (200, 201)
- ✅ Error responses (400, 401, 403, 404, 500)
- ✅ Response schema
- ✅ Example responses

### Schema Definitions
Click on "Schemas" at the bottom to see all DTOs:
- Request DTOs
- Response DTOs
- Entity models
- Enum values

---

## 🔧 Configuration

### Swagger Configuration in application.yml

Each service has this configuration:

```yaml
springdoc:
  api-docs:
    path: /api-docs           # or /auth/api-docs for auth service
    enabled: true
  swagger-ui:
    path: /swagger-ui.html    # or /auth/swagger-ui.html for auth service
    enabled: true
    operations-sorter: method # Sort by HTTP method
    tags-sorter: alpha        # Sort tags alphabetically
  show-actuator: false
```

### OpenAPI Configuration Class

Each service has `OpenAPIConfig.java`:
- Defines API metadata (title, description, version)
- Configures servers (local, via Gateway)
- Sets up JWT Bearer authentication
- Contact and license information

---

## 🚨 Common Issues & Solutions

### Issue 1: Swagger UI not loading

**Solution:**
- Verify service is running: `curl http://localhost:PORT/actuator/health`
- Check port number matches service
- Check context path in URL

### Issue 2: "401 Unauthorized" on protected endpoints

**Solution:**
- Get fresh JWT token from Auth Service
- Click "Authorize" button (🔓)
- Paste token (without "Bearer" prefix)
- Click "Authorize"

### Issue 3: Token expired

**Solution:**
- Tokens expire after 1 hour (default)
- Get new token from Auth Service
- Use refresh token endpoint: `POST /auth/user/refresh`

### Issue 4: "403 Forbidden" on admin endpoints

**Solution:**
- Verify you're using admin account (`admin@vita.com`)
- Admin endpoints require ADMIN role
- Check token includes role claim

### Issue 5: Cannot access Swagger through API Gateway

**Solution:**
- Swagger UI should be accessed **directly** from each service
- Use direct URLs: http://localhost:PORT/swagger-ui.html
- Don't go through API Gateway for Swagger

---

## 🎨 Swagger UI Themes

Swagger UI comes with built-in themes. You can customize in `application.yml`:

```yaml
springdoc:
  swagger-ui:
    display-operation-id: true
    display-request-duration: true
    filter: true
    try-it-out-enabled: true
```

---

## 📊 Export OpenAPI Specification

Download OpenAPI JSON for each service:

```bash
# Auth Service
curl http://localhost:8082/auth/api-docs > auth-service-api.json

# User Service
curl http://localhost:8081/user/api-docs > user-service-api.json

# Post Service
curl http://localhost:8083/post/api-docs > post-service-api.json

# Rental Service
curl http://localhost:8084/api-docs > rental-service-api.json

# Media Service
curl http://localhost:8085/api-docs > media-service-api.json
```

Use these JSON files to:
- Import into Postman
- Generate client SDKs
- Share with frontend team
- Version control API specs

---

## 🔍 Swagger Annotations

### Common Annotations Used

```java
@Tag(name = "User Management", description = "User CRUD operations")
@Operation(summary = "Get user by ID", description = "Returns user details")
@ApiResponse(responseCode = "200", description = "Success")
@ApiResponse(responseCode = "404", description = "User not found")
@Parameter(description = "User ID", required = true)
@Schema(description = "User creation request")
```

---

## 🧪 Testing Best Practices

### 1. Test in Order
1. Auth Service → Get token
2. Other services → Use token

### 2. Use "Try it out" Feature
- Modify request bodies
- Test different parameters
- See actual responses

### 3. Check Response Codes
- 200/201: Success
- 400: Bad request (check validation)
- 401: Unauthorized (get token)
- 403: Forbidden (check role)
- 404: Not found
- 500: Server error (check logs)

### 4. Test Public vs Protected Endpoints
- Public: No auth needed (GET posts, rentals)
- Protected: Auth required (POST, PUT, DELETE)
- Admin: Admin role required

---

## 📚 Additional Resources

### Springdoc OpenAPI Documentation
- Official Docs: https://springdoc.org/
- GitHub: https://github.com/springdoc/springdoc-openapi

### OpenAPI Specification
- OpenAPI 3.0 Spec: https://swagger.io/specification/

### Tools
- Swagger Editor: https://editor.swagger.io/
- Swagger Inspector: https://inspector.swagger.io/

---

## 🎯 Quick Reference

### All Swagger UI URLs

```bash
# Auth Service
http://localhost:8082/auth/swagger-ui.html

# User Service
http://localhost:8081/user/swagger-ui.html

# Post Service
http://localhost:8083/post/swagger-ui.html

# Rental Service
http://localhost:8084/swagger-ui.html

# Media Service
http://localhost:8085/swagger-ui.html
```

### Test Account

```
Username: admin@vita.com
Password: admin123
Role: ADMIN
```

---

## ✅ Setup Checklist

- [x] Swagger dependencies added to all services
- [x] OpenAPI configuration classes created
- [x] application.yml configured for Swagger
- [x] API Gateway allows Swagger endpoints
- [x] JWT Bearer authentication configured
- [x] All services accessible via Swagger UI

---

## 🎉 You're Ready!

Start testing your APIs with Swagger UI now!

1. Start all services
2. Navigate to any Swagger UI URL
3. Get JWT token from Auth Service
4. Authorize with token
5. Test APIs interactively!

---

**Happy API Testing! 🚀**

