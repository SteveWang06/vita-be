# API Gateway - Vita Backend

## 🌟 Tổng quan
API Gateway là cửa ngõ duy nhất cho tất cả các request từ client đến các microservices. Gateway này đã được trang bị hệ thống bảo vệ DDoS toàn diện và các tính năng bảo mật, hiệu suất cao.

## 🚀 Các tính năng chính

### 1. **Bảo mật (Security)**
- ✅ **JWT Token Validation**: Validate JWT format local trước khi gọi auth service
- ✅ **Security Headers**: Thêm các security headers để bảo vệ khỏi XSS, clickjacking
- ✅ **CORS Configuration**: Cấu hình CORS policy để kiểm soát domain truy cập
- ✅ **Public Endpoints**: Định nghĩa rõ ràng các endpoint không cần authentication

### 2. **🛡️ DDoS Protection (NEW!)**
- ✅ **IP Blocking**: Blacklist/Whitelist với auto-blocking cho IPs vi phạm
- ✅ **Enhanced Rate Limiting**: Giới hạn khác nhau cho anonymous vs authenticated users
- ✅ **Request Validation**: Kiểm tra size, headers, và phát hiện patterns đáng ngờ
- ✅ **Attack Detection**: Monitoring và alerting tấn công real-time
- ✅ **Auto-cleanup**: Tự động dọn dẹp để tránh memory leak
- ✅ **Admin APIs**: Interface quản lý DDoS protection

**📖 Chi tiết: [DDOS_PROTECTION.md](DDOS_PROTECTION.md)**

### 3. **Hiệu suất (Performance)**
- ✅ **Token Caching**: Cache kết quả validation token để giảm latency
- ✅ **Circuit Breaker**: Xử lý khi auth service không khả dụng
- ✅ **Connection Pooling**: Quản lý connection pool hiệu quả
- ✅ **Async Processing**: Sử dụng reactive programming với WebFlux

### 4. **Monitoring & Observability**
- ✅ **Health Check**: Endpoint kiểm tra trạng thái service
- ✅ **DDoS Statistics**: Metrics chi tiết về attacks và blocks
- ✅ **Cache Statistics**: Theo dõi hiệu suất cache
- ✅ **Structured Logging**: Log có cấu trúc với SLF4J

## 🏗️ Architecture

### Filter Chain (theo thứ tự)
1. **IP Blocking Filter** (-300): Block malicious IPs
2. **Request Validation Filter** (-250): Validate request patterns
3. **Rate Limiting Filter** (-200): Limit requests per IP/user
4. **Authentication Filter** (-100): Validate JWT tokens

## 🔧 Cấu hình

### Enhanced Rate Limiting
```yaml
app:
  rate-limit:
    anonymous:
      requests-per-minute: 60        # Anonymous users
    authenticated:
      requests-per-minute: 200       # Authenticated users
    cleanup-interval-minutes: 5
```

### IP Blocking
```yaml
app:
  security:
    ip-blocking:
      enabled: true
      max-violations: 10
      whitelist:
        - "127.0.0.1"
        - "::1"
      blacklist: []
```

### Request Validation
```yaml
app:
  security:
    request-validation:
      enabled: true
      max-request-size: 10485760    # 10 MB
      max-url-length: 2048
      max-header-size: 8192
      max-headers-count: 50
```

### Circuit Breaker
```yaml
resilience4j:
  circuitbreaker:
    instances:
      authService:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 10000
```

### Cache
```yaml
app:
  cache:
    token-ttl: 300 # 5 minutes
```

## 📊 API Endpoints

### Routes to Microservices
| Path | Target Service | Port | Auth Required |
|------|---------------|------|---------------|
| `/api/v1/auth/**` | Auth Service | 8082 | ❌ No |
| `/api/v1/user/**` | User Service | 8081 | ✅ Yes |
| `/api/v1/post/create` | Post Service | 8083 | ✅ Yes |
| `/api/v1/post` (GET) | Post Service | 8083 | ❌ No |

### Health & Monitoring
- `GET /actuator/health` - Health check
- `GET /actuator/info` - Service information
- `GET /actuator/cache-stats` - Cache statistics

### DDoS Protection Management
- `GET /api/v1/admin/ddos-protection/statistics` - Xem statistics
- `GET /api/v1/admin/ddos-protection/status` - Kiểm tra attack status
- `GET /api/v1/admin/ddos-protection/suspicious-ips` - Top suspicious IPs
- `POST /api/v1/admin/ddos-protection/block-ip` - Block IP
- `POST /api/v1/admin/ddos-protection/unblock-ip` - Unblock IP
- `GET /api/v1/admin/ddos-protection/check-ip` - Kiểm tra IP status

## 🛡️ DDoS Protection Features

### 1. IP Blocking
- **Whitelist**: Trusted IPs bypass tất cả checks
- **Blacklist**: Permanently blocked IPs
- **Auto-blocking**: Tự động block sau 10 violations
- **Runtime blocking**: Temporary blocks

### 2. Rate Limiting
- **Anonymous**: 60 requests/minute
- **Authenticated**: 200 requests/minute
- **Auto cleanup**: Mỗi 5 phút
- **Response headers**: Rate limit info trong response

### 3. Request Validation
- Size limits (10MB max)
- URL length validation (2048 chars)
- Header validation (8KB total, max 50 headers)
- Suspicious pattern detection:
  - SQL injection attempts
  - XSS attacks
  - Path traversal
  - Malicious user-agents

### 4. Attack Detection
- Real-time monitoring
- Automatic severity assessment
- Alert generation
- Statistics tracking

## 📈 Monitoring

### DDoS Statistics
```bash
curl http://localhost:8080/api/v1/admin/ddos-protection/statistics
```

Response:
```json
{
  "totalRequests": 10000,
  "blockedRequests": 150,
  "rateLimitedRequests": 200,
  "activeConnections": 50,
  "blockRate": 1.5,
  "timestamp": "2025-10-28T10:30:00"
}
```

### Check Attack Status
```bash
curl http://localhost:8080/api/v1/admin/ddos-protection/status
```

Response:
```json
{
  "underAttack": false,
  "severity": "NORMAL",
  "statistics": { ... }
}
```

### View Suspicious IPs
```bash
curl http://localhost:8080/api/v1/admin/ddos-protection/suspicious-ips?limit=20
```

## 🚨 Incident Response

### Khi phát hiện tấn công:

1. **Check status:**
   ```bash
   curl http://localhost:8080/api/v1/admin/ddos-protection/status
   ```

2. **Xem top suspicious IPs:**
   ```bash
   curl http://localhost:8080/api/v1/admin/ddos-protection/suspicious-ips?limit=50
   ```

3. **Block malicious IPs:**
   ```bash
   curl -X POST "http://localhost:8080/api/v1/admin/ddos-protection/block-ip?ip=192.168.1.100"
   ```

4. **Monitor và adjust rate limits nếu cần**

## 🔍 Security Features

### JWT Validation Flow
1. Client gửi request với JWT token
2. Gateway validate JWT format locally
3. Nếu format hợp lệ, gọi auth service để validate
4. Cache kết quả validation
5. Thêm user info vào request headers

### Security Headers
- `X-Frame-Options: DENY` - Chống clickjacking
- `X-Content-Type-Options: nosniff` - Chống MIME sniffing
- `X-XSS-Protection: 1; mode=block` - Chống XSS
- `Content-Security-Policy: default-src 'self'` - CSP policy

### Rate Limit Headers
- `X-RateLimit-Limit`: Giới hạn tối đa
- `X-RateLimit-Remaining`: Số request còn lại
- `X-RateLimit-Reset`: Thời gian reset

## ⚡ Performance Features

### Caching Strategy
- **Token Cache**: Cache validation result trong 5 phút
- **User Info Cache**: Cache thông tin user
- **Rate Limit Cache**: In-memory với auto cleanup

### Circuit Breaker Pattern
- **Closed State**: Hoạt động bình thường
- **Open State**: Block tất cả calls khi failure rate > 50%
- **Half-Open State**: Cho phép một số calls để test recovery

### Connection Management
```yaml
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          max-connections: 500
          max-idle-time: 30s
          max-life-time: 60s
```

## 🧪 Testing

### Test Rate Limiting (Anonymous)
```bash
# Should receive HTTP 429 after 60 requests
for i in {1..100}; do
  curl -X GET http://localhost:8080/api/v1/post
done
```

### Test with Authentication
```bash
# Login first
TOKEN=$(curl -X POST http://localhost:8080/api/v1/auth/user/token \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@vita.com","password":"admin123"}' | jq -r '.token')

# Should receive HTTP 429 after 200 requests
for i in {1..250}; do
  curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/post
done
```

### Test IP Blocking
```bash
# Manually block an IP
curl -X POST "http://localhost:8080/api/v1/admin/ddos-protection/block-ip?ip=192.168.1.100"

# Try to access from that IP - should receive HTTP 403
```

## 🚀 Deployment

### Requirements
- Java 21+
- Spring Boot 3.4.5+
- Spring Cloud 2024.0.0+

### Running
```bash
# Development
mvn spring-boot:run

# Production
mvn clean package
java -jar target/api-gateway-0.0.1-SNAPSHOT.jar
```

Gateway will start on **port 8080**

### Environment Variables
```bash
# Database (if needed)
DBMS_CONNECTION=jdbc:mysql://localhost:3306/auth_service_db
DBMS_USERNAME=root
DBMS_PASSWORD=password

# JWT
JWT_SIGNER_KEY=your-secret-key
JWT_VALID_DURATION=3600
JWT_REFRESHABLE_DURATION=36000
```

## 📝 Logs

Logs được lưu tại:
- **Console**: Real-time output
- **File**: `logs/api-gateway.log` (Max 10MB, 30 days retention)

Important log patterns:
- `Rate limit exceeded` - Rate limiting triggered
- `Blocked blacklisted IP` - IP blocking triggered
- `Suspicious headers detected` - Request validation triggered
- `ALERT: Possible DDoS attack` - Attack detection triggered
- `IP auto-blocked due to excessive violations` - Auto-blocking triggered

## 📈 Performance Metrics

### Expected Improvements
- **Latency**: Giảm 60-80% nhờ token caching
- **Throughput**: Tăng 3-5x nhờ async processing
- **Reliability**: 99.9% uptime nhờ circuit breaker
- **Security**: Bảo vệ toàn diện khỏi DDoS attacks

### Monitoring Points
- Response time per endpoint
- Cache hit/miss ratio
- Circuit breaker state changes
- Rate limiting violations
- Authentication success/failure rates
- DDoS attack detection and mitigation
- IP blocking statistics

## 🛠️ Troubleshooting

### Legitimate users bị block?
```bash
# Check violations
curl "http://localhost:8080/api/v1/admin/ddos-protection/check-ip?ip=X.X.X.X"

# Unblock
curl -X POST "http://localhost:8080/api/v1/admin/ddos-protection/unblock-ip?ip=X.X.X.X"
```

### Rate limit quá thấp?
- Tăng `requests-per-minute` trong `application.yaml`
- Restart service để apply changes
- Consider whitelist cho trusted IPs

### Memory usage cao?
- Check số lượng tracked IPs
- Giảm cleanup interval
- Review whitelist/blacklist configuration

## 📚 Documentation

- **[DDOS_PROTECTION.md](DDOS_PROTECTION.md)**: Complete DDoS protection guide
- **[application.yaml](src/main/resources/application.yaml)**: Full configuration reference

## 🎯 Production Checklist

- [ ] Configure whitelist với trusted IPs
- [ ] Test rate limiting với realistic traffic
- [ ] Setup monitoring alerts
- [ ] Secure admin endpoints
- [ ] Review và tune thresholds
- [ ] Configure proper logging
- [ ] Setup backup và recovery procedures
- [ ] Document incident response process

## 🤝 Contributing

When adding new features:
1. Update configuration in `application.yaml`
2. Add appropriate filters with correct order
3. Update documentation
4. Add tests
5. Review security implications

---

**Gateway Port**: 8080  
**Spring Boot Version**: 3.4.5  
**Spring Cloud Version**: 2024.0.0
