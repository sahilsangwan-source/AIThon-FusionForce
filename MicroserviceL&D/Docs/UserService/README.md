# 👥 User Service Documentation

User management, authentication, and profile management documentation.

## 📖 Files in This Directory

### 1. **COMPLETE_USER_SERVICE_IMPLEMENTATION.md** ⭐ START HERE
- Complete implementation guide
- All features documented
- Configuration details
- Database schema
- API endpoints reference
- **Read Time:** 30 minutes

### 2. **USER_SERVICE_QUICK_REFERENCE.md**
- Quick reference guide
- Common commands
- API endpoints summary
- Testing procedures
- **Read Time:** 10 minutes

### 3. **USER_SERVICE_TESTING.md**
- Testing procedures
- Manual test cases
- Automated testing
- Troubleshooting
- **Read Time:** 15 minutes

---

## 🎯 Quick Overview

The User Service handles:
- ✅ User registration
- ✅ Authentication (JWT)
- ✅ Profile management
- ✅ Role management
- ✅ User verification
- ✅ Password management

---

## 📊 API Endpoints

### Public Endpoints (No Authentication)
```
POST   /api/auth/register        - Register new user
POST   /api/auth/login           - Login user
POST   /api/auth/refresh         - Refresh JWT token
```

### Protected Endpoints (JWT Required)
```
GET    /api/users/profile        - Get current user profile
PUT    /api/users/profile        - Update user profile
GET    /api/users/{id}           - Get user by ID
PUT    /api/users/{id}           - Update user
DELETE /api/users/{id}           - Delete user
GET    /api/users                - List all users (admin)
```

---

## 🔐 Authentication Flow

```
1. User registers: POST /api/auth/register
2. User logs in: POST /api/auth/login
3. Receive JWT token
4. Send token in Authorization header
5. Access protected endpoints
6. Token expires: Use refresh endpoint
```

---

## 💾 Database Schema

### User Table
```sql
id              - Primary key
email           - Unique email
password        - Hashed password
full_name       - User's full name
role            - STUDENT, INSTRUCTOR, ADMIN
status          - ACTIVE, INACTIVE
created_at      - Creation timestamp
updated_at      - Update timestamp
```

---

## 🚀 How to Use This Documentation

### For New Users (20 minutes)
1. **USER_SERVICE_QUICK_REFERENCE.md** - Overview
2. **COMPLETE_USER_SERVICE_IMPLEMENTATION.md** - Full guide

### For Testing (30 minutes)
1. **USER_SERVICE_TESTING.md** - Test procedures
2. Run tests and verify

### For Development
1. **COMPLETE_USER_SERVICE_IMPLEMENTATION.md** - Implementation details
2. Check API endpoints
3. Review database schema

---

## 🔑 Key Features

### User Management
- User registration
- User login
- Profile management
- Role assignment
- User deactivation

### Authentication
- JWT token generation
- Token validation
- Token refresh
- Token expiration

### Security
- Password hashing
- Email verification
- Rate limiting
- CORS enabled

---

## 📈 Service Details

**Port:** 8081 (Internal only, access via API Gateway)
**Database:** PostgreSQL
**Cache:** Redis
**Message Broker:** Kafka
**Service Registry:** Eureka

---

## 🧪 Quick Test

### 1. Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "fullName": "Test User"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

### 3. Get Profile (with JWT)
```bash
TOKEN="your_jwt_token_here"
curl http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🏗️ Architecture

```
API Gateway (Port 8080)
    ↓
User Service (Port 8081)
    ├─ Authentication Controller
    ├─ User Controller
    ├─ User Service (Business Logic)
    ├─ User Repository
    ├─ Database (PostgreSQL)
    └─ Cache (Redis)
```

---

## 📋 Database Configuration

### Connection Details
- **Host:** localhost (or postgres in Docker)
- **Port:** 5432
- **Database:** lms_db
- **User:** lmsuser
- **Password:** lmspassword

### Connection String
```
jdbc:postgresql://localhost:5432/lms_db
```

---

## 🔄 Service Communication

**With Eureka:**
- Service registers at startup
- Eureka monitors health
- Load balancer routes requests
- Automatic failover

**With API Gateway:**
- All requests through gateway
- JWT validated at gateway
- User info passed via headers
- No direct access possible

---

## 🔒 Security Implementation

1. **Password Hashing** - BCrypt encryption
2. **JWT Tokens** - Secure token-based auth
3. **CORS** - Cross-origin resource sharing
4. **Rate Limiting** - Prevent abuse
5. **Input Validation** - Prevent injection
6. **Error Handling** - Secure error messages

---

## 📚 Configuration Files

### Application Properties
```yaml
server.port: 8081
spring.datasource.url: jdbc:postgresql://postgres:5432/lms_db
spring.datasource.username: lmsuser
spring.datasource.password: lmspassword
spring.redis.host: redis
spring.redis.port: 6379
eureka.client.service-url.defaultZone: http://eureka-server:8761/eureka/
```

---

## 🆘 Troubleshooting

### Service Not Starting
**Check:**
- Database connectivity
- Redis connectivity
- Port 8081 availability

**Solutions:**
- Verify PostgreSQL is running
- Verify Redis is running
- Check logs for errors

### JWT Validation Failing
**Check:**
- Token format is correct
- Token is not expired
- Secret key matches

### Database Errors
**Check:**
- Database user credentials
- Database is created
- Tables are initialized

---

## 📊 Statistics

- **API Endpoints:** 10+
- **Database Tables:** 2+
- **Authentication Methods:** JWT
- **Cache Implementation:** Redis
- **Message Broker:** Kafka

---

## 🎯 Key Features

✅ User registration and login
✅ JWT authentication
✅ Profile management
✅ Role-based access
✅ Password hashing
✅ Email verification
✅ Rate limiting
✅ Caching support
✅ Kafka integration
✅ Database persistence

---

## 📞 Quick Reference

| Task | Command |
|------|---------|
| Register | POST /api/auth/register |
| Login | POST /api/auth/login |
| Get Profile | GET /api/users/profile |
| Update Profile | PUT /api/users/profile |
| Refresh Token | POST /api/auth/refresh |

---

## 🎉 Status

✅ **User Service** - Fully implemented
✅ **Authentication** - JWT configured
✅ **Database** - PostgreSQL integrated
✅ **Caching** - Redis enabled
✅ **Documentation** - Complete

---

## 📖 Related Documentation

- **API Gateway:** See `../APIGateway/` for routing
- **Eureka:** See `../Eureka/` for service discovery
- **Training Service:** See `../TrainingService/` for training integration
- **General:** See `../General/` for project overview

---

**Start Reading:** USER_SERVICE_QUICK_REFERENCE.md

**For Complete Details:** COMPLETE_USER_SERVICE_IMPLEMENTATION.md

For navigation to all docs, see the parent directory: `../README.md`

