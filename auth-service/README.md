# 🚀 Hướng dẫn Setup Admin Account Tự động - Auth Service

## 📋 Tổng quan

Auth Service đã được cấu hình để **tự động tạo tài khoản ADMIN** khi service khởi động. Hệ thống sẽ:

1. ✅ Tự động tạo role `ADMIN` nếu chưa có
2. ✅ Tự động tạo user admin với email và password từ biến môi trường
3. ✅ Sử dụng BCryptPasswordEncoder để mã hóa mật khẩu
4. ✅ Không log mật khẩu thô (chỉ log thông tin cần thiết)

## 🏗️ Kiến trúc

### **Entities:**
- **User**: `id`, `email`, `password`, `enabled`, `roles`
- **Role**: `id`, `name`, `description`

### **Repositories:**
- **UserRepository**: Quản lý user với các method tìm kiếm theo role
- **RoleRepository**: Quản lý role

### **Services:**
- **AdminInitializationService**: Tự động tạo admin khi khởi động

### **Controllers:**
- **AdminController**: API endpoints để kiểm tra admin

## 🔧 Cấu hình

### **Biến môi trường (Environment Variables):**
```bash
# Database
DBMS_CONNECTION=jdbc:mysql://localhost:3306/auth_service_db
DBMS_USERNAME=root
DBMS_PASSWORD=password

# Admin Account
DEFAULT_ADMIN_EMAIL=admin@vita.com
DEFAULT_ADMIN_PASSWORD=admin123

# JWT
JWT_SIGNER_KEY=your-secret-key
JWT_VALID_DURATION=3600
JWT_REFRESHABLE_DURATION=36000
```

### **Giá trị mặc định (Dev Environment):**
```yaml
app:
  admin:
    email: admin@vita.com
    password: admin123
    role: ADMIN
```

## 🚀 Cách hoạt động

### **1. Khi khởi động service:**
```
AdminInitializationService.run() được gọi
↓
Kiểm tra role ADMIN có tồn tại không
↓
Nếu chưa có → Tạo role ADMIN
↓
Kiểm tra user admin có tồn tại không
↓
Nếu chưa có → Tạo user admin với role ADMIN
```

### **2. Logs khi khởi động:**
```
[main] INFO AdminInitializationService - Starting admin account initialization...
[main] INFO AdminInitializationService - Checking if ADMIN role exists...
[main] INFO AdminInitializationService - Creating ADMIN role...
[main] INFO AdminInitializationService - ADMIN role created successfully with ID: uuid-here
[main] INFO AdminInitializationService - Checking if admin user exists with email: admin@vita.com
[main] INFO AdminInitializationService - Creating admin user...
[main] INFO AdminInitializationService - Admin user created successfully with ID: uuid-here and email: admin@vita.com
[main] INFO AdminInitializationService - Admin account ready - Email: admin@vita.com, Role: ADMIN
[main] INFO AdminInitializationService - Admin account initialization completed successfully
```

## 🔗 API Endpoints

### **Kiểm tra Admin Account:**
```bash
GET /auth/api/admin/check
Authorization: Bearer <jwt-token>
```

**Response:**
```json
{
  "result": {
    "adminExists": true,
    "adminEmail": "admin@vita.com",
    "message": "Admin account exists"
  },
  "message": "Admin check completed successfully"
}
```

### **Liệt kê Admin Users:**
```bash
GET /auth/api/admin/list
Authorization: Bearer <jwt-token>
```

**Response:**
```json
{
  "result": [
    {
      "id": "uuid-here",
      "email": "admin@vita.com",
      "enabled": true,
      "roles": ["ADMIN"]
    }
  ],
  "message": "Admin users retrieved successfully"
}
```

## 🛡️ Bảo mật

### **Password Encoding:**
- Sử dụng **BCryptPasswordEncoder** với strength mặc định (10)
- Mật khẩu được mã hóa trước khi lưu vào database
- Không bao giờ log mật khẩu thô

### **Role-based Access Control:**
- Endpoint `/api/admin/**` yêu cầu role `ADMIN`
- Sử dụng `@PreAuthorize("hasRole('ADMIN')")` annotation
- Spring Security xử lý authorization

### **Database Security:**
- Mật khẩu database được đọc từ biến môi trường
- Không hardcode credentials trong code

## 🗄️ Database Schema

### **Users Table:**
```sql
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);
```

### **Roles Table:**
```sql
CREATE TABLE roles (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(500)
);
```

### **User Roles Table:**
```sql
CREATE TABLE user_roles (
    user_id VARCHAR(36),
    role_id VARCHAR(36),
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

## 🚀 Deployment

### **1. Setup Database:**
```sql
CREATE DATABASE auth_service_db;
USE auth_service_db;
```

### **2. Environment Variables:**
```bash
export DBMS_CONNECTION="jdbc:mysql://localhost:3306/auth_service_db"
export DBMS_USERNAME="root"
export DBMS_PASSWORD="your-password"
export DEFAULT_ADMIN_EMAIL="admin@company.com"
export DEFAULT_ADMIN_PASSWORD="secure-password-123"
```

### **3. Khởi động Service:**
```bash
cd auth-service
mvn spring-boot:run
```

### **4. Kiểm tra:**
```bash
# Health check
curl http://localhost:8082/auth/actuator/health

# Check admin (cần JWT token)
curl -H "Authorization: Bearer <jwt-token>" \
     http://localhost:8082/auth/api/admin/check
```

## 🧪 Testing

### **Unit Tests:**
```java
@SpringBootTest
class AdminInitializationServiceTest {
    
    @Test
    void shouldCreateAdminRoleWhenNotExists() {
        // Test logic
    }
    
    @Test
    void shouldCreateAdminUserWhenNotExists() {
        // Test logic
    }
}
```

### **Integration Tests:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminControllerIntegrationTest {
    
    @Test
    void shouldReturnAdminStatus() {
        // Test API endpoint
    }
}
```

## 🔍 Monitoring & Debugging

### **Logs quan trọng:**
- Admin initialization process
- Role creation
- User creation
- Database operations
- Security events

### **Health Checks:**
- Database connectivity
- Service status
- Admin account status

### **Metrics:**
- Admin account creation success rate
- Database operation latency
- Authentication success/failure rates

## 🛠️ Troubleshooting

### **Lỗi thường gặp:**

1. **Database Connection Failed:**
   ```
   Failed to initialize admin account: Communications link failure
   ```
   - **Giải pháp:** Kiểm tra database connection string và credentials

2. **Role Already Exists:**
   ```
   ADMIN role already exists, skipping creation
   ```
   - **Giải pháp:** Đây là behavior bình thường, không cần lo lắng

3. **User Already Exists:**
   ```
   Admin user already exists, skipping creation
   ```
   - **Giải pháp:** Đây là behavior bình thường, không cần lo lắng

4. **Permission Denied:**
   ```
   Access Denied for user 'root'@'localhost'
   ```
   - **Giải pháp:** Kiểm tra database user permissions

### **Debug Mode:**
```yaml
logging:
  level:
    com.vita.auth_service: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

## 📈 Performance

### **Optimizations:**
- Sử dụng `@Transactional` cho database operations
- Lazy loading cho user roles (nếu cần)
- Connection pooling với HikariCP
- JPA query optimization

### **Expected Performance:**
- Admin initialization: < 1 second
- Database operations: < 100ms
- API responses: < 50ms

## 🔄 Customization

### **Thay đổi Admin Role Name:**
```yaml
app:
  admin:
    role: SUPER_ADMIN
```

### **Thêm nhiều Admin Users:**
```java
@Value("${app.admin.emails:admin1@company.com,admin2@company.com}")
private List<String> adminEmails;
```

### **Custom Password Policy:**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12); // Higher strength
}
```

## 🎉 Kết quả

Sau khi setup thành công:
- ✅ Admin account được tạo tự động khi khởi động
- ✅ Mật khẩu được mã hóa an toàn với BCrypt
- ✅ Role-based access control hoạt động
- ✅ API endpoints sẵn sàng để kiểm tra admin
- ✅ Hệ thống sẵn sàng cho production

**Happy coding! 🚀**
