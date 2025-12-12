# 📚 Training Service Documentation

Training and course management documentation.

## 📖 Files in This Directory

### 1. **TRAINING_SERVICE_COMPLETE.md** ⭐ START HERE
- Complete implementation guide
- All features documented
- Course management
- Assignment tracking
- Completion monitoring
- API endpoints reference
- **Read Time:** 30 minutes

### 2. **API_TESTING.md**
- API testing procedures
- Manual test cases
- Test workflows
- Curl examples
- **Read Time:** 20 minutes

---

## 🎯 Quick Overview

The Training Service handles:
- ✅ Course creation and management
- ✅ Training module organization
- ✅ Assignment creation and tracking
- ✅ Course enrollment
- ✅ Progress tracking
- ✅ Completion management
- ✅ Quiz management

---

## 📊 API Endpoints

### Public Endpoints (No Authentication)
```
GET    /api/training/search              - Search courses
GET    /api/training/published           - List published courses
GET    /api/training/public/**           - Public course information
```

### Protected Endpoints (JWT Required)
```
GET    /api/training/courses             - Get user's courses
GET    /api/training/courses/{id}        - Get course details
POST   /api/training/courses             - Create course
PUT    /api/training/courses/{id}        - Update course
DELETE /api/training/courses/{id}        - Delete course

GET    /api/training/modules/**          - Get modules
POST   /api/training/modules             - Create module
PUT    /api/training/modules/{id}        - Update module

GET    /api/training/assignments/**      - Get assignments
POST   /api/training/assignments         - Create assignment
PUT    /api/training/assignments/{id}    - Update assignment

GET    /api/training/completion/**       - Get completion status
POST   /api/training/completion/{id}     - Mark as complete

POST   /api/training/enrollments         - Enroll in course
GET    /api/training/enrollments         - Get enrollments
```

---

## 🏆 Course Workflow

```
1. Instructor creates course
2. Add modules to course
3. Create assignments
4. Publish course
5. Students search/find course
6. Students enroll
7. Students access modules
8. Students complete assignments
9. Track progress
10. Mark course complete
```

---

## 💾 Database Schema

### Core Tables
```
courses
├─ id, title, description
├─ instructor_id, category
├─ status (DRAFT, PUBLISHED)
├─ created_at, updated_at

modules
├─ id, course_id
├─ title, content
├─ order_index
├─ created_at

assignments
├─ id, module_id
├─ title, description
├─ due_date, status
├─ created_at

enrollments
├─ id, course_id, user_id
├─ enrolled_date, completion_date

progress
├─ id, user_id, course_id
├─ progress_percentage
├─ last_accessed
```

---

## 🚀 How to Use This Documentation

### For New Users (30 minutes)
1. **This file** - Overview
2. **TRAINING_SERVICE_COMPLETE.md** - Full guide
3. **API_TESTING.md** - Test examples

### For API Integration
1. **TRAINING_SERVICE_COMPLETE.md** - API endpoints
2. **API_TESTING.md** - Testing procedures

### For Testing
1. **API_TESTING.md** - Test cases
2. Run tests and verify

---

## 🔑 Key Features

### Course Management
- Create courses
- Organize modules
- Manage assignments
- Publish courses
- Track progress

### Student Features
- Search courses
- Enroll in courses
- Access materials
- Submit assignments
- Track progress
- Complete courses

### Instructor Features
- Create courses
- Add content
- Create assignments
- View student progress
- Monitor completion

### Admin Features
- Manage all courses
- View analytics
- User management
- Category management
- System configuration

---

## 📈 Service Details

**Port:** 8082 (Internal only, access via API Gateway)
**Database:** PostgreSQL
**Cache:** Redis
**Message Broker:** Kafka
**Service Registry:** Eureka

---

## 🧪 Quick Test Examples

### 1. Search Courses
```bash
curl http://localhost:8080/api/training/search?q=python
```

### 2. List Published Courses
```bash
curl http://localhost:8080/api/training/published
```

### 3. Get User's Courses (with JWT)
```bash
TOKEN="your_jwt_token_here"
curl http://localhost:8080/api/training/courses \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Enroll in Course
```bash
TOKEN="your_jwt_token_here"
curl -X POST http://localhost:8080/api/training/enrollments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"courseId": 1}'
```

### 5. Get Progress
```bash
TOKEN="your_jwt_token_here"
curl http://localhost:8080/api/training/completion/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🏗️ Architecture

```
API Gateway (Port 8080)
    ↓
Training Service (Port 8082)
    ├─ Course Controller
    ├─ Module Controller
    ├─ Assignment Controller
    ├─ Enrollment Controller
    ├─ Progress Controller
    ├─ Training Service (Business Logic)
    ├─ Repository Layer
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

---

## 🔄 Service Communication

**With Eureka:**
- Service registers at startup
- Eureka monitors health
- Load balancer routes requests
- Automatic failover

**With User Service:**
- Accesses user information
- Validates user enrollment
- Updates user progress

**With API Gateway:**
- All requests through gateway
- JWT validated at gateway
- Automatic load balancing

---

## 🔒 Security Features

1. **JWT Authentication** - Token-based auth
2. **Authorization** - Role-based access control
3. **Data Validation** - Input validation
4. **Rate Limiting** - Prevent abuse
5. **Encryption** - Data protection
6. **CORS** - Cross-origin support

---

## 📚 Configuration

### Application Properties
```yaml
server.port: 8082
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
- Port 8082 availability

**Solutions:**
- Verify PostgreSQL is running
- Verify Redis is running
- Check logs

### Course Not Appearing
**Check:**
- Course is published
- Course status is correct
- Database has course data

### Enrollment Failing
**Check:**
- User is authenticated
- Course exists
- User is not already enrolled

---

## 📊 Statistics

- **API Endpoints:** 15+
- **Database Tables:** 6+
- **Entity Types:** 5 (Course, Module, Assignment, Enrollment, Progress)
- **Authentication:** JWT
- **Cache:** Redis enabled
- **Message Broker:** Kafka enabled

---

## 🎯 Key Features

✅ Course management
✅ Module organization
✅ Assignment creation
✅ Student enrollment
✅ Progress tracking
✅ Completion monitoring
✅ Search functionality
✅ Publishing system
✅ Category management
✅ Caching support

---

## 📞 Quick Reference

| Task | Endpoint |
|------|----------|
| Search Courses | GET /api/training/search |
| List Published | GET /api/training/published |
| Get Courses | GET /api/training/courses |
| Create Course | POST /api/training/courses |
| Get Modules | GET /api/training/modules |
| Get Assignments | GET /api/training/assignments |
| Track Progress | GET /api/training/completion |
| Enroll | POST /api/training/enrollments |

---

## 🎉 Status

✅ **Training Service** - Fully implemented
✅ **Course Management** - Complete
✅ **Progress Tracking** - Enabled
✅ **Database** - PostgreSQL integrated
✅ **Caching** - Redis enabled
✅ **Documentation** - Complete

---

## 📖 Related Documentation

- **API Gateway:** See `../APIGateway/` for routing
- **Eureka:** See `../Eureka/` for service discovery
- **User Service:** See `../UserService/` for user management
- **General:** See `../General/` for project overview

---

**Start Reading:** TRAINING_SERVICE_COMPLETE.md

**For Testing:** API_TESTING.md

For navigation to all docs, see the parent directory: `../README.md`

