
# Learning & Development Portal - System Architecture Design

## Executive Summary

Enterprise-scale Learning Management System (LMS) designed for 5000+ employees with 500+ concurrent users, supporting multiple content types (videos, documents, quizzes) with SSO integration.

---

## 1. System Overview

### 1.1 Key Requirements
- **Scale**: 5000+ employees, 500+ concurrent users
- **Content Types**: Videos, Documents, PDFs, Quizzes, Assessments
- **User Roles**: Employee, Admin, Super Admin
- **Authentication**: JWT + OAuth2/SAML SSO
- **Reporting**: Excel export with Python-based analytics
- **Real-time**: Notifications for assignments and deadlines
- **Architecture**: Microservices with event-driven communication

### 1.2 Technology Stack

#### Backend
- **Microservices Framework**: Spring Boot (Java)
- **Database**: H2 (Development), PostgreSQL (Production recommended)
- **Message Broker**: Apache Kafka
- **Cache**: Redis
- **API Gateway**: Spring Cloud Gateway / Kong
- **Service Discovery**: Eureka / Consul
- **Load Balancer**: Nginx / HAProxy

#### Frontend
- **Framework**: React 18+
- **State Management**: Redux Toolkit / Zustand
- **UI Library**: Material-UI / Ant Design
- **API Client**: Axios with interceptors

#### DevOps
- **Containerization**: Docker
- **Orchestration**: Docker Compose (local), Kubernetes (production)
- **Monitoring**: Prometheus + Grafana
- **Logging**: ELK Stack (Elasticsearch, Logstash, Kibana)
- **Tracing**: Jaeger / Zipkin

#### Reporting
- **Engine**: Python (Pandas, OpenPyXL)
- **API**: FastAPI / Flask
- **Scheduler**: Celery with Redis

---

## 2. Microservices Architecture

### 2.1 High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     API Gateway (Kong/Spring Cloud Gateway)  │
│                     - Rate Limiting                          │
│                     - Authentication                         │
│                     - Request Routing                        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
        ┌─────────────────────────────────────────────┐
        │         Service Discovery (Eureka)          │
        └─────────────────────────────────────────────┘
                              │
        ┌─────────────────────┴─────────────────────────┐
        │                                               │
        ▼                                               ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│   User       │  │   Training   │  │  Progress    │  │  Notification│
│   Service    │  │   Service    │  │  Service     │  │  Service     │
└──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘
        │                 │                 │                 │
        ▼                 ▼                 ▼                 ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  Assignment  │  │   Content    │  │  Analytics   │  │   Workflow   │
│  Service     │  │   Service    │  │  Service     │  │   Service    │
└──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘
        │                 │                 │                 │
        └─────────────────┴─────────────────┴─────────────────┘
                              │
                              ▼
                    ┌──────────────────┐
                    │  Apache Kafka    │
                    │  Event Bus       │
                    └──────────────────┘
```

### 2.2 Service Breakdown

#### **2.2.1 User Service**
**Port**: 8081  
**Responsibility**: User management, authentication, authorization, role management

**Database Schema**:
```sql
users (
  id UUID PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255),
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  employee_id VARCHAR(50) UNIQUE,
  department VARCHAR(100),
  role VARCHAR(50),
  status VARCHAR(20),
  created_at TIMESTAMP,
  updated_at TIMESTAMP
)

roles (
  id UUID PRIMARY KEY,
  name VARCHAR(50) UNIQUE,
  permissions JSONB
)

user_roles (
  user_id UUID REFERENCES users(id),
  role_id UUID REFERENCES roles(id),
  PRIMARY KEY (user_id, role_id)
)

sso_providers (
  id UUID PRIMARY KEY,
  provider_name VARCHAR(50),
  client_id VARCHAR(255),
  client_secret VARCHAR(255),
  config JSONB
)

user_sessions (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  token VARCHAR(500),
  refresh_token VARCHAR(500),
  expires_at TIMESTAMP,
  created_at TIMESTAMP
)
```

**REST APIs**:
- `POST /api/users/register` - User registration
- `POST /api/users/login` - Login with JWT
- `POST /api/users/sso/oauth2` - SSO login (OAuth2)
- `POST /api/users/sso/saml` - SSO login (SAML)
- `POST /api/users/refresh-token` - Refresh JWT token
- `GET /api/users/{id}` - Get user profile
- `PUT /api/users/{id}` - Update user profile
- `POST /api/users/roles` - Assign roles (Super Admin only)
- `GET /api/users/search` - Search users (Admin)
- `DELETE /api/users/{id}` - Deactivate user

**Why H2 vs PostgreSQL**:
- **H2**: Development/testing (in-memory, zero config, fast)
- **PostgreSQL**: Production (ACID, better concurrency, JSONB support, proven at scale)

**Redis Caching Strategy**:
```
Cache Pattern: user:{userId}
TTL: 1 hour
Invalidation: On user update/role change

Cache Pattern: user:session:{token}
TTL: Token expiry time
Invalidation: On logout
```

**Why Redis Here**: 
- User profile accessed on every authenticated request
- Reduces database load by 70-80%
- Sub-millisecond response time
- Session validation without DB hit

**Kafka Events Published**:
```
Topic: user.created
Topic: user.role.changed
Topic: user.deactivated
```

---

#### **2.2.2 Training Service**
**Port**: 8082  
**Responsibility**: Training content management, course creation, curriculum design

**Database Schema**:
```sql
trainings (
  id UUID PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  category VARCHAR(100),
  difficulty_level VARCHAR(20),
  duration_hours DECIMAL(5,2),
  thumbnail_url VARCHAR(500),
  created_by UUID,
  status VARCHAR(20),
  created_at TIMESTAMP,
  updated_at TIMESTAMP
)

training_modules (
  id UUID PRIMARY KEY,
  training_id UUID REFERENCES trainings(id),
  title VARCHAR(255),
  description TEXT,
  sequence_order INT,
  content_type VARCHAR(50),
  estimated_duration_minutes INT
)

training_content (
  id UUID PRIMARY KEY,
  module_id UUID REFERENCES training_modules(id),
  content_url VARCHAR(500),
  content_type VARCHAR(50),
  file_size BIGINT,
  metadata JSONB
)

training_prerequisites (
  training_id UUID REFERENCES trainings(id),
  prerequisite_training_id UUID REFERENCES trainings(id),
  PRIMARY KEY (training_id, prerequisite_training_id)
)

training_tags (
  training_id UUID REFERENCES trainings(id),
  tag VARCHAR(50),
  PRIMARY KEY (training_id, tag)
)

quizzes (
  id UUID PRIMARY KEY,
  module_id UUID REFERENCES training_modules(id),
  title VARCHAR(255),
  passing_score INT,
  time_limit_minutes INT
)

quiz_questions (
  id UUID PRIMARY KEY,
  quiz_id UUID REFERENCES quizzes(id),
  question_text TEXT,
  question_type VARCHAR(20),
  options JSONB,
  correct_answer JSONB,
  points INT
)
```

**REST APIs**:
- `POST /api/trainings` - Create training (Admin/Super Admin)
- `GET /api/trainings` - List trainings (with pagination, filters)
- `GET /api/trainings/{id}` - Get training details
- `PUT /api/trainings/{id}` - Update training
- `DELETE /api/trainings/{id}` - Soft delete training
- `POST /api/trainings/{id}/modules` - Add module
- `POST /api/trainings/{id}/publish` - Publish training
- `GET /api/trainings/categories` - Get categories
- `POST /api/trainings/{id}/quizzes` - Add quiz

**Content Storage Strategy**:
- **Videos**: AWS S3 / MinIO (object storage) + CloudFront CDN
- **Documents**: S3 with signed URLs (security)
- **Thumbnails**: S3 with aggressive caching
- **Metadata**: Database

**Redis Caching Strategy**:
```
Cache Pattern: training:{trainingId}
TTL: 6 hours

Cache Pattern: training:list:{filters_hash}
TTL: 30 minutes

Cache Pattern: training:popular
TTL: 1 hour
```

**Why Redis Here**:
- Training catalog is read-heavy (90% reads, 10% writes)
- Popular trainings accessed frequently
- Reduces DB load for listing operations
- Improves response time from 200ms to 10ms

**Kafka Events Published**:
```
Topic: training.created
Topic: training.published
Topic: training.updated
Topic: training.deleted
```

---

#### **2.2.3 Assignment Service**
**Port**: 8083  
**Responsibility**: Assign trainings to users, manage mandatory trainings, deadlines

**Database Schema**:
```sql
assignments (
  id UUID PRIMARY KEY,
  training_id UUID,
  assigned_by UUID,
  assignment_type VARCHAR(20), -- MANDATORY, OPTIONAL, RECOMMENDED
  due_date TIMESTAMP,
  created_at TIMESTAMP
)

user_assignments (
  id UUID PRIMARY KEY,
  assignment_id UUID REFERENCES assignments(id),
  user_id UUID,
  status VARCHAR(20), -- ASSIGNED, IN_PROGRESS, COMPLETED, OVERDUE
  assigned_at TIMESTAMP,
  started_at TIMESTAMP,
  completed_at TIMESTAMP,
  completion_percentage INT
)

mandatory_trainings (
  id UUID PRIMARY KEY,
  training_id UUID,
  role VARCHAR(50),
  department VARCHAR(100),
  effective_from TIMESTAMP,
  created_by UUID
)

assignment_reminders (
  id UUID PRIMARY KEY,
  user_assignment_id UUID REFERENCES user_assignments(id),
  reminder_type VARCHAR(20),
  scheduled_at TIMESTAMP,
  sent_at TIMESTAMP
)
```

**REST APIs**:
- `POST /api/assignments` - Create assignment (Admin/Super Admin)
- `POST /api/assignments/bulk` - Bulk assign to users/departments
- `GET /api/assignments/user/{userId}` - Get user assignments
- `PUT /api/assignments/{id}/status` - Update assignment status
- `POST /api/assignments/mandatory` - Create mandatory training rule
- `GET /api/assignments/overdue` - Get overdue assignments

**Kafka Integration - Critical for Scale**:

**Why Kafka Here**: 
When Super Admin assigns training to 1000 employees:
- **Without Kafka**: Synchronous = 1000 DB writes + 1000 notifications = 30-60 seconds, blocks API
- **With Kafka**: Publish event → return immediately (< 1 second), process asynchronously

**Scenario Example**:
```
Super Admin assigns "Security Training" to entire Engineering department (500 people)

Without Kafka:
1. API receives request
2. Loop through 500 users
3. Create 500 assignment records (DB writes)
4. Send 500 notifications
5. Return response after 45 seconds
6. User waits, potential timeout

With Kafka:
1. API receives request
2. Publish single event to Kafka
3. Return response in 800ms
4. Background consumers process assignments
5. Users receive notifications within 2-3 minutes
6. No API timeout, better UX
```

**Events Published**:
```
Topic: training.assigned
Partitions: 10
Payload: {
  assignmentId: UUID,
  trainingId: UUID,
  userIds: [UUID],
  assignedBy: UUID,
  dueDate: timestamp,
  assignmentType: string
}

Topic: training.mandatory.created
Payload: {
  trainingId: UUID,
  roles: [string],
  departments: [string],
  effectiveFrom: timestamp
}
```

**Events Consumed**:
```
Topic: training.completed → Update assignment status
Topic: user.role.changed → Auto-assign mandatory trainings
Topic: user.created → Auto-assign mandatory trainings for role
```

**Kafka Configuration**:
```yaml
Partitions: 10 (parallel processing)
Replication Factor: 3 (fault tolerance)
Consumer Groups: 
  - assignment-processor (creates assignments)
  - notification-processor (sends notifications)
Retention: 7 days
```

---

#### **2.2.4 Progress Service**
**Port**: 8084  
**Responsibility**: Track user progress, quiz scores, completion status

**Database Schema**:
```sql
user_progress (
  id UUID PRIMARY KEY,
  user_id UUID,
  training_id UUID,
  module_id UUID,
  progress_percentage INT,
  last_accessed_at TIMESTAMP,
  time_spent_minutes INT
)

quiz_attempts (
  id UUID PRIMARY KEY,
  user_id UUID,
  quiz_id UUID,
  score INT,
  max_score INT,
  attempt_number INT,
  answers JSONB,
  completed_at TIMESTAMP
)

video_progress (
  id UUID PRIMARY KEY,
  user_id UUID,
  video_id UUID,
  watched_duration INT,
  total_duration INT,
  last_position INT,
  completed BOOLEAN
)

completion_certificates (
  id UUID PRIMARY KEY,
  user_id UUID,
  training_id UUID,
  issued_at TIMESTAMP,
  certificate_url VARCHAR(500),
  verification_code VARCHAR(50)
)

learning_path_progress (
  id UUID PRIMARY KEY,
  user_id UUID,
  path_id UUID,
  completed_trainings INT,
  total_trainings INT,
  progress_percentage INT
)
```

**REST APIs**:
- `POST /api/progress/update` - Update progress
- `GET /api/progress/user/{userId}/training/{trainingId}` - Get progress
- `POST /api/progress/quiz/submit` - Submit quiz
- `GET /api/progress/quiz/{quizId}/attempts` - Get quiz attempts
- `POST /api/progress/video/update` - Update video progress
- `GET /api/progress/certificates/{userId}` - Get certificates
- `POST /api/progress/complete` - Mark training complete

**Kafka Integration - High Frequency Updates**:

**Why Kafka Here**:
- Progress updates are high-frequency (every 30 seconds for video, every quiz answer)
- 500 concurrent users × 2 updates/min = 1000 updates/min = 16 updates/sec
- Decouples progress tracking from user experience
- Enables real-time analytics without blocking user

**Scenario Example**:
```
User watching video training:

Without Kafka:
- Every 30 seconds: Direct DB write
- 500 users = 16 DB writes/sec
- Database becomes bottleneck
- Video player lags during save

With Kafka:
- Every 30 seconds: Publish to Kafka (< 5ms)
- Video player never lags
- Consumer batches 100 updates, writes once
- DB writes reduced from 16/sec to 0.16/sec (100x reduction)
```

**Events Published**:
```
Topic: progress.updated
Partitions: 20 (high throughput)
Payload: {
  userId: UUID,
  trainingId: UUID,
  moduleId: UUID,
  progressPercentage: int,
  timestamp: long
}

Topic: training.completed
Payload: {
  userId: UUID,
  trainingId: UUID,
  completedAt: timestamp,
  finalScore: int
}

Topic: quiz.submitted
Payload: {
  userId: UUID,
  quizId: UUID,
  score: int,
  passed: boolean
}
```

**Redis Caching Strategy**:
```
Cache Pattern: progress:{userId}:{trainingId}
TTL: 5 minutes
Strategy: Write-through cache

Cache Pattern: progress:leaderboard:{trainingId}
TTL: 10 minutes
```

**Why Redis Here**:
- Real-time progress display without DB queries
- Aggregates progress from multiple modules
- Reduces DB read load by 95%
- Enables instant progress bar updates

**Batch Processing**:
```java
@Scheduled(fixedDelay = 60000) // Every minute
public void batchProgressUpdates() {
    // Collect progress updates from Redis
    // Batch write to database
    // Clear Redis cache
}
```

---

#### **2.2.5 Notification Service**
**Port**: 8085  
**Responsibility**: Send notifications (email, in-app, push, SMS)

**Database Schema**:
```sql
notifications (
  id UUID PRIMARY KEY,
  user_id UUID,
  type VARCHAR(50),
  title VARCHAR(255),
  message TEXT,
  read_status BOOLEAN,
  action_url VARCHAR(500),
  created_at TIMESTAMP
)

notification_preferences (
  user_id UUID PRIMARY KEY,
  email_enabled BOOLEAN,
  push_enabled BOOLEAN,
  in_app_enabled BOOLEAN,
  sms_enabled BOOLEAN,
  digest_frequency VARCHAR(20)
)

notification_templates (
  id UUID PRIMARY KEY,
  type VARCHAR(50),
  channel VARCHAR(20),
  subject VARCHAR(255),
  body_template TEXT,
  variables JSONB
)

notification_delivery_log (
  id UUID PRIMARY KEY,
  notification_id UUID,
  channel VARCHAR(20),
  status VARCHAR(20),
  sent_at TIMESTAMP,
  error_message TEXT
)
```

**REST APIs**:
- `GET /api/notifications/user/{userId}` - Get notifications
- `PUT /api/notifications/{id}/read` - Mark as read
- `PUT /api/notifications/read-all` - Mark all as read
- `POST /api/notifications/preferences` - Update preferences
- `GET /api/notifications/unread-count` - Get unread count

**Kafka Integration - Decoupled Notification System**:

**Why Kafka Here**:
- Decouples notification sending from business logic
- Handles notification bursts (1000s of assignments)
- Retry mechanism for failed deliveries
- Multiple consumers (email, SMS, push) process independently
- Prevents notification failures from affecting core services

**Scenario Example**:
```
Super Admin assigns training to 1000 employees:

Without Kafka:
- Assignment Service tries to send 1000 emails
- Email service is slow (200ms per email)
- Total time: 200 seconds
- If email fails, assignment fails
- No retry mechanism

With Kafka:
- Assignment Service publishes event
- Returns immediately
- Notification Service consumes at its own pace
- Sends 1000 emails in parallel (10 workers)
- Time: 20 seconds
- Failed emails automatically retry
- Assignment success independent of notification
```

**Events Consumed**:
```
Topic: training.assigned → Send assignment notification
Topic: training.deadline.approaching → Send reminder
Topic: training.completed → Send completion notification
Topic: training.overdue → Send overdue alert
Topic: quiz.failed → Send retry encouragement
Topic: certificate.issued → Send certificate notification
```

**Implementation Example**:
```java
@KafkaListener(topics = "training.assigned", groupId = "notification-service")
public void handleTrainingAssigned(TrainingAssignedEvent event) {
    // Fetch user preferences from Redis
    UserPreferences prefs = redisCache.get("user:prefs:" + event.getUserId());
    
    if (prefs.isEmailEnabled()) {
        emailService.sendAsync(event.getUserId(), "training_assigned", event);
    }
    
    if (prefs.isPushEnabled()) {
        pushService.sendAsync(event.getUserId(), "New Training Assigned", event);
    }
    
    // Always create in-app notification
    createInAppNotification(event);
}
```

**Redis Caching**:
```
Cache Pattern: notifications:unread:{userId}
TTL: 5 minutes

Cache Pattern: user:prefs:{userId}
TTL: 1 hour
```

---

#### **2.2.6 Content Service**
**Port**: 8086  
**Responsibility**: Manage content storage, CDN, video streaming, file uploads

**Database Schema**:
```sql
content_metadata (
  id UUID PRIMARY KEY,
  content_type VARCHAR(50),
  file_name VARCHAR(255),
  file_size BIGINT,
  mime_type VARCHAR(100),
  storage_path VARCHAR(500),
  cdn_url VARCHAR(500),
  uploaded_by UUID,
  upload_status VARCHAR(20),
  created_at TIMESTAMP
)

video_metadata (
  content_id UUID PRIMARY KEY REFERENCES content_metadata(id),
  duration_seconds INT,
  resolution VARCHAR(20),
  codec VARCHAR(50),
  thumbnail_url VARCHAR(500),
  hls_playlist_url VARCHAR(500)
)

content_access_log (
  id UUID PRIMARY KEY,
  content_id UUID,
  user_id UUID,
  accessed_at TIMESTAMP,
  ip_address VARCHAR(50)
)
```

**REST APIs**:
- `POST /api/content/upload` - Upload content
- `GET /api/content/{id}` - Get content metadata
- `GET /api/content/{id}/signed-url` - Get signed URL for download
- `POST /api/content/video/transcode` - Trigger video transcoding
- `DELETE /api/content/{id}` - Delete content

**Storage Architecture**:
```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ Upload
       ▼
┌─────────────────┐
│ Content Service │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐      ┌──────────────┐
│   MinIO/S3      │─────▶│  CloudFront  │
│ Object Storage  │      │     CDN      │
└─────────────────┘      └──────────────┘
       │
       ▼
┌─────────────────┐
│ Video Transcoder│
│   (FFmpeg)      │
└─────────────────┘
```

**Why Object Storage (S3/MinIO)**:
- Scalable (unlimited storage)
- Cost-effective ($0.023/GB vs database storage)
- Built-in redundancy
- Direct browser upload (presigned URLs)
- CDN integration

**Why CDN (CloudFront)**:
- Reduces latency (edge locations)
- Reduces bandwidth costs (80% reduction)
- Handles traffic spikes
- Video streaming optimization

**Redis Caching**:
```
Cache Pattern: content:metadata:{contentId}
TTL: 24 hours

Cache Pattern: content:signed-url:{contentId}
TTL: 5 minutes (URL expiry time)
```

---

#### **2.2.7 Analytics Service**
**Port**: 8087  
**Responsibility**: Generate reports, analytics, data aggregation

**Database Schema**:
```sql
training_analytics (
  id UUID PRIMARY KEY,
  training_id UUID,
  total_enrollments INT,
  total_completions INT,
  average_score DECIMAL(5,2),
  average_completion_time_hours DECIMAL(8,2),
  completion_rate DECIMAL(5,2),
  calculated_at TIMESTAMP
)

user_analytics (
  id UUID PRIMARY KEY,
  user_id UUID,
  total_trainings_completed INT,
  total_hours_learned DECIMAL(8,2),
  average_quiz_score DECIMAL(5,2),
  last_activity_at TIMESTAMP,
  calculated_at TIMESTAMP
)

department_analytics (
  id UUID PRIMARY KEY,
  department VARCHAR(100),
  total_users INT,
  active_users INT,
  completion_rate DECIMAL(5,2),
  average_score DECIMAL(5,2),
  calculated_at TIMESTAMP
)

report_requests (
  id UUID PRIMARY KEY,
  requested_by UUID,
  report_type VARCHAR(50),
  filters JSONB,
  status VARCHAR(20),
  file_url VARCHAR(500),
  created_at TIMESTAMP,
  completed_at TIMESTAMP
)
```

**REST APIs**:
- `GET /api/analytics/training/{id}` - Get training analytics
- `GET /api/analytics/user/{id}` - Get user analytics
- `GET /api/analytics/department/{name}` - Get department analytics
- `POST /api/analytics/reports/generate` - Generate custom report
- `GET /api/analytics/reports/{id}` - Get report status/download
- `GET /api/analytics/dashboard` - Get dashboard metrics

**Python Integration for Report Generation**:

**Why Python Here**:
- Pandas: Powerful data manipulation (10x faster than Java for data processing)
- OpenPyXL: Excel generation with formatting
- Matplotlib/Seaborn: Chart generation
- NumPy: Statistical calculations
- Existing ML libraries for predictive analytics

**Architecture**:
```
┌──────────────────┐
│ Analytics Service│
│    (Java)        │
└────────┬─────────┘
         │ REST API
         ▼
┌──────────────────┐      ┌─────────────┐
│ Report Generator │─────▶│   Celery    │
│   (Python/Flask) │      │   Workers   │
└──────────────────┘      └─────────────┘
         │                       │
         ▼                       ▼
┌──────────────────┐      ┌─────────────┐
│   PostgreSQL     │      │    Redis    │
│   (Read Replica) │      │   (Queue)   │
└──────────────────┘      └─────────────┘
```

**Report Generation Flow**:
```python
# Python FastAPI endpoint
@app.post("/api/reports/generate")
async def generate_report(request: ReportRequest):
    # Create async task
    task = generate_excel_report.delay(
        report_type=request.report_type,
        filters=request.filters,
        user_id=request.user_id
    )
    return {"task_id": task.id, "status": "processing"}

# Celery task
@celery.task
def generate_excel_report(report_type, filters, user_id):
    # Fetch data from database
    df = pd.read_sql(query, db_connection)
    
    # Process data
    df['completion_rate'] = (df['completed'] / df['assigned']) * 100
    
    # Create Excel with formatting
    with pd.ExcelWriter('report.xlsx', engine='openpyxl') as writer:
        df.to_excel(writer, sheet_name='Summary')
        
        # Add charts
        workbook = writer.book
        chart = BarChart()
        # ... chart configuration
        
    # Upload to S3
    s3.upload_file('report.xlsx', bucket, key)
    
    # Publish completion event
    kafka_producer.send('report.generated', {
        'task_id': task.id,
        'user_id': user_id,
        'file_url': s3_url
    })
```

**Kafka Integration**:
```
Events Consumed:
- training.completed → Update analytics
- quiz.submitted → Update score analytics
- user.progress.updated → Update learning hours

Events Published:
- report.generated → Notify user
- analytics.calculated → Update dashboard
```

**Redis Caching**:
```
Cache Pattern: analytics:training:{trainingId}
TTL: 1 hour

Cache Pattern: analytics:dashboard:{userId}
TTL: 15 minutes
```

---

#### **2.2.8 Workflow Service**
**Port**: 8088  
**Responsibility**: Approval workflows, training publication, admin requests

**Database Schema**:
```sql
workflows (
  id UUID PRIMARY KEY,
  workflow_type VARCHAR(50),
  entity_id UUID,
  entity_type VARCHAR(50),
  initiated_by UUID,
  status VARCHAR(20),
  created_at TIMESTAMP,
  completed_at TIMESTAMP
)

workflow_steps (
  id UUID PRIMARY KEY,
  workflow_id UUID REFERENCES workflows(id),
  step_number INT,
  approver_role VARCHAR(50),
  approver_id UUID,
  status VARCHAR(20),
  comments TEXT,
  approved_at TIMESTAMP
)

workflow_templates (
  id UUID PRIMARY KEY,
  workflow_type VARCHAR(50),
  steps JSONB,
  created_by UUID
)
```

**REST APIs**:
- `POST /api/workflows/initiate` - Start workflow
- `GET /api/workflows/pending/{userId}` - Get pending approvals
- `POST /api/workflows/{id}/approve` - Approve step
- `POST /api/workflows/{id}/reject` - Reject workflow
- `GET /api/workflows/{id}/status` - Get workflow status

**Workflow Examples**:

1. **Training Publication Workflow**:
   - Admin creates training → Pending
   - Super Admin reviews → Approved/Rejected
   - If approved → Training published

2. **Admin Promotion Workflow**:
   - Super Admin initiates
   - HR approval required
   - IT approval required
   - User role updated

**Kafka Integration**:
```
Events Published:
- workflow.approved → Trigger action (publish training, update role)
- workflow.rejected → Notify creator

Events Consumed:
- training.created → Initiate approval workflow
- user.role.change.requested → Initiate approval workflow
```

---

## 3. API Gateway Architecture

### 3.1 API Gateway Responsibilities

```
┌─────────────────────────────────────────────────────────┐
│                      API Gateway                        │
│                                                         │
│  ┌─────────────────────────────────────────────────┐  │
│  │  1. Authentication & Authorization              │  │
│  │     - JWT validation                            │  │
│  │     - OAuth2/SAML integration                   │  │
│  │     - Role-based access control                 │  │
│  └─────────────────────────────────────────────────┘  │
│                                                         │
│  ┌─────────────────────────────────────────────────┐  │
│  │  2. Rate Limiting                               │  │
│  │     - Per user: 100 req/min                     │  │
│  │     - Per IP: 1000 req/min                      │  │
│  │     - Per endpoint: Custom limits               │  │
│  └─────────────────────────────────────────────────┘  │
│                                                         │
│  ┌─────────────────────────────────────────────────┐  │
│  │  3. Request Routing                             │  │
│  │     - Service discovery integration             │  │
│  │     - Load balancing                            │  │
│  │     - Circuit breaker                           │  │
│  └─────────────────────────────────────────────────┘  │
│                