# DDoS Protection Guide - API Gateway

## 📋 Tổng Quan

API Gateway đã được trang bị hệ thống bảo vệ DDoS (Distributed Denial of Service) toàn diện với nhiều lớp bảo mật:

1. **IP Blocking** - Chặn IP độc hại
2. **Rate Limiting** - Giới hạn số lượng request
3. **Request Validation** - Kiểm tra tính hợp lệ của request
4. **Monitoring & Alerting** - Giám sát và cảnh báo tấn công
5. **Auto-blocking** - Tự động chặn IP có hành vi đáng ngờ

## 🛡️ Các Lớp Bảo Vệ

### 1. IP Blocking Filter (Lớp 1 - Cao nhất)
**Order: -300** (Chạy đầu tiên)

Chặn các IP độc hại trước khi xử lý request:
- **Whitelist**: Các IP tin cậy bỏ qua tất cả kiểm tra
- **Blacklist**: Các IP bị chặn vĩnh viễn
- **Runtime Blacklist**: Tự động chặn tạm thời các IP vi phạm
- **Violation Tracking**: Theo dõi số lần vi phạm của mỗi IP

**Cấu hình:**
```yaml
app:
  security:
    ip-blocking:
      enabled: true
      max-violations: 10        # Tự động chặn sau 10 lần vi phạm
      whitelist:
        - "127.0.0.1"           # Localhost
        - "::1"                 # IPv6 localhost
        - "10.0.0.1"            # Thêm các IP tin cậy
      blacklist:
        - "192.168.1.100"       # Thêm các IP độc hại
```

### 2. Request Validation Filter (Lớp 2)
**Order: -250**

Kiểm tra tính hợp lệ của request để phát hiện các cuộc tấn công:

**Các kiểm tra:**
- ✅ Kích thước request (mặc định: tối đa 10MB)
- ✅ Độ dài URL (mặc định: tối đa 2048 ký tự)
- ✅ Kích thước headers (mặc định: tối đa 8KB)
- ✅ Số lượng headers (mặc định: tối đa 50)
- ✅ Phát hiện các pattern đáng ngờ (SQL injection, XSS, etc.)
- ✅ Kiểm tra User-Agent (phát hiện bot/scanner độc hại)

**Cấu hình:**
```yaml
app:
  security:
    request-validation:
      enabled: true
      max-request-size: 10485760    # 10 MB
      max-url-length: 2048
      max-header-size: 8192         # 8 KB
      max-headers-count: 50
```

**Các pattern đáng ngờ được phát hiện:**
- SQL Injection: `union select`, `' or '1'='1`, `drop table`
- XSS: `<script`, `javascript:`, `onerror=`, `onload=`
- Path Traversal: `../`, `..\\`
- PHP Shell: `<?php`, `<?=`

**User-Agent đáng ngờ:**
- Scanner tools: `sqlmap`, `nikto`, `nmap`, `masscan`
- Penetration testing: `metasploit`, `burpsuite`, `havij`
- Security scanners: `acunetix`, `netsparker`, `zaproxy`

### 3. Rate Limiting Filter (Lớp 3)
**Order: -200**

Giới hạn số lượng request từ mỗi IP/user:

**Tính năng:**
- 📊 Rate limit khác nhau cho anonymous vs authenticated users
- 🧹 Auto cleanup để tránh memory leak
- 📈 Response headers với thông tin rate limit
- ⏱️ Sliding window algorithm (1 phút)

**Cấu hình:**
```yaml
app:
  rate-limit:
    anonymous:
      requests-per-minute: 60       # User chưa đăng nhập: 60 req/min
    authenticated:
      requests-per-minute: 200      # User đã đăng nhập: 200 req/min
    cleanup-interval-minutes: 5     # Cleanup mỗi 5 phút
```

**Response Headers:**
- `X-RateLimit-Limit`: Giới hạn tối đa
- `X-RateLimit-Remaining`: Số request còn lại
- `X-RateLimit-Reset`: Thời gian reset (Unix timestamp)

**Khi vượt giới hạn:**
```json
HTTP 429 Too Many Requests
{
  "error": "Rate limit exceeded",
  "message": "Too many requests. Limit: 60 requests per minute. Please try again later.",
  "retryAfter": 60
}
```

### 4. Authentication Filter (Lớp 4)
**Order: -100**

Xác thực JWT token và phân quyền.

## 📊 Monitoring & Management

### DDoS Protection Service

Service tập trung để giám sát và phát hiện tấn công:

**Metrics được theo dõi:**
- Total requests
- Blocked requests
- Rate-limited requests
- Active connections
- Suspicious IPs count
- Block rate percentage

**Heuristics phát hiện tấn công:**
1. Block rate > 10%
2. Số lượng suspicious IPs > 100
3. Số request bị block > 1000

**Severity Levels:**
- `NORMAL`: Không có tấn công
- `LOW`: 10-20% block rate
- `MEDIUM`: 20-30% block rate
- `HIGH`: 30-50% block rate
- `CRITICAL`: > 50% block rate

### Scheduled Tasks

1. **Cleanup Old Data** (Mỗi 5 phút)
   - Xóa suspicious IPs cũ (> 1 giờ)
   - Xóa blocked IPs tracking (> 30 phút)

2. **Monitor and Alert** (Mỗi 1 phút)
   - Kiểm tra tấn công
   - Log statistics khi phát hiện tấn công
   - Có thể tích hợp với hệ thống alert (Email, Slack, etc.)

## 🔧 Management APIs

### 1. Xem Statistics
```http
GET /api/v1/admin/ddos-protection/statistics
```

Response:
```json
{
  "totalRequests": 10000,
  "blockedRequests": 150,
  "rateLimitedRequests": 200,
  "activeConnections": 50,
  "suspiciousIpsCount": 15,
  "recentBlockedIpsCount": 5,
  "blockRate": 1.5,
  "timestamp": "2025-10-28T10:30:00"
}
```

### 2. Xem Top Suspicious IPs
```http
GET /api/v1/admin/ddos-protection/suspicious-ips?limit=20
```

Response:
```json
{
  "192.168.1.100": 500,
  "10.0.0.50": 350,
  "172.16.0.10": 200
}
```

### 3. Kiểm Tra Trạng Thái
```http
GET /api/v1/admin/ddos-protection/status
```

Response:
```json
{
  "underAttack": true,
  "severity": "HIGH",
  "statistics": { ... }
}
```

### 4. Block IP Thủ Công
```http
POST /api/v1/admin/ddos-protection/block-ip?ip=192.168.1.100
```

Response:
```json
{
  "message": "IP blocked successfully",
  "ip": "192.168.1.100"
}
```

### 5. Unblock IP
```http
POST /api/v1/admin/ddos-protection/unblock-ip?ip=192.168.1.100
```

### 6. Kiểm Tra IP
```http
GET /api/v1/admin/ddos-protection/check-ip?ip=192.168.1.100
```

Response:
```json
{
  "ip": "192.168.1.100",
  "blocked": true,
  "violations": 15
}
```

### 7. Reset Statistics
```http
POST /api/v1/admin/ddos-protection/reset-statistics
```

⚠️ **Lưu ý**: Chỉ reset khi cần thiết (sau khi xử lý xong tấn công)

## 🔐 Bảo Mật Endpoints

Các endpoint quản lý DDoS protection nên được bảo vệ bởi admin role:

```java
// Trong SecurityConfig hoặc tương tự
.pathMatchers("/api/v1/admin/ddos-protection/**").hasRole("ADMIN")
```

## ⚙️ Cấu Hình Nâng Cao

### Connection Limits
```yaml
server:
  netty:
    connection-timeout: 10s
    idle-timeout: 30s
  http2:
    enabled: true
  compression:
    enabled: true

spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 5000
        response-timeout: 10s
        pool:
          type: elastic
          max-connections: 500      # Giới hạn concurrent connections
          max-idle-time: 30s
          max-life-time: 60s
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
        permittedNumberOfCallsInHalfOpenState: 3
```

### Logging
```yaml
logging:
  level:
    com.vita.api_gateway.filter: INFO
    com.vita.api_gateway.service: INFO
  file:
    name: logs/api-gateway.log
    max-size: 10MB
    max-history: 30
```

## 📈 Best Practices

### 1. Whitelist Configuration
- Thêm IP của các service nội bộ vào whitelist
- Thêm IP của load balancer/reverse proxy
- Thêm IP của monitoring tools

### 2. Rate Limit Tuning
- Điều chỉnh dựa trên traffic thực tế
- Anonymous users: 30-60 requests/minute
- Authenticated users: 100-300 requests/minute
- Admin users: Có thể cao hơn hoặc whitelist

### 3. Monitoring
- Theo dõi statistics thường xuyên
- Set up alerts khi `blockRate > 5%`
- Log suspicious IPs để phân tích
- Review và update blacklist định kỳ

### 4. Response to Attack
Khi phát hiện tấn công:
1. Check statistics và severity
2. Review top suspicious IPs
3. Block các IP độc hại nếu cần
4. Tăng rate limit nếu là legitimate traffic spike
5. Giảm rate limit nếu đang bị tấn công

### 5. Testing
Test DDoS protection:
```bash
# Test rate limiting
for i in {1..100}; do
  curl -X GET http://localhost:8080/api/v1/post
done

# Bạn sẽ nhận được HTTP 429 sau khi vượt limit
```

## 🚨 Incident Response

### Khi bị tấn công DDoS:

1. **Xác nhận tấn công:**
   ```bash
   curl http://localhost:8080/api/v1/admin/ddos-protection/status
   ```

2. **Xem top IPs đáng ngờ:**
   ```bash
   curl http://localhost:8080/api/v1/admin/ddos-protection/suspicious-ips?limit=50
   ```

3. **Block các IP độc hại:**
   ```bash
   curl -X POST "http://localhost:8080/api/v1/admin/ddos-protection/block-ip?ip=IP_ADDRESS"
   ```

4. **Giảm rate limit tạm thời** (trong application.yaml):
   ```yaml
   app:
     rate-limit:
       anonymous:
         requests-per-minute: 20  # Giảm từ 60 xuống 20
   ```

5. **Monitor và điều chỉnh:**
   - Theo dõi block rate
   - Review logs
   - Cập nhật blacklist

6. **Sau khi kết thúc tấn công:**
   - Khôi phục rate limit bình thường
   - Review và permanent block các IP độc hại
   - Document incident để cải thiện

## 📝 Logs

Các log quan trọng cần theo dõi:

```
# IP bị block
WARN  - Blocked blacklisted IP: 192.168.1.100 attempting to access: /api/v1/post

# Rate limit exceeded
WARN  - Rate limit exceeded for client: ANON:192.168.1.100 (IP: 192.168.1.100, Type: anonymous, Count: 65)

# Suspicious request
WARN  - Suspicious headers detected from IP 192.168.1.100

# Attack detected
ERROR - ALERT: Possible DDoS attack detected! Severity: HIGH

# Auto-block
ERROR - IP auto-blocked due to excessive violations: 192.168.1.100 (Violations: 15)
```

## 🔍 Troubleshooting

### Legitimate users bị block?
1. Check violations: `/check-ip?ip=X.X.X.X`
2. Unblock: `/unblock-ip?ip=X.X.X.X`
3. Thêm vào whitelist nếu là trusted IP

### Rate limit quá thấp?
- Tăng `requests-per-minute` trong config
- Phân biệt rõ anonymous vs authenticated
- Consider whitelist cho power users

### Memory usage cao?
- Check cleanup interval (mặc định: 5 phút)
- Giảm cleanup interval xuống 2-3 phút
- Review số lượng IPs trong rateLimitMap

## 🎯 Production Checklist

- [ ] Đã configure whitelist với các IP tin cậy
- [ ] Đã test rate limiting với realistic traffic
- [ ] Đã setup monitoring alerts
- [ ] Đã document incident response process
- [ ] Đã backup config trước khi deploy
- [ ] Đã test recovery từ attack simulation
- [ ] Đã integrate với logging/monitoring system
- [ ] Đã secure admin endpoints với proper authentication
- [ ] Đã review và tune tất cả thresholds
- [ ] Đã prepare communication plan cho users khi có incident

## 📚 References

- [OWASP DDoS Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Denial_of_Service_Cheat_Sheet.html)
- [Spring Cloud Gateway Documentation](https://spring.io/projects/spring-cloud-gateway)
- [Resilience4j Documentation](https://resilience4j.readme.io/)

---

**Note**: DDoS protection là một quá trình liên tục. Cần monitor, review và update configuration thường xuyên dựa trên traffic patterns và threat landscape.

