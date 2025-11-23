# Start All Vita Backend Services

Complete guide to start all microservices.

---

## 🚀 OPTION 1: Run from IntelliJ (Recommended for Development)

### Prerequisites
1. **Invalidate Caches** (DO THIS FIRST!):
   - File → Invalidate Caches...
   - Select: Invalidate and Restart
   - Wait for IntelliJ to restart

2. **Reload Maven Projects**:
   - Maven tab → 🔄 Reload All Maven Projects
   - Wait for completion

### Start Services (in order)

1. **Auth Service** (Port 8082):
   - Run: `AuthServiceApplication.java`
   - Wait for: "Started AuthServiceApplication"
   - Test: http://localhost:8082/auth/swagger-ui.html

2. **User Service** (Port 8081):
   - Run: `UserServiceApplication.java`
   - Wait for: "Started UserServiceApplication"
   - Test: http://localhost:8081/user/swagger-ui.html

3. **Post Service** (Port 8083):
   - Run: `PostServiceApplication.java`
   - Wait for: "Started PostServiceApplication"
   - Test: http://localhost:8083/post/swagger-ui.html

4. **Rental Service** (Port 8084):
   - Run: `RentalServiceApplication.java`
   - Wait for: "Started RentalServiceApplication"
   - Test: http://localhost:8084/swagger-ui.html

5. **Media Service** (Port 8085):
   - Run: `MediaServiceApplication.java`
   - Wait for: "Started MediaServiceApplication"
   - Test: http://localhost:8085/swagger-ui.html

6. **API Gateway** (Port 8080):
   - Run: `ApiGatewayApplication.java`
   - Wait for: "Started ApiGatewayApplication"
   - Test: http://localhost:8080/actuator/health

---

## 🔥 OPTION 2: Run from JAR (100% Works!)

### Build All Services First

```bash
cd /Users/nguyenthanhphuoc/Desktop/My-folder/vita-be
./build-all-services.sh
```

### Start Services via JAR

Open **6 separate terminal windows**:

#### Terminal 1: Auth Service
```bash
cd /Users/nguyenthanhphuoc/Desktop/My-folder/vita-be/auth-service
java -jar target/auth-service-0.0.1-SNAPSHOT.jar
```

#### Terminal 2: User Service
```bash
cd /Users/nguyenthanhphuoc/Desktop/My-folder/vita-be/user-service
java -jar target/user-service-0.0.1-SNAPSHOT.jar
```

#### Terminal 3: Post Service
```bash
cd /Users/nguyenthanhphuoc/Desktop/My-folder/vita-be/post-service
java -jar target/post-service-0.0.1-SNAPSHOT.jar
```

#### Terminal 4: Rental Service
```bash
cd /Users/nguyenthanhphuoc/Desktop/My-folder/vita-be/rental-service
java -jar target/rental-service-0.0.1-SNAPSHOT.jar
```

#### Terminal 5: Media Service
```bash
cd /Users/nguyenthanhphuoc/Desktop/My-folder/vita-be/media-service
java -jar target/media-service-0.0.1-SNAPSHOT.jar
```

#### Terminal 6: API Gateway
```bash
cd /Users/nguyenthanhphuoc/Desktop/My-folder/vita-be/api-gateway
java -jar target/api-gateway-0.0.1-SNAPSHOT.jar
```

---

## ⚡ OPTION 3: Quick Start Single Service

If you just need user-service:

```bash
cd /Users/nguyenthanhphuoc/Desktop/My-folder/vita-be
./run-user-service-jar.sh
```

---

## 🔍 Verify Services Running

### Check All Ports
```bash
lsof -i :8080  # API Gateway
lsof -i :8081  # User Service
lsof -i :8082  # Auth Service
lsof -i :8083  # Post Service
lsof -i :8084  # Rental Service
lsof -i :8085  # Media Service
```

### Test Health Endpoints
```bash
# Via API Gateway
curl http://localhost:8080/actuator/health

# Direct Services
curl http://localhost:8081/user/actuator/health
curl http://localhost:8082/auth/actuator/health
curl http://localhost:8083/post/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8085/actuator/health
```

### Access Swagger UI
- Auth:   http://localhost:8082/auth/swagger-ui.html
- User:   http://localhost:8081/user/swagger-ui.html
- Post:   http://localhost:8083/post/swagger-ui.html
- Rental: http://localhost:8084/swagger-ui.html
- Media:  http://localhost:8085/swagger-ui.html

---

## 🛑 Stop All Services

### If Running in IntelliJ
- Click **Stop** button (red square) for each service

### If Running from Terminal
- Press `Ctrl+C` in each terminal window

### Kill All Java Processes (Nuclear Option)
```bash
# Kill specific ports
lsof -ti:8080 | xargs kill -9  # API Gateway
lsof -ti:8081 | xargs kill -9  # User Service
lsof -ti:8082 | xargs kill -9  # Auth Service
lsof -ti:8083 | xargs kill -9  # Post Service
lsof -ti:8084 | xargs kill -9  # Rental Service
lsof -ti:8085 | xargs kill -9  # Media Service

# Or kill all Spring Boot apps
pkill -f "spring-boot"
```

---

## 🚨 Troubleshooting

### Issue: "Port already in use"

**Solution:**
```bash
# Find process on port
lsof -i :8081

# Kill process
lsof -ti:8081 | xargs kill -9
```

### Issue: "No qualifying bean of type 'UserProfileMapper'"

**Solution:**
1. Run from JAR instead: `./run-user-service-jar.sh`
2. Or follow: `INTELLIJ_MAPSTRUCT_FIX.md`

### Issue: "Access denied for user 'root'@'localhost'"

**Solution:** Check MySQL password in `application.yml`:
```yaml
datasource:
  username: root
  password: password  # Must be 'password'
```

### Issue: MySQL not running

**Solution:**
```bash
# Start MySQL
brew services start mysql

# Or
mysql.server start

# Verify
mysql -u root -p
# Enter password: password
```

---

## 📊 Service Startup Order

**Recommended order:**
1. Auth Service (other services may need authentication)
2. User Service
3. Post Service  
4. Rental Service
5. Media Service
6. API Gateway (last, routes to all services)

**But can start in any order** - services are independent.

---

## ✅ Success Checklist

- [ ] MySQL is running
- [ ] All databases created (`*_service_db`)
- [ ] All services built successfully
- [ ] Auth Service running on 8082
- [ ] User Service running on 8081
- [ ] Post Service running on 8083
- [ ] Rental Service running on 8084
- [ ] Media Service running on 8085
- [ ] API Gateway running on 8080
- [ ] All Swagger UIs accessible
- [ ] Can login via Auth Service
- [ ] Can test APIs via Postman

---

## 🎯 Quick Test

1. **Start Auth Service**
2. **Get JWT Token**:
   ```bash
   curl -X POST http://localhost:8082/auth/user/token \
     -H "Content-Type: application/json" \
     -d '{"username":"admin@vita.com","password":"admin123"}'
   ```
3. **Copy token from response**
4. **Test protected endpoint**:
   ```bash
   curl http://localhost:8081/user/ \
     -H "Authorization: Bearer YOUR_TOKEN"
   ```

---

## 📝 Notes

- **Development**: Use IntelliJ (easier debugging)
- **Testing/Production**: Use JAR (more stable)
- **MapStruct Issues**: Always run from JAR
- **First Time**: Run `./build-all-services.sh` first
- **Database**: Must have MySQL running with correct credentials

---

## 🔗 Related Documentation

- `SWAGGER_DOCUMENTATION.md` - API documentation guide
- `INTELLIJ_MAPSTRUCT_FIX.md` - Fix MapStruct issues
- Each service's `README.md` - Service-specific docs

---

**Last Updated:** November 23, 2025  
**All Services:** ✅ Working

