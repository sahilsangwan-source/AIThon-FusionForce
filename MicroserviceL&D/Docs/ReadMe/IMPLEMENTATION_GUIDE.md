# Learning & Development Portal - Implementation Guide

## 🚀 Quick Start

This guide provides step-by-step instructions to implement and run the Learning & Development Portal.

---

## 📋 Prerequisites

- Docker & Docker Compose installed
- Java 17+ (for local development)
- Maven 3.9+ (for local development)
- Python 3.11+ (for Analytics Service)
- Git

---

## 🏗️ Project Structure

```
learning-portal/
├── docker-compose.yml          # Main orchestration file
├── init-db.sql                 # Database initialization
├── eureka-server/              # Service Discovery (Port 8761)
├── api-gateway/                # API Gateway (Port 8080)
├── user-service/               # User Management (Port 8081)
├── training-service/           # Training Content (Port 8082)
├── assignment-service/         # Training Assignment (Port 8083)
├── progress-service/           # Progress Tracking (Port 8084)
├── notification-service/       # Notifications (Port 8085)
├── content-service/            # Media Storage (Port 8086)
├── analytics-service/          # Analytics & Reports (Port 8087)
└── workflow-service/           # Approval Workflows (Port 8088)
```

---

## 🔧 Infrastructure Components

### Docker Images Used:
- **PostgreSQL**: `postgres:15-alpine`
- **Redis**: `redis:7-alpine`
- **Kafka**: `confluentinc/cp-kafka:7.5.0`
- **Zookeeper**: `confluentinc/cp-zookeeper:7.5.0`
- **MinIO**: `minio/minio:latest`

### Default Credentials:

#### PostgreSQL
```
Host: localhost:5432
Database: lms_db
Username: lmsuser
Password: lmspassword
```

#### Redis
```
Host: localhost:6379
Password: redispassword
```

#### Kafka
```
Bootstrap Servers: localhost:29092
```

#### MinIO (S3 Compatible)
```
Endpoint: http://localhost:9000
Console: http://localhost:9001
Access Key: minioadmin
Secret Key: minioadmin123
```

#### Default Super Admin
```
Email: admin@company.com
Password: Admin@123
Role: SUPER_ADMIN
```

---

## 🚀 Running the Application

### Option 1: Using Docker Compose (Recommended)

```bash
# Start all services
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f [service-name]

# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

### Option 2: Local Development

```bash
# Start infrastructure only
docker-compose up -d postgres redis kafka zookeeper minio

# Run each service locally
cd user-service
mvn spring-boot:run

# In separate terminals, run other services
cd training-service
mvn spring-boot:run
```

---

## 📡 Service Endpoints

### Eureka Dashboard
```
http://localhost:8761
```

### API Gateway
```
http://localhost:8080
```

### MinIO Console
```
http://localhost:9001
```

### Individual Services (Direct Access)
- User Service: http://localhost:8081
- Training Service: http://localhost:8082
- Assignment Service: http://localhost:8083
- Progress Service: http://localhost:8084
- Notification Service: http://localhost:8085
- Content Service: http://localhost:8086
- Analytics Service: http://localhost:8087
- Workflow Service: http://localhost:8088

---

## 🧪 API Testing

### 1. User Registration

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

### 2. User Login

```bash
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@company.com",
    "password": "Admin@123"
  }'
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### 3. Get User Profile

```bash
curl -X GET http://localhost:8081/api/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 4. Create Training (Admin/Super Admin)

```bash
curl -X POST http://localhost:8082/api/trainings \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Java Spring Boot Fundamentals",
    "description": "Learn Spring Boot from scratch",
    "category": "Programming",
    "difficultyLevel": "BEGINNER",
    "durationHours": 10.5
  }'
```

### 5. List Trainings

```bash
curl -X GET "http://localhost:8082/api/trainings?page=0&size=20" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 6. Assign Training to Users (Bulk)

```bash
curl -X POST http://localhost:8083/api/assignments/bulk \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "trainingId": "TRAINING_UUID",
    "userIds": ["USER_UUID_1", "USER_UUID_2"],
    "assignmentType": "MANDATORY",
    "dueDate": "2024-12-31T23:59:59"
  }'
```

### 7. Update Progress

```bash
curl -X POST http://localhost:8084/api/progress/update \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "trainingId": "TRAINING_UUID",
    "moduleId": "MODULE_UUID",
    "progressPercentage": 50,
    "timeSpentMinutes": 30
  }'
```

### 8. Get User Assignments

```bash
curl -X GET http://localhost:8083/api/assignments/user/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 9. Get Notifications

```bash
curl -X GET http://localhost:8085/api/notifications/user/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 10. Generate Analytics Report

```bash
curl -X POST http://localhost:8087/api/analytics/reports/generate \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reportType": "USER_COMPLETION",
    "filters": {
      "startDate": "2024-01-01",
      "endDate": "2024-12-31",
      "department": "Engineering"
    }
  }'
```

---

## 🔐 Authentication Flow

### JWT Token Structure

```json
{
  "sub": "user-uuid",
  "email": "user@company.com",
  "roles": ["EMPLOYEE", "ADMIN"],
  "permissions": ["read:trainings", "write:progress"],
  "iat": 1702345678,
  "exp": 1702349278
}
```

### Using JWT in Requests

All authenticated requests must include the JWT token in the Authorization header:

```
Authorization: Bearer YOUR_ACCESS_TOKEN
```

### Token Refresh

When access token expires, use refresh token:

```bash
curl -X POST http://localhost:8081/api/users/refresh-token \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

---

## 📊 Monitoring & Health Checks

### Service Health

```bash
# Check all services
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
# ... and so on for other services
```

### Eureka Dashboard

Visit http://localhost:8761 to see all registered services

### Database Connection

```bash
# Connect to PostgreSQL
docker exec -it lms-postgres psql -U lmsuser -d lms_db

# List tables
\dt

# Query users
SELECT * FROM users;
```

### Redis Connection

```bash
# Connect to Redis
docker exec -it lms-redis redis-cli -a redispassword

# Check keys
KEYS *

# Get cached user
GET user:USER_UUID
```

### Kafka Topics

```bash
# List topics
docker exec -it lms-kafka kafka-topics --list --bootstrap-server localhost:9092

# Consume messages
docker exec -it lms-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic training.assigned \
  --from-beginning
```

---

## 🐛 Troubleshooting

### Services Not Starting

```bash
# Check logs
docker-compose logs [service-name]

# Restart specific service
docker-compose restart [service-name]

# Rebuild service
docker-compose up -d --build [service-name]
```

### Database Connection Issues

```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Check PostgreSQL logs
docker-compose logs postgres

# Verify database exists
docker exec -it lms-postgres psql -U lmsuser -l
```

### Kafka Connection Issues

```bash
# Check Kafka and Zookeeper
docker-compose ps kafka zookeeper

# Check Kafka logs
docker-compose logs kafka

# Verify Kafka is ready
docker exec -it lms-kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

### Port Already in Use

```bash
# Find process using port
lsof -i :8081

# Kill process
kill -9 PID
```

---

## 📝 Development Workflow

### Adding a New Microservice

1. Create service directory
2. Add `pom.xml` with dependencies
3. Create `application.yml` configuration
4. Implement entities, repositories, services, controllers
5. Add to `docker-compose.yml`
6. Create `Dockerfile`
7. Register with Eureka
8. Add routes to API Gateway

### Database Migrations

```bash
# Add new migration to init-db.sql
# Recreate database
docker-compose down -v
docker-compose up -d postgres
```

### Testing Kafka Events

```bash
# Produce test event
docker exec -it lms-kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic training.assigned

# Then type your JSON message
```

---

## 🔒 Security Considerations

### Production Checklist

- [ ] Change all default passwords
- [ ] Use environment variables for secrets
- [ ] Enable HTTPS/TLS
- [ ] Configure CORS properly
- [ ] Enable rate limiting
- [ ] Set up firewall rules
- [ ] Enable database encryption
- [ ] Configure backup strategy
- [ ] Set up monitoring alerts
- [ ] Enable audit logging

### Environment Variables for Production

```bash
# Database
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=strong_password

# Redis
export SPRING_REDIS_PASSWORD=redis_strong_password

# JWT
export JWT_SECRET=your_256_bit_secret_key

# MinIO
export MINIO_ROOT_USER=prod_minio_user
export MINIO_ROOT_PASSWORD=minio_strong_password
```

---

## 📈 Performance Optimization

### Redis Caching Strategy

```
- User profiles: TTL 1 hour
- Training catalog: TTL 6 hours
- Progress data: TTL 5 minutes
- Session tokens: TTL = token expiry
```

### Database Indexing

All critical indexes are created in `init-db.sql`:
- User email, employee_id
- Training category, status
- Assignment user_id, status
- Progress user_id, training_id

### Kafka Partitioning

```
- training.assigned: 10 partitions
- progress.updated: 20 partitions (high throughput)
- notification.*: 5 partitions
```

---

## 🎯 Next Steps

1. **Complete User Service Implementation**
   - Finish all entity classes
   - Implement repositories
   - Create service layer
   - Build REST controllers
   - Add JWT utilities

2. **Implement Remaining Services**
   - Follow same pattern as User Service
   - Add Kafka producers/consumers
   - Implement Redis caching
   - Add validation and error handling

3. **Build API Gateway**
   - Configure routes
   - Add authentication filter
   - Implement rate limiting
   - Add circuit breakers

4. **Create Frontend**
   - React application
   - Authentication flow
   - Training catalog
   - Progress tracking
   - Admin dashboard

5. **Add Monitoring**
   - Prometheus metrics
   - Grafana dashboards
   - ELK stack for logging
   - Alert configuration

---

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Redis Documentation](https://redis.io/documentation)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---

## 🤝 Support

For issues or questions:
1. Check the troubleshooting section
2. Review service logs
3. Verify all services are running
4. Check network connectivity between services

---

**Note**: This is a development setup. For production deployment, additional security hardening, monitoring, and infrastructure setup is required.