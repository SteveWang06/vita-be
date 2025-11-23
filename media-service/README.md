# Media Service

**Media Service** for Vita Backend - Handle image and video uploads to AWS S3 using Presigned URLs.

---

## 📋 Overview

Media Service provides secure and efficient file upload capabilities using AWS S3 Presigned URLs. This approach ensures:
- ✅ **Secure**: AWS credentials never exposed to frontend
- ✅ **Fast**: Direct upload to S3 without backend bottleneck
- ✅ **Scalable**: No backend bandwidth consumption
- ✅ **Controlled**: Full validation and authorization before upload

---

## 🏗️ Architecture

```
┌─────────────┐                    ┌──────────────┐                    ┌─────────┐
│  Frontend   │                    │   Backend    │                    │ AWS S3  │
└──────┬──────┘                    └──────┬───────┘                    └────┬────┘
       │                                  │                                  │
       │ 1. Request presigned URL         │                                  │
       │─────────────────────────────────>│                                  │
       │                                  │ 2. Validate & Generate URL       │
       │                                  │─────────────────────────────────>│
       │ 3. Return presigned URL          │                                  │
       │<─────────────────────────────────│                                  │
       │                                  │                                  │
       │ 4. Upload file directly          │                                  │
       │──────────────────────────────────────────────────────────────────>│
       │                                  │                                  │
       │ 5. Confirm upload                │                                  │
       │─────────────────────────────────>│ 6. Verify file exists           │
       │                                  │─────────────────────────────────>│
       │ 7. Return file URL               │                                  │
       │<─────────────────────────────────│                                  │
```

---

## ⚙️ Configuration

### Application Properties (`application.yml`)

```yaml
server:
  port: 8085

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/media_service_db
    username: root
    password: root

aws:
  s3:
    access-key: YOUR_AWS_ACCESS_KEY
    secret-key: YOUR_AWS_SECRET_KEY
    region: ap-southeast-1
    bucket-name: vita-media-bucket
    presigned-url-expiration: 3600  # 1 hour

file:
  upload:
    max-size-image: 10485760      # 10MB
    max-size-video: 104857600     # 100MB
    allowed-image-types:
      - image/jpeg
      - image/png
      - image/webp
      - image/gif
    allowed-video-types:
      - video/mp4
      - video/mpeg
      - video/quicktime
      - video/webm
```

### Environment Variables

Set these environment variables:

```bash
export AWS_S3_ACCESS_KEY=your_access_key
export AWS_S3_SECRET_KEY=your_secret_key
export AWS_S3_REGION=ap-southeast-1
export AWS_S3_BUCKET_NAME=vita-media-bucket
```

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- MySQL 8.0+
- AWS S3 Bucket

### Build & Run

```bash
# Navigate to media-service directory
cd media-service

# Build the project
./mvnw clean install

# Run the service
./mvnw spring-boot:run
```

Service will start on **http://localhost:8085**

---

## 📡 API Endpoints

### Upload Management

#### 1. Request Presigned URL
**POST** `/media/upload-request`

Request a presigned URL for uploading file to S3.

**Headers:**
- `Authorization: Bearer {jwt_token}` (Required)
- `Content-Type: application/json`

**Request Body:**
```json
{
  "fileName": "profile-photo.jpg",
  "fileType": "image/jpeg",
  "fileSize": 1048576
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Presigned URL generated successfully",
  "result": {
    "fileId": "uuid-file-id",
    "uploadUrl": "https://bucket.s3.region.amazonaws.com/...",
    "expiresIn": 3600,
    "fileKey": "uploads/image/user-id/2024-01-15/uuid.jpg"
  }
}
```

#### 2. Upload File to S3 (Frontend)

Use the `uploadUrl` from step 1 to upload file directly:

```javascript
await axios.put(uploadUrl, file, {
  headers: {
    'Content-Type': file.type
  }
});
```

#### 3. Confirm Upload
**POST** `/media/{fileId}/confirm`

Confirm that file has been uploaded successfully.

**Headers:**
- `Authorization: Bearer {jwt_token}` (Required)

**Response:**
```json
{
  "code": 1000,
  "message": "Upload confirmed successfully",
  "result": {
    "fileId": "uuid-file-id",
    "fileName": "profile-photo.jpg",
    "fileUrl": "https://bucket.s3.region.amazonaws.com/...",
    "fileType": "image/jpeg",
    "fileSize": 1048576,
    "mediaType": "IMAGE",
    "status": "COMPLETED",
    "uploadedAt": "2024-01-15T10:30:00",
    "createdAt": "2024-01-15T10:29:00"
  }
}
```

### Media Management

#### 4. Get Media by ID
**GET** `/media/{fileId}`

**Headers:**
- `Authorization: Bearer {jwt_token}` (Required)

#### 5. Get User's Media
**GET** `/media?type={IMAGE|VIDEO}&page=0&size=20`

Get all media uploaded by current user with pagination.

**Query Parameters:**
- `type` (optional): Filter by media type (IMAGE or VIDEO)
- `page` (optional, default: 0): Page number
- `size` (optional, default: 20): Page size

**Headers:**
- `Authorization: Bearer {jwt_token}` (Required)

#### 6. Delete Media
**DELETE** `/media/{fileId}`

Delete media file. Only owner or ADMIN can delete.

**Headers:**
- `Authorization: Bearer {jwt_token}` (Required)

### Admin Management

#### 7. Get All Media (Admin)
**GET** `/media/admin?page=0&size=20`

Get all media from all users (Admin only).

**Headers:**
- `Authorization: Bearer {jwt_token}` (Admin token required)

#### 8. Cleanup Expired Uploads (Admin)
**POST** `/media/admin/cleanup`

Cleanup expired pending uploads. Marks expired uploads as FAILED.

**Headers:**
- `Authorization: Bearer {jwt_token}` (Admin token required)

---

## 🔒 Security Features

### File Validation
- ✅ File type validation (whitelist)
- ✅ File size limits (10MB for images, 100MB for videos)
- ✅ User quota check (1GB per user)
- ✅ Presigned URL expiration (1 hour)

### Authentication & Authorization
- ✅ JWT token required for all endpoints
- ✅ User can only access their own media
- ✅ Admin can access all media
- ✅ Owner or Admin can delete media

### AWS S3 Security
- ✅ AWS credentials stored in backend only
- ✅ Presigned URLs with short expiration
- ✅ S3 bucket with proper IAM policies

---

## 📂 Project Structure

```
media-service/
├── src/
│   ├── main/
│   │   ├── java/com/vita/media_service/
│   │   │   ├── config/
│   │   │   │   ├── S3Config.java
│   │   │   │   └── FileUploadConfig.java
│   │   │   ├── controller/
│   │   │   │   └── MediaController.java
│   │   │   ├── domain/
│   │   │   │   ├── entity/
│   │   │   │   │   └── Media.java
│   │   │   │   └── enums/
│   │   │   │       ├── MediaType.java
│   │   │   │       └── MediaStatus.java
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   └── PresignedUrlRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── PresignedUrlResponse.java
│   │   │   │       ├── MediaResponse.java
│   │   │   │       ├── PageResponse.java
│   │   │   │       └── ApiResponse.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── MediaNotFoundException.java
│   │   │   │   ├── InvalidFileTypeException.java
│   │   │   │   ├── FileSizeExceededException.java
│   │   │   │   ├── FileNotFoundInS3Exception.java
│   │   │   │   ├── UnauthorizedAccessException.java
│   │   │   │   └── QuotaExceededException.java
│   │   │   ├── repository/
│   │   │   │   └── MediaRepository.java
│   │   │   ├── service/
│   │   │   │   └── MediaService.java
│   │   │   └── MediaServiceApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/com/vita/media_service/
└── pom.xml
```

---

## 🧪 Testing

Use the Postman collection provided in the root directory:
- Import `Vita_Backend_API_Collection.json`
- Navigate to **Media Service** folder
- Test APIs through API Gateway

### Quick Test Flow:

1. **Login** (Auth Service) to get JWT token
2. **Request Presigned URL** for image upload
3. **Upload file** directly to S3 using the presigned URL
4. **Confirm upload** to mark as completed
5. **Get user's media** to see uploaded files
6. **Delete media** when no longer needed

---

## 🛠️ Technology Stack

- **Spring Boot 3.3.5**: Application framework
- **AWS SDK for Java v2**: S3 integration
- **MySQL**: Database
- **Lombok**: Reduce boilerplate code
- **MapStruct**: DTO mapping
- **Maven**: Build tool

---

## 🌐 Integration with API Gateway

All API calls should go through API Gateway:

```
http://localhost:8080/api/v1/media/*
```

API Gateway handles:
- ✅ Routing to media-service
- ✅ JWT authentication
- ✅ Header forwarding (X-User-Id, X-User-Role)
- ✅ Logging and monitoring

---

## 📝 Database Schema

### Media Table

| Column | Type | Description |
|--------|------|-------------|
| id | VARCHAR(36) | Primary key (UUID) |
| user_id | VARCHAR(36) | User ID who uploaded |
| file_name | VARCHAR(255) | Original filename |
| file_key | VARCHAR(500) | S3 object key (unique) |
| file_type | VARCHAR(50) | MIME type |
| file_size | BIGINT | File size in bytes |
| media_type | ENUM | IMAGE or VIDEO |
| status | ENUM | PENDING, COMPLETED, FAILED, DELETED |
| upload_url | VARCHAR(1000) | Presigned upload URL |
| public_url | VARCHAR(500) | Public S3 URL |
| thumbnail_url | VARCHAR(500) | Thumbnail URL (optional) |
| expires_at | DATETIME | Presigned URL expiration |
| completed_at | DATETIME | Upload completion time |
| created_at | DATETIME | Record creation time |
| updated_at | DATETIME | Last update time |
| metadata | TEXT | Additional metadata (JSON) |

---

## 🚨 Error Codes

| Code | Message | Description |
|------|---------|-------------|
| 400 | Invalid file type | File type not in whitelist |
| 400 | File size exceeded | File too large |
| 400 | Validation failed | Request body validation failed |
| 403 | Unauthorized access | Not owner or admin |
| 404 | Media not found | File ID doesn't exist |
| 404 | File not found in S3 | File not uploaded to S3 |
| 429 | Quota exceeded | User quota limit reached |
| 500 | Internal server error | Unexpected error |

---

## 📊 Monitoring & Maintenance

### Cleanup Expired Uploads

Run periodically (e.g., daily cron job):

```bash
curl -X POST http://localhost:8080/api/v1/media/admin/cleanup \
  -H "Authorization: Bearer {admin_token}"
```

This will mark expired pending uploads as FAILED.

---

## 🔗 Related Services

- **API Gateway**: Port 8080 (Entry point)
- **Auth Service**: Port 8082 (Authentication)
- **User Service**: Port 8081 (User management)
- **Rental Service**: Port 8084 (Rental properties)

---

## 📞 Support

For issues or questions, please refer to the main project README or contact the development team.

---

## 📄 License

This service is part of the Vita Backend project.

