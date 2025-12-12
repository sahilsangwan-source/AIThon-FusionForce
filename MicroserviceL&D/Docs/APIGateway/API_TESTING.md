# 🧪 API Testing Guide - Learning & Development Portal

## 📋 Overview

This document provides comprehensive API testing examples for all microservices in the Learning & Development Portal.

---

## 🔐 Authentication

All authenticated endpoints require a JWT token in the Authorization header:

```
Authorization: Bearer YOUR_ACCESS_TOKEN
```

### Get Access Token

```bash
# Login to get token
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@company.com",
    "password": "Admin@123"
  }'

# Response:
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "uuid",
    "email": "admin@company.com",
    "firstName": "Super",
    "lastName": "Admin",
    "role": "SUPER_ADMIN"
  }
}
```

---

## 1️⃣ User Service APIs (Port 8081)

### 1.1 Register New User

```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@company.com",
    "password": "Password@123",
    "firstName": "John",
    "lastName": "Doe",
    "employeeId": "EMP002",
    "department": "Engineering"
  }'
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "john.doe@company.com",
  "firstName": "John",
  "lastName": "Doe",
  "employeeId": "EMP002",
  "department": "Engineering",
  "role": "EMPLOYEE",
  "status": "ACTIVE",
  "createdAt": "2024-12-12T10:30:00"
}
```

### 1.2 User Login

```bash
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@company.com",
    "password": "Password@123"
  }'
```

### 1.3 Get Current User Profile

```bash
curl -X GET http://localhost:8081/api/users/me \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 1.4 Update User Profile

```bash
curl -X PUT http://localhost:8081/api/users/me \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Smith",
    "department": "DevOps"
  }'
```

### 1.5 Search Users (Admin Only)

```bash
curl -X GET "http://localhost:8081/api/users/search?query=john&page=0&size=20" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### 1.6 Assign Role (Super Admin Only)

```bash
curl -X POST http://localhost:8081/api/users/roles \
  -H "Authorization: Bearer YOUR_SUPER_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "roleId": "admin-role-uuid"
  }'
```

### 1.7 Refresh Token

```bash
curl -X POST http://localhost:8081/api/users/refresh-token \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

### 1.8 Logout

```bash
curl -X POST http://localhost:8081/api/users/logout \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 2️⃣ Training Service APIs (Port 8082)

### 2.1 Create Training (Admin/Super Admin)

```bash
curl -X POST http://localhost:8082/api/trainings \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Introduction to Microservices",
    "description": "Learn microservices architecture patterns",
    "category": "Architecture",
    "difficultyLevel": "INTERMEDIATE",
    "durationHours": 8.5,
    "thumbnailUrl": "https://example.com/thumbnail.jpg"
  }'
```

**Response (201 Created):**
```json
{
  "id": "training-uuid",
  "title": "Introduction to Microservices",
  "description": "Learn microservices architecture patterns",
  "category": "Architecture",
  "difficultyLevel": "INTERMEDIATE",
  "durationHours": 8.5,
  "status": "DRAFT",
  "createdBy": "admin-uuid",
  "createdAt": "2024-12-12T10:30:00"
}
```

### 2.2 List All Trainings

```bash
curl -X GET "http://localhost:8082/api/trainings?page=0&size=20&category=Architecture&status=PUBLISHED" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "training-uuid",
      "title": "Introduction to Microservices",
      "category": "Architecture",
      "durationHours": 8.5,
      "status": "PUBLISHED"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### 2.3 Get Training Details

```bash
curl -X GET http://localhost:8082/api/trainings/TRAINING_UUID \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 2.4 Update Training

```bash
curl -X PUT http://localhost:8082/api/trainings/TRAINING_UUID \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Advanced Microservices",
    "description": "Deep dive into microservices",
    "durationHours": 12.0
  }'
```

### 2.5 Add Module to Training

```bash
curl -X POST http://localhost:8082/api/trainings/TRAINING_UUID/modules \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Module 1: Introduction",
    "description": "Getting started with microservices",
    "sequenceOrder": 1,
    "contentType": "VIDEO",
    "estimatedDurationMinutes": 45
  }'
```

### 2.6 Add Quiz to Module

```bash
curl -X POST http://localhost:8082/api/trainings/modules/MODULE_UUID/quizzes \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Module 1 Quiz",
    "passingScore": 70,
    "timeLimitMinutes": 30,
    "questions": [
      {
        "questionText": "What is a microservice?",
        "questionType": "MULTIPLE_CHOICE",
        "options": ["A", "B", "C", "D"],
        "correctAnswer": "A",
        "points": 10
      }
    ]
  }'
```

### 2.7 Publish Training

```bash
curl -X POST http://localhost:8082/api/trainings/TRAINING_UUID/publish \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### 2.8 Get Training Categories

```bash
curl -X GET http://localhost:8082/api/trainings/categories \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 3️⃣ Assignment Service APIs (Port 8083)

### 3.1 Create Single Assignment

```bash
curl -X POST http://localhost:8083/api/assignments \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "trainingId": "TRAINING_UUID",
    "userId": "USER_UUID",
    "assignmentType": "MANDATORY",
    "dueDate": "2024-12-31T23:59:59"
  }'
```

### 3.2 Bulk Assignment

```bash
curl -X POST http://localhost:8083/api/assignments/bulk \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "trainingId": "TRAINING_UUID",
    "userIds": ["USER_UUID_1", "USER_UUID_2", "USER_UUID_3"],
    "assignmentType": "MANDATORY",
    "dueDate": "2024-12-31T23:59:59"
  }'
```

**Response (202 Accepted):**
```json
{
  "message": "Assignment request accepted. Processing 3 users.",
  "assignmentId": "assignment-uuid",
  "status": "PROCESSING"
}
```

### 3.3 Assign to Department

```bash
curl -X POST http://localhost:8083/api/assignments/department \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "trainingId": "TRAINING_UUID",
    "department": "Engineering",
    "assignmentType": "MANDATORY",
    "dueDate": "2024-12-31T23:59:59"
  }'
```

### 3.4 Get User Assignments

```bash
curl -X GET http://localhost:8083/api/assignments/user/me \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response (200 OK):**
```json
{
  "assignments": [
    {
      "id": "assignment-uuid",
      "training": {
        "id": "training-uuid",
        "title": "Introduction to Microservices",
        "durationHours": 8.5
      },
      "status": "ASSIGNED",
      "assignmentType": "MANDATORY",
      "dueDate": "2024-12-31T23:59:59",
      "assignedAt": "2024-12-01T10:00:00",
      "completionPercentage": 0
    }
  ]
}
```

### 3.5 Get Overdue Assignments

```bash
curl -X GET http://localhost:8083/api/assignments/overdue \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### 3.6 Create Mandatory Training Rule

```bash
curl -X POST http://localhost:8083/api/assignments/mandatory \
  -H "Authorization: Bearer YOUR_SUPER_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "trainingId": "TRAINING_UUID",
    "role": "EMPLOYEE",
    "department": "Engineering",
    "effectiveFrom": "2024-01-01T00:00:00"
  }'
```

---

## 4️⃣ Progress Service APIs (Port 8084)

### 4.1 Update Progress

```bash
curl -X POST http://localhost:8084/api/progress/update \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "trainingId": "TRAINING_UUID",
    "moduleId": "MODULE_UUID",
    "progressPercentage": 50,
    "timeSpentMinutes": 30
  }'
```

### 4.2 Get User Progress for Training

```bash
curl -X GET http://localhost:8084/api/progress/user/me/training/TRAINING_UUID \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response (200 OK):**
```json
{
  "trainingId": "TRAINING_UUID",
  "userId": "USER_UUID",
  "overallProgress": 50,
  "timeSpentMinutes": 120,
  "modules": [
    {
      "moduleId": "MODULE_UUID_1",
      "title": "Module 1",
      "progressPercentage": 100,
      "completed": true
    },
    {
      "moduleId": "MODULE_UUID_2",
      "title": "Module 2",
      "progressPercentage": 0,
      "completed": false
    }
  ],
  "lastAccessedAt": "2024-12-12T10:30:00"
}
```

### 4.3 Submit Quiz

```bash
curl -X POST http://localhost:8084/api/progress/quiz/submit \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "quizId": "QUIZ_UUID",
    "answers": [
      {
        "questionId": "Q1_UUID",
        "answer": "A"
      },
      {
        "questionId": "Q2_UUID",
        "answer": "B"
      }
    ]
  }'
```

**Response (200 OK):**
```json
{
  "attemptId": "attempt-uuid",
  "score": 80,
  "maxScore": 100,
  "passed": true,
  "attemptNumber": 1,
  "completedAt": "2024-12-12T10:30:00"
}
```

### 4.4 Get Quiz Attempts

```bash
curl -X GET http://localhost:8084/api/progress/quiz/QUIZ_UUID/attempts \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4.5 Update Video Progress

```bash
curl -X POST http://localhost:8084/api/progress/video/update \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "videoId": "VIDEO_UUID",
    "watchedDuration": 300,
    "totalDuration": 600,
    "lastPosition": 300
  }'
```

### 4.6 Mark Training Complete

```bash
curl -X POST http://localhost:8084/api/progress/complete \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "trainingId": "TRAINING_UUID"
  }'
```

### 4.7 Get Certificates

```bash
curl -X GET http://localhost:8084/api/progress/certificates/me \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 5️⃣ Notification Service APIs (Port 8085)

### 5.1 Get User Notifications

```bash
curl -X GET "http://localhost:8085/api/notifications/user/me?page=0&size=20" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response (200 OK):**
```json
{
  "notifications": [
    {
      "id": "notification-uuid",
      "type": "TRAINING_ASSIGNED",
      "title": "New Training Assigned",
      "message": "You have been assigned: Introduction to Microservices",
      "readStatus": false,
      "actionUrl": "/trainings/TRAINING_UUID",
      "createdAt": "2024-12-12T10:00:00"
    }
  ],
  "unreadCount": 5
}
```

### 5.2 Mark Notification as Read

```bash
curl -X PUT http://localhost:8085/api/notifications/NOTIFICATION_UUID/read \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 5.3 Mark All as Read

```bash
curl -X PUT http://localhost:8085/api/notifications/read-all \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 5.4 Get Unread Count

```bash
curl -X GET http://localhost:8085/api/notifications/unread-count \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 5.5 Update Notification Preferences

```bash
curl -X POST http://localhost:8085/api/notifications/preferences \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "emailEnabled": true,
    "pushEnabled": true,
    "inAppEnabled": true,
    "smsEnabled": false,
    "digestFrequency": "DAILY"
  }'
```

---

## 6️⃣ Content Service APIs (Port 8086)

### 6.1 Upload Content

```bash
curl -X POST http://localhost:8086/api/content/upload \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -F "file=@/path/to/video.mp4" \
  -F "contentType=VIDEO" \
  -F "trainingId=TRAINING_UUID"
```

**Response (201 Created):**
```json
{
  "contentId": "content-uuid",
  "fileName": "video.mp4",
  "fileSize": 104857600,
  "contentType": "VIDEO",
  "uploadStatus": "COMPLETED",
  "cdnUrl": "https://cdn.example.com/content/video.mp4"
}
```

### 6.2 Get Content Metadata

```bash
curl -X GET http://localhost:8086/api/content/CONTENT_UUID \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 6.3 Get Signed URL for Download

```bash
curl -X GET http://localhost:8086/api/content/CONTENT_UUID/signed-url \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response (200 OK):**
```json
{
  "url": "https://s3.amazonaws.com/bucket/content?signature=...",
  "expiresIn": 300
}
```

### 6.4 Delete Content

```bash
curl -X DELETE http://localhost:8086/api/content/CONTENT_UUID \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

---

## 7️⃣ Analytics Service APIs (Port 8087)

### 7.1 Get Training Analytics

```bash
curl -X GET http://localhost:8087/api/analytics/training/TRAINING_UUID \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

**Response (200 OK):**
```json
{
  "trainingId": "TRAINING_UUID",
  "totalEnrollments": 150,
  "totalCompletions": 120,
  "completionRate": 80.0,
  "averageScore": 85.5,
  "averageCompletionTimeHours": 7.5
}
```

### 7.2 Get User Analytics

```bash
curl -X GET http://localhost:8087/api/analytics/user/USER_UUID \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### 7.3 Get Department Analytics

```bash
curl -X GET http://localhost:8087/api/analytics/department/Engineering \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### 7.4 Generate Custom Report

```bash
curl -X POST http://localhost:8087/api/analytics/reports/generate \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reportType": "USER_COMPLETION",
    "filters": {
      "startDate": "2024-01-01",
      "endDate": "2024-12-31",
      "department": "Engineering",
      "trainingCategory": "Programming"
    },
    "format": "EXCEL"
  }'
```

**Response (202 Accepted):**
```json
{
  "reportId": "report-uuid",
  "status": "PROCESSING",
  "message": "Report generation started. You will be notified when ready."
}
```

### 7.5 Get Report Status

```bash
curl -X GET http://localhost:8087/api/analytics/reports/REPORT_UUID \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

**Response (200 OK):**
```json
{
  "reportId": "report-uuid",
  "status": "COMPLETED",
  "fileUrl": "https://s3.amazonaws.com/reports/report.xlsx",
  "createdAt": "2024-12-12T10:00:00",
  "completedAt": "2024-12-12T10:05:00"
}
```

### 7.6 Get Dashboard Metrics

```bash
curl -X GET http://localhost:8087/api/analytics/dashboard \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

---

## 8️⃣ Workflow Service APIs (Port 8088)

### 8.1 Initiate Workflow

```bash
curl -X POST http://localhost:8088/api/workflows/initiate \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "workflowType": "TRAINING_PUBLICATION",
    "entityId": "TRAINING_UUID",
    "entityType": "TRAINING"
  }'
```

### 8.2 Get Pending Approvals

```bash
curl -X GET http://localhost:8088/api/workflows/pending/me \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 8.3 Approve Workflow Step

```bash
curl -X POST http://localhost:8088/api/workflows/WORKFLOW_UUID/approve \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "comments": "Approved. Content looks good."
  }'
```

### 8.4 Reject Workflow

```bash
curl -X POST http://localhost:8088/api/workflows/WORKFLOW_UUID/reject \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "comments": "Please revise the content."
  }'
```

### 8.5 Get Workflow Status

```bash
curl -X GET http://localhost:8088/api/workflows/WORKFLOW_UUID/status \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 🧪 Testing Scenarios

### Scenario 1: Complete User Journey

```bash
# 1. Register
curl -X POST http://localhost:8081/api/users/register -H "Content-Type: application/json" -d '{"email":"test@company.com","password":"Test@123","firstName":"Test","lastName":"User","employeeId":"EMP999","department":"IT"}'

# 2. Login
TOKEN=$(curl -X POST http://localhost:8081/api/users/login -H "Content-Type: application/json" -d '{"email":"test@company.com","password":"Test@123"}' | jq -r '.accessToken')

# 3. Get Profile
curl -X GET http://localhost:8081/api/users/me -H "Authorization: Bearer $TOKEN"

# 4. View Trainings
curl -X GET http://localhost:8082/api/trainings -H "Authorization: Bearer $TOKEN"

# 5. View Assignments
curl -X GET http://localhost:8083/api/assignments/user/me -H "Authorization: Bearer $TOKEN"
```

### Scenario 2: Admin Creates and Assigns Training

```bash
# 1. Login as Admin
ADMIN_TOKEN=$(curl -X POST http://localhost:8081/api/users/login -H "Content-Type: application/json" -d '{"email":"admin@company.com","password":"Admin@123"}' | jq -r '.accessToken')

# 2. Create Training
TRAINING_ID=$(curl -X POST http://localhost:8082/api/trainings -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" -d '{"title":"Test Training","description":"Test","category":"IT","difficultyLevel":"BEGINNER","durationHours":5}' | jq -r '.id')

# 3. Publish Training
curl -X POST http://localhost:8082/api/trainings/$TRAINING_ID/publish -H "Authorization: Bearer $ADMIN_TOKEN"

# 4. Assign to Department
curl -X POST http://localhost:8083/api/assignments/department -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" -d "{\"trainingId\":\"$TRAINING_ID\",\"department\":\"IT\",\"assignmentType\":\"MANDATORY\",\"dueDate\":\"2024-12-31T23:59:59\"}"
```

---

## 📊 Postman Collection

Import this JSON into Postman for easy testing:

```json
{
  "info": {
    "name": "Learning Portal API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost",
      "type": "string"
    },
    {
      "key": "token",
      "value": "",
      "type": "string"
    }
  ]
}
```

Save this collection and update the `token` variable after login.

---

## ✅ Testing Checklist

- [ ] User registration works
- [ ] User login returns JWT token
- [ ] Token refresh works
- [ ] Protected endpoints require authentication
- [ ] Admin can create trainings
- [ ] Bulk assignment works
- [ ] Progress tracking updates correctly
- [ ] Notifications are created
- [ ] Reports can be generated
- [ ] Workflow approvals work

---

**For more details, see IMPLEMENTATION_GUIDE.md and QUICK_START.md**