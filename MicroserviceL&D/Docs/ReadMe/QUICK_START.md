# 🚀 Quick Start Guide - Learning & Development Portal

## ⚡ Get Started in 5 Minutes

This guide will help you get the Learning & Development Portal up and running quickly.

---

## 📋 Prerequisites Check

```bash
# Check Docker
docker --version
# Should show: Docker version 20.x or higher

# Check Docker Compose
docker-compose --version
# Should show: Docker Compose version 2.x or higher
```

---

## 🏃 Quick Start Steps

### Step 1: Clone/Navigate to Project

```bash
cd /Users/sahil_sangwan/Desktop/plans
```

### Step 2: Start Infrastructure Services

```bash
# Start only infrastructure (PostgreSQL, Redis, Kafka, MinIO, Eureka)
docker-compose up -d postgres redis zookeeper kafka minio eureka-server

# Wait for services to be healthy (30-60 seconds)
docker-compose ps
```

### Step 3: Verify Infrastructure

```bash
# Check PostgreSQL
docker exec -it lms-postgres psql -U lmsuser -d lms_db -c "SELECT COUNT(*) FROM users;"

# Check Redis
docker exec -it lms-redis redis-cli -a redispassword ping

# Check Kafka
docker exec -it lms-kafka kafka-topics --list --bootstrap-server localhost:9092

# Check MinIO
curl http://localhost:9000/minio/health/live
```

### Step 4: Access Services

Open in your browser:

- **Eureka Dashboard**: http://localhost:8761
- **MinIO Console**: http://localhost:9001 (minioadmin / minioadmin123)
- **PostgreSQL**: localhost:5432 (lmsuser / lmspassword)
- **Redis**: localhost:6379 (password: redispassword)
- **Kafka**: localhost:29092

---

## 🧪 Test the Setup

### Test 1: Database Connection

```bash
docker exec -it lms-postgres psql -U lmsuser -d lms_db << EOF
-- Check if tables exist
\dt

-- Check default admin user
SELECT email, role, status FROM users WHERE email = 'admin@company.com';

-- Check roles
SELECT * FROM roles;
EOF
```

**Expected Output:**
```
email                  | role        | status
-----------------------|-------------|--------
admin@company.com      | SUPER_ADMIN | ACTIVE
```

### Test 2: Redis Connection

```bash
docker exec -it lms-redis redis-cli -a redispassword << EOF
PING
SET test:key "Hello LMS"
GET test:key
DEL test:key
EOF
```

**Expected Output:**
```
PONG
OK
"Hello LMS"
(integer) 1
```

### Test 3: Kafka Topics

```bash
# Create test topic
docker exec -it lms-kafka kafka-topics \
  --create \
  --bootstrap-server localhost:9092 \
  --topic test-topic \
  --partitions 3 \
  --replication-factor 1

# List topics
docker exec -it lms-kafka kafka-topics \
  --list \
  --bootstrap-server localhost:9092
```

### Test 4: MinIO Bucket Creation

```bash
# Install MinIO client (mc)
docker run --rm --network plans_lms-network \
  minio/mc alias set myminio http://minio:9000 minioadmin minioadmin123

# Create bucket for training content
docker run --rm --network plans_lms-network \
  minio/mc mb myminio/training-content
```

---

## 🔧 Building Microservices

### Option A: Build All Services

```bash
# Build all services at once
docker-compose build

# Start all services
docker-compose up -d

# Check status
docker-compose ps
```

### Option B: Build Individual Services

```bash
# Build User Service
cd user-service
mvn clean package -DskipTests
docker build -t lms-user-service .

# Build Training Service
cd ../training-service
mvn clean package -DskipTests
docker build -t lms-training-service .

# Continue for other services...
```

---

## 📡 Testing APIs

### Using cURL

#### 1. Login as Admin

```bash
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@company.com",
    "password": "Admin@123"
  }' | jq
```

**Save the access token from response:**
```bash
export TOKEN="your_access_token_here"
```

#### 2. Get User Profile

```bash
curl -X GET http://localhost:8081/api/users/me \
  -H "Authorization: Bearer $TOKEN" | jq
```

#### 3. Register New User

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
  }' | jq
```

#### 4. Create Training

```bash
curl -X POST http://localhost:8082/api/trainings \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Introduction to Microservices",
    "description": "Learn microservices architecture",
    "category": "Architecture",
    "difficultyLevel": "INTERMEDIATE",
    "durationHours": 8.0
  }' | jq
```

---

## 🐛 Troubleshooting

### Issue: Services not starting

```bash
# Check logs
docker-compose logs [service-name]

# Common issues:
# 1. Port already in use
lsof -i :8081  # Check what's using the port
kill -9 <PID>  # Kill the process

# 2. Database not ready
docker-compose restart postgres
docker-compose logs postgres

# 3. Kafka not ready
docker-compose restart kafka zookeeper
docker-compose logs kafka
```

### Issue: Database connection failed

```bash
# Verify PostgreSQL is running
docker-compose ps postgres

# Check if database exists
docker exec -it lms-postgres psql -U lmsuser -l

# Recreate database
docker-compose down -v
docker-compose up -d postgres
# Wait 30 seconds for init-db.sql to run
```

### Issue: Redis connection failed

```bash
# Test Redis
docker exec -it lms-redis redis-cli -a redispassword ping

# Restart Redis
docker-compose restart redis
```

### Issue: Kafka connection failed

```bash
# Check Kafka status
docker-compose ps kafka zookeeper

# Restart Kafka stack
docker-compose restart zookeeper kafka

# Verify Kafka is ready
docker exec -it lms-kafka kafka-broker-api-versions \
  --bootstrap-server localhost:9092
```

---

## 📊 Monitoring

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f user-service

# Last 100 lines
docker-compose logs --tail=100 user-service
```

### Check Service Health

```bash
# User Service
curl http://localhost:8081/actuator/health | jq

# Training Service
curl http://localhost:8082/actuator/health | jq

# All services
for port in 8081 8082 8083 8084 8085 8086 8087 8088; do
  echo "Checking port $port..."
  curl -s http://localhost:$port/actuator/health | jq '.status'
done
```

### Monitor Resources

```bash
# Container stats
docker stats

# Disk usage
docker system df

# Network inspection
docker network inspect plans_lms-network
```

---

## 🧹 Cleanup

### Stop Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v

# Remove images
docker-compose down --rmi all
```

### Clean Docker System

```bash
# Remove unused containers
docker container prune -f

# Remove unused images
docker image prune -a -f

# Remove unused volumes
docker volume prune -f

# Complete cleanup
docker system prune -a --volumes -f
```

---

## 📚 Next Steps

1. **Explore APIs**: Use the Postman collection (see API_TESTING.md)
2. **Add Data**: Create users, trainings, and assignments
3. **Monitor**: Check Eureka dashboard and service logs
4. **Develop**: Start implementing additional features
5. **Test**: Run integration tests

---

## 🎯 Common Tasks

### Add New User

```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@company.com",
    "password": "Password@123",
    "firstName": "Test",
    "lastName": "User",
    "employeeId": "EMP003",
    "department": "IT"
  }'
```

### Assign Training

```bash
# First, get training ID and user ID from database
# Then assign:
curl -X POST http://localhost:8083/api/assignments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "trainingId": "TRAINING_UUID",
    "userId": "USER_UUID",
    "assignmentType": "MANDATORY",
    "dueDate": "2024-12-31T23:59:59"
  }'
```

### Check Progress

```bash
curl -X GET http://localhost:8084/api/progress/user/USER_UUID/training/TRAINING_UUID \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🔐 Security Notes

⚠️ **Important**: This is a development setup. For production:

1. Change all default passwords
2. Use environment variables for secrets
3. Enable HTTPS/TLS
4. Configure firewall rules
5. Set up proper backup strategy
6. Enable audit logging
7. Implement rate limiting
8. Use strong JWT secrets

---

## 📞 Getting Help

If you encounter issues:

1. Check the logs: `docker-compose logs [service-name]`
2. Verify all services are running: `docker-compose ps`
3. Check the troubleshooting section above
4. Review IMPLEMENTATION_GUIDE.md for detailed information

---

## ✅ Success Checklist

- [ ] All infrastructure services running (postgres, redis, kafka, minio)
- [ ] Eureka dashboard accessible at http://localhost:8761
- [ ] Can login with admin@company.com / Admin@123
- [ ] Can create new users
- [ ] Can create trainings
- [ ] Can assign trainings
- [ ] Can track progress
- [ ] All health checks passing

---

**🎉 Congratulations! Your Learning & Development Portal is ready!**

For detailed API documentation and advanced features, see:
- `IMPLEMENTATION_GUIDE.md` - Complete implementation guide
- `API_TESTING.md` - API testing examples
- `architecture-part2.md` - Architecture details