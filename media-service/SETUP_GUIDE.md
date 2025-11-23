# Media Service Setup Guide

Complete guide to setup AWS S3 and configure Media Service.

---

## 📋 Prerequisites

- AWS Account
- AWS CLI installed (optional)
- Java 21+
- MySQL 8.0+

---

## 🪣 Step 1: Create S3 Bucket

### Via AWS Console:

1. Login to [AWS Console](https://console.aws.amazon.com/)
2. Navigate to **S3** service
3. Click **"Create bucket"**
4. Configure bucket:
   - **Bucket name**: `vita-media-bucket` (must be globally unique)
   - **Region**: `ap-southeast-1` (Singapore) or your preferred region
   - **Block Public Access**: Keep defaults (block all public access)
   - **Bucket Versioning**: Disabled (optional)
   - **Encryption**: Enable (AES-256)
5. Click **"Create bucket"**

### Via AWS CLI:

```bash
aws s3 mb s3://vita-media-bucket --region ap-southeast-1
```

---

## 🔑 Step 2: Create IAM User for Media Service

### Via AWS Console:

1. Navigate to **IAM** service
2. Click **"Users"** → **"Add users"**
3. User name: `vita-media-service-user`
4. Select **"Programmatic access"** (Access key)
5. Click **"Next: Permissions"**
6. Click **"Attach existing policies directly"**
7. Search and select: **`AmazonS3FullAccess`** (or create custom policy below)
8. Click **"Next"** until **"Create user"**
9. **IMPORTANT**: Copy and save:
   - Access key ID
   - Secret access key

### Custom IAM Policy (Recommended):

Instead of `AmazonS3FullAccess`, create a custom policy with minimal permissions:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowS3Upload",
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:PutObjectAcl",
        "s3:GetObject",
        "s3:DeleteObject",
        "s3:HeadObject"
      ],
      "Resource": "arn:aws:s3:::vita-media-bucket/*"
    },
    {
      "Sid": "AllowS3ListBucket",
      "Effect": "Allow",
      "Action": [
        "s3:ListBucket"
      ],
      "Resource": "arn:aws:s3:::vita-media-bucket"
    }
  ]
}
```

---

## 🔐 Step 3: Configure S3 Bucket Policy

Add this bucket policy to allow presigned URL uploads:

1. Go to S3 bucket → **Permissions** tab
2. Scroll to **Bucket policy**
3. Click **"Edit"** and add:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowPresignedUploads",
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::YOUR_ACCOUNT_ID:user/vita-media-service-user"
      },
      "Action": "s3:PutObject",
      "Resource": "arn:aws:s3:::vita-media-bucket/*"
    }
  ]
}
```

Replace `YOUR_ACCOUNT_ID` with your AWS account ID.

---

## 🌐 Step 4: Configure CORS (if frontend needs direct access)

1. Go to S3 bucket → **Permissions** tab
2. Scroll to **CORS configuration**
3. Click **"Edit"** and add:

```json
[
  {
    "AllowedHeaders": [
      "*"
    ],
    "AllowedMethods": [
      "GET",
      "PUT",
      "POST",
      "DELETE",
      "HEAD"
    ],
    "AllowedOrigins": [
      "http://localhost:3000",
      "https://yourdomain.com"
    ],
    "ExposeHeaders": [
      "ETag"
    ],
    "MaxAgeSeconds": 3000
  }
]
```

---

## ⚙️ Step 5: Configure Media Service

### Update `application.yml`:

```yaml
aws:
  s3:
    access-key: ${AWS_S3_ACCESS_KEY}
    secret-key: ${AWS_S3_SECRET_KEY}
    region: ap-southeast-1
    bucket-name: vita-media-bucket
    presigned-url-expiration: 3600  # 1 hour
```

### Set Environment Variables:

#### Linux/MacOS:

```bash
export AWS_S3_ACCESS_KEY=your_access_key_here
export AWS_S3_SECRET_KEY=your_secret_key_here
export AWS_S3_REGION=ap-southeast-1
export AWS_S3_BUCKET_NAME=vita-media-bucket
```

Add to `~/.bashrc` or `~/.zshrc` for persistence.

#### Windows:

```cmd
set AWS_S3_ACCESS_KEY=your_access_key_here
set AWS_S3_SECRET_KEY=your_secret_key_here
set AWS_S3_REGION=ap-southeast-1
set AWS_S3_BUCKET_NAME=vita-media-bucket
```

---

## 🗄️ Step 6: Setup Database

### Create Database:

```sql
CREATE DATABASE media_service_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### Grant Permissions:

```sql
GRANT ALL PRIVILEGES ON media_service_db.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

Tables will be created automatically by Hibernate on first startup.

---

## 🚀 Step 7: Start Media Service

### Via Maven:

```bash
cd media-service
./mvnw clean install
./mvnw spring-boot:run
```

### Via JAR:

```bash
./mvnw clean package
java -jar target/media-service-0.0.1-SNAPSHOT.jar
```

### Via IDE (IntelliJ/Eclipse):

1. Open project
2. Run `MediaServiceApplication.java`

Service will start on **http://localhost:8085**

---

## 🧪 Step 8: Test Setup

### Test 1: Health Check

```bash
curl http://localhost:8085/actuator/health
```

Expected: `{"status":"UP"}`

### Test 2: Request Presigned URL

```bash
curl -X POST http://localhost:8080/api/v1/media/upload-request \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "fileName": "test.jpg",
    "fileType": "image/jpeg",
    "fileSize": 1048576
  }'
```

Expected: Presigned URL response

### Test 3: Upload File to S3

Use the presigned URL from Test 2:

```bash
curl -X PUT "PRESIGNED_URL_FROM_STEP_2" \
  -H "Content-Type: image/jpeg" \
  --data-binary "@/path/to/test.jpg"
```

### Test 4: Confirm Upload

```bash
curl -X POST http://localhost:8080/api/v1/media/FILE_ID/confirm \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🔍 Troubleshooting

### Issue 1: "Access Denied" Error

**Solution:**
- Verify IAM user has correct permissions
- Check bucket policy is configured
- Ensure AWS credentials are correct

### Issue 2: "Presigned URL Expired"

**Solution:**
- Default expiration is 1 hour
- Adjust `aws.s3.presigned-url-expiration` in config
- Ensure system time is synchronized

### Issue 3: CORS Error

**Solution:**
- Add your frontend domain to CORS config
- Include all required HTTP methods
- Clear browser cache

### Issue 4: "File Not Found in S3"

**Solution:**
- Verify file was uploaded successfully
- Check S3 bucket region matches config
- Confirm file key is correct

### Issue 5: Database Connection Error

**Solution:**
- Verify MySQL is running
- Check database credentials
- Ensure database `media_service_db` exists

---

## 📊 Monitoring

### View S3 Bucket Contents:

```bash
aws s3 ls s3://vita-media-bucket/uploads/ --recursive
```

### Check Database Records:

```sql
USE media_service_db;
SELECT * FROM media ORDER BY created_at DESC LIMIT 10;
```

### View Application Logs:

```bash
tail -f logs/media-service.log
```

---

## 🔒 Security Best Practices

1. **Never commit AWS credentials** to version control
2. **Use environment variables** for sensitive data
3. **Rotate AWS access keys** regularly
4. **Enable S3 bucket encryption**
5. **Set short expiration** for presigned URLs
6. **Implement user quotas** to prevent abuse
7. **Monitor S3 costs** regularly
8. **Use IAM roles** instead of access keys (for EC2/ECS)

---

## 💰 Cost Optimization

### S3 Pricing (ap-southeast-1):

- **Storage**: ~$0.025 per GB/month
- **PUT/POST requests**: ~$0.005 per 1,000 requests
- **GET requests**: ~$0.0004 per 1,000 requests
- **Data transfer OUT**: ~$0.12 per GB

### Tips to Reduce Costs:

1. **Implement lifecycle policies** to archive old files
2. **Use S3 Intelligent-Tiering** for automatic cost optimization
3. **Enable compression** for images/videos before upload
4. **Delete unused files** regularly
5. **Monitor storage usage** with CloudWatch

---

## 🔗 Useful AWS CLI Commands

```bash
# List all buckets
aws s3 ls

# List bucket contents
aws s3 ls s3://vita-media-bucket/

# Download file from S3
aws s3 cp s3://vita-media-bucket/uploads/image/user-id/file.jpg ./local.jpg

# Delete file from S3
aws s3 rm s3://vita-media-bucket/uploads/image/user-id/file.jpg

# Sync local directory to S3
aws s3 sync ./local-dir s3://vita-media-bucket/backups/

# Get bucket size
aws s3 ls s3://vita-media-bucket/ --recursive --summarize | grep "Total Size"
```

---

## 📞 Support

For AWS-specific issues:
- [AWS Support Center](https://console.aws.amazon.com/support/)
- [AWS Documentation](https://docs.aws.amazon.com/s3/)

For Media Service issues:
- Check application logs
- Refer to README.md
- Contact development team

---

## ✅ Setup Checklist

- [ ] AWS account created
- [ ] S3 bucket created
- [ ] IAM user created with access keys
- [ ] Bucket policy configured
- [ ] CORS configured (if needed)
- [ ] Environment variables set
- [ ] Database created
- [ ] Media service started
- [ ] Tested upload workflow
- [ ] Verified files in S3

---

Setup complete! 🎉

