# Post Service

Service quản lý các bài đăng (posts) trong hệ thống Vita.

## Endpoints

Tất cả các endpoints đều được gọi thông qua API Gateway tại: `{{api_gateway_url}}/api/v1/post`

### 1. Tạo Post Mới (Create Post)
```http
POST /api/v1/post/create
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "title": "Tiêu đề bài viết",
  "content": "Nội dung bài viết"
}
```

**Yêu cầu:**
- JWT token hợp lệ (được lấy từ endpoint `/api/v1/auth/user/token`)
- API Gateway sẽ tự động validate token và trích xuất `userId` từ JWT payload
- `userId` sẽ được tự động gán vào trường `userId` của post

**Response:**
```json
{
  "id": "post_id",
  "title": "Tiêu đề bài viết",
  "content": "Nội dung bài viết",
  "userId": "user_id_from_jwt_token",
  "createdAt": "2025-10-28T10:00:00"
}
```

### 2. Lấy Tất Cả Posts
```http
GET /api/v1/post
```
**Lưu ý:** Endpoint này là public (không cần authentication) - Cho phép đọc danh sách posts

**Response:**
```json
[
  {
    "id": "post_id",
    "title": "Tiêu đề bài viết",
    "content": "Nội dung bài viết",
    "userId": "user_id",
    "createdAt": "2025-10-28T10:00:00"
  }
]
```

### 3. Lấy Post Theo ID
```http
GET /api/v1/post/{postId}
```
**Lưu ý:** Endpoint này là public (không cần authentication) - Cho phép đọc chi tiết post

**Response:**
```json
{
  "id": "post_id",
  "title": "Tiêu đề bài viết",
  "content": "Nội dung bài viết",
  "userId": "user_id",
  "createdAt": "2025-10-28T10:00:00"
}
```

### 4. Cập Nhật Post
```http
PUT /api/v1/post/{postId}
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "title": "Tiêu đề mới",
  "content": "Nội dung mới"
}
```

**Yêu cầu:**
- JWT token hợp lệ
- **Phân quyền:**
  - ✅ **Tác giả** (người tạo post) có thể cập nhật
  - ✅ **Admin** có thể cập nhật bất kỳ post nào

**Response khi không có quyền:**
```json
HTTP 403 Forbidden
{
  "error": "Unauthorized",
  "message": "Only the post author or admin can update this post"
}
```

### 5. Xóa Post
```http
DELETE /api/v1/post/{postId}
Authorization: Bearer <jwt_token>
```

**Yêu cầu:**
- JWT token hợp lệ
- **Phân quyền:**
  - ✅ **Tác giả** (người tạo post) có thể xóa
  - ✅ **Admin** có thể xóa bất kỳ post nào

**Response khi không có quyền:**
```json
HTTP 403 Forbidden
{
  "error": "Unauthorized",
  "message": "Only the post author or admin can delete this post"
}
```

## Phân Quyền Endpoints

| HTTP Method | Endpoint | Authentication | Authorization | Mô tả |
|------------|----------|----------------|---------------|-------|
| GET | `/api/v1/post` | ❌ Không | Public | Lấy tất cả posts |
| GET | `/api/v1/post/{id}` | ❌ Không | Public | Lấy post theo ID |
| POST | `/api/v1/post/create` | ✅ Có | Any authenticated user | Tạo post mới |
| PUT | `/api/v1/post/{id}` | ✅ Có | Author or Admin | Cập nhật post |
| DELETE | `/api/v1/post/{id}` | ✅ Có | Author or Admin | Xóa post |

### Chi tiết phân quyền:

#### Create Post (POST)
- **Ai có thể tạo**: Bất kỳ user đã đăng nhập
- **userId** tự động được gán từ JWT token

#### Update Post (PUT)
- **Tác giả** (người tạo post): ✅ Có thể update post của mình
- **Admin**: ✅ Có thể update bất kỳ post nào
- **User khác**: ❌ Không thể update post của người khác

#### Delete Post (DELETE)
- **Tác giả** (người tạo post): ✅ Có thể xóa post của mình
- **Admin**: ✅ Có thể xóa bất kỳ post nào
- **User khác**: ❌ Không thể xóa post của người khác

## Luồng Xác Thực (Authentication Flow)

1. **Đăng nhập để lấy JWT token:**
```http
POST {{api_gateway_url}}/api/v1/auth/user/token
Content-Type: application/json

{
  "username": "your_email@example.com",
  "password": "your_password"
}
```

Response sẽ trả về JWT token:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "authenticated": true
}
```

2. **Sử dụng token để tạo post:**
```http
POST {{api_gateway_url}}/api/v1/post/create
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "title": "My First Post",
  "content": "This is my first post!"
}
```

3. **API Gateway xử lý:**
   - Validate JWT token
   - Trích xuất `userId`, `username`, và `role` từ token
   - Thêm headers tự động:
     - `X-User-Id`: ID của user
     - `X-Username`: Username
     - `X-User-Role`: Role của user
   - Forward request đến Post Service

4. **Post Service nhận request:**
   - Nhận `userId` từ header `X-User-Id`
   - Tạo post với `userId` tương ứng
   - Trả về post đã được tạo

## Postman Collection

Import file `Vita_Backend_API_Collection.json` vào Postman để có sẵn tất cả các request đã được cấu hình.

**Cách sử dụng:**
1. Set biến môi trường:
   - `api_gateway_url`: http://localhost:8080
   - `jwt_token`: Token nhận được sau khi đăng nhập

2. Thực hiện Login để lấy token
3. Copy token vào biến `jwt_token`
4. Các request Create/Update/Delete Post sẽ tự động sử dụng token này

## Cấu trúc Database

```java
Post {
    String id;           // Auto-generated
    String title;        // Tiêu đề bài viết
    String content;      // Nội dung bài viết
    String userId;       // ID người tạo (tự động từ JWT)
    String updatedBy;    // ID người cập nhật cuối
    LocalDateTime createdAt;  // Thời gian tạo
    LocalDateTime updatedAt;  // Thời gian cập nhật
}
```

## Port Configuration

- **Post Service**: http://localhost:8083
- **API Gateway**: http://localhost:8080

**Lưu ý:** Luôn gọi API thông qua API Gateway (port 8080), không gọi trực tiếp đến Post Service.

## Testing Scenarios

### Scenario 1: User tạo và xóa post của mình
```bash
# 1. Login as normal user
TOKEN=$(curl -X POST http://localhost:8080/api/v1/auth/user/token \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com","password":"password123"}' | jq -r '.token')

# 2. Tạo post
POST_ID=$(curl -X POST http://localhost:8080/api/v1/post/create \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"My Post","content":"Content"}' | jq -r '.id')

# 3. Xóa post của mình - SUCCESS ✅
curl -X DELETE http://localhost:8080/api/v1/post/$POST_ID \
  -H "Authorization: Bearer $TOKEN"
```

### Scenario 2: User cố xóa post của người khác - FAILED ❌
```bash
# 1. Login as user A
TOKEN_A=$(curl -X POST http://localhost:8080/api/v1/auth/user/token \
  -H "Content-Type: application/json" \
  -d '{"username":"userA@example.com","password":"password"}' | jq -r '.token')

# 2. User A tạo post
POST_ID=$(curl -X POST http://localhost:8080/api/v1/post/create \
  -H "Authorization: Bearer $TOKEN_A" \
  -H "Content-Type: application/json" \
  -d '{"title":"User A Post","content":"Content"}' | jq -r '.id')

# 3. Login as user B
TOKEN_B=$(curl -X POST http://localhost:8080/api/v1/auth/user/token \
  -H "Content-Type: application/json" \
  -d '{"username":"userB@example.com","password":"password"}' | jq -r '.token')

# 4. User B cố xóa post của User A - FAILED ❌
curl -X DELETE http://localhost:8080/api/v1/post/$POST_ID \
  -H "Authorization: Bearer $TOKEN_B"

# Response: HTTP 403 Forbidden
# {
#   "error": "Unauthorized",
#   "message": "Only the post author or admin can delete this post"
# }
```

### Scenario 3: Admin xóa bất kỳ post nào - SUCCESS ✅
```bash
# 1. Login as normal user
TOKEN_USER=$(curl -X POST http://localhost:8080/api/v1/auth/user/token \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com","password":"password"}' | jq -r '.token')

# 2. User tạo post
POST_ID=$(curl -X POST http://localhost:8080/api/v1/post/create \
  -H "Authorization: Bearer $TOKEN_USER" \
  -H "Content-Type: application/json" \
  -d '{"title":"User Post","content":"Content"}' | jq -r '.id')

# 3. Login as admin
TOKEN_ADMIN=$(curl -X POST http://localhost:8080/api/v1/auth/user/token \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@vita.com","password":"admin123"}' | jq -r '.token')

# 4. Admin xóa post của user - SUCCESS ✅
curl -X DELETE http://localhost:8080/api/v1/post/$POST_ID \
  -H "Authorization: Bearer $TOKEN_ADMIN"

# Response: HTTP 204 No Content
```

## Error Handling

### Authorization Errors

**Khi user không có quyền update/delete:**
```json
HTTP 403 Forbidden
{
  "error": "Unauthorized",
  "message": "Only the post author or admin can delete this post"
}
```

**Khi không có JWT token:**
```json
HTTP 401 Unauthorized
{
  "error": "Missing or invalid Authorization header",
  "status": 401
}
```

**Khi post không tồn tại:**
```json
HTTP 404 Not Found
{
  "error": "Post not found",
  "message": "Post not found with id: {postId}"
}
```

