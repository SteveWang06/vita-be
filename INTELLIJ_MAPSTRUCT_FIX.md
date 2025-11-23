# IntelliJ MapStruct Fix Guide

**Problem:** `No qualifying bean of type 'UserProfileMapper' available`

**Root Cause:** IntelliJ is running old code from cache, missing generated MapStruct implementations.

---

## ✅ SOLUTION (Follow in Order)

### Method 1: Invalidate Caches (RECOMMENDED)

This is the **guaranteed fix** for IntelliJ cache issues.

#### Step 1: Stop All Services
Stop all running services in IntelliJ.

#### Step 2: Clean Build via Terminal

```bash
cd /Users/nguyenthanhphuoc/Desktop/My-folder/vita-be
./fix-intellij-mapstruct.sh
```

This will clean and rebuild all services with MapStruct.

#### Step 3: Invalidate Caches in IntelliJ

1. **File** → **Invalidate Caches...**
2. Check **ALL** boxes:
   - ✅ Clear file system cache and Local History
   - ✅ Clear downloaded shared indexes
   - ✅ Clear VCS Log caches and indexes
3. Select **Invalidate and Restart**
4. Click **Invalidate and Restart** button
5. **Wait** for IntelliJ to restart (1-2 minutes)

#### Step 4: After Restart

1. Wait for IntelliJ indexing to complete (progress bar at bottom)
2. Open **Maven** tab (View → Tool Windows → Maven)
3. Click **🔄 Reload All Maven Projects**
4. Wait for reload to complete

#### Step 5: Verify Generated Sources

Check these folders are marked as **Generated Sources Root** (green icon):

```
auth-service/target/generated-sources/annotations
user-service/target/generated-sources/annotations
rental-service/target/generated-sources/annotations
```

If **not green**:
- Right-click folder → **Mark Directory as** → **Generated Sources Root**

#### Step 6: Run Services

Now run each service:
- `AuthServiceApplication.java`
- `UserServiceApplication.java`
- `PostServiceApplication.java`
- `RentalServiceApplication.java`
- `MediaServiceApplication.java`

---

### Method 2: Run via Maven (Bypass IntelliJ)

If IntelliJ still has issues, run directly via Maven:

```bash
# User Service
cd /Users/nguyenthanhphuoc/Desktop/My-folder/vita-be
./start-user-service.sh
```

Or manually:
```bash
cd user-service
./mvnw spring-boot:run
```

This bypasses IntelliJ completely and uses Maven's classpath.

---

### Method 3: Clean IntelliJ Project

If above methods fail, clean IntelliJ project:

#### Step 1: Close Project
1. **File** → **Close Project**

#### Step 2: Delete IntelliJ Files
```bash
cd /Users/nguyenthanhphuoc/Desktop/My-folder/vita-be
rm -rf .idea
rm -rf */.idea
rm -rf */target
```

#### Step 3: Reimport Project
1. **Open** → Select `vita-be` folder
2. **Trust Project**
3. Wait for IntelliJ to import and index (~5 minutes)
4. Reload Maven Projects
5. Run services

---

## 🔍 Verify Mappers Generated

Check that mappers exist in classpath:

```bash
cd user-service
ls -la target/classes/com/vita/user_service/mapper/

# Should show:
# UserProfileMapper.class
# UserProfileMapperImpl.class  ← This is the generated one
```

Check source exists:
```bash
ls -la target/generated-sources/annotations/com/vita/user_service/mapper/

# Should show:
# UserProfileMapperImpl.java
```

View generated code:
```bash
cat target/generated-sources/annotations/com/vita/user_service/mapper/UserProfileMapperImpl.java
```

Should contain:
```java
@Component  // ← Spring will create bean
public class UserProfileMapperImpl implements UserProfileMapper {
    // ... implementation
}
```

---

## 📊 Expected Mappers by Service

| Service | Mappers Generated | File Names |
|---------|------------------|------------|
| Auth Service | 4 | UserMapperImpl, RoleMapperImpl, PermissionMapperImpl, ProfileMapperImpl |
| User Service | 1 | UserProfileMapperImpl |
| Post Service | 0 | (No MapStruct) |
| Rental Service | 3 | RentalMapperImpl, RentalRequestMapperImpl, ReviewMapperImpl |
| Media Service | 0 | (No MapStruct) |

---

## 🚨 Common Issues

### Issue 1: "Mapper interface but no implementation"

**Cause:** MapStruct annotation processor didn't run.

**Fix:**
```bash
cd <service>
./mvnw clean compile -DskipTests
```

### Issue 2: "Generated sources folder not green in IntelliJ"

**Cause:** IntelliJ doesn't recognize as source folder.

**Fix:**
- Right-click `target/generated-sources/annotations`
- **Mark Directory as** → **Generated Sources Root**

### Issue 3: "IntelliJ shows mapper but Spring can't find it"

**Cause:** IntelliJ cache issue.

**Fix:**
- File → Invalidate Caches → Invalidate and Restart

### Issue 4: "@Component annotation missing"

**Cause:** MapStruct config incorrect.

**Fix:** Check `pom.xml` has:
```xml
<compilerArgs>
    <arg>-Amapstruct.defaultComponentModel=spring</arg>
</compilerArgs>
```

---

## 🎯 Quick Commands Reference

```bash
# Clean and rebuild all services
./fix-intellij-mapstruct.sh

# Build single service
cd <service>
./mvnw clean install -DskipTests

# Run service via Maven (bypass IntelliJ)
cd <service>
./mvnw spring-boot:run

# Check generated mappers
find */target/generated-sources/annotations -name "*Mapper*.java"

# Check compiled mappers
find */target/classes -name "*Mapper*.class"
```

---

## ✅ Verification Checklist

After fix, verify:

- [ ] Generated sources exist in `target/generated-sources/annotations`
- [ ] Compiled classes exist in `target/classes`
- [ ] Generated sources folder is **green** in IntelliJ
- [ ] Maven reload completed without errors
- [ ] IntelliJ indexing completed
- [ ] Service starts without "bean not found" error
- [ ] Swagger UI accessible at service URL

---

## 🎓 Why This Happens

**MapStruct** uses annotation processing to generate implementation classes:

1. **Interface** (you write):
   ```java
   @Mapper(componentModel = "spring")
   public interface UserProfileMapper {
       UserResponse toUserProfileResponse(User entity);
   }
   ```

2. **Implementation** (MapStruct generates):
   ```java
   @Component  // ← Spring bean
   public class UserProfileMapperImpl implements UserProfileMapper {
       @Override
       public UserResponse toUserProfileResponse(User entity) {
           // Generated code here
       }
   }
   ```

3. **Problem:** If implementation not generated or not in classpath:
   ```
   No qualifying bean of type 'UserProfileMapper' available
   ```

4. **Solution:** Ensure:
   - Annotation processor runs (Maven compile)
   - Generated code in classpath (target/classes)
   - IntelliJ recognizes generated sources
   - Spring can scan @Component annotation

---

## 📞 Still Having Issues?

If none of the above work:

1. **Check Java version:** Should be Java 21
   ```bash
   java -version
   ```

2. **Check Maven version:** Should be 3.8+
   ```bash
   mvn -version
   ```

3. **Check MySQL running:**
   ```bash
   mysql -u root -p
   # password: password
   ```

4. **Check port not in use:**
   ```bash
   lsof -i :8081
   ```

5. **View full error log:** Run with debug:
   ```bash
   cd user-service
   ./mvnw spring-boot:run -Ddebug
   ```

---

## 🎉 Success Indicators

You'll know it's fixed when:

✅ Service starts without errors  
✅ Log shows: `Started UserServiceApplication in X seconds`  
✅ Swagger UI accessible: http://localhost:8081/user/swagger-ui.html  
✅ Health endpoint works: http://localhost:8081/user/actuator/health  

---

**Last Updated:** November 23, 2025  
**Status:** Verified Working

