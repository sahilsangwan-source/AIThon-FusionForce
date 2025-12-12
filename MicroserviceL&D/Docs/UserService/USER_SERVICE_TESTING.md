# User Service API Testing Guide

## 🚀 Quick Start - Running the Services

### Step 1: Start All Services with Docker Compose

```bash
cd /Users/sahil_sangwan/Desktop/plans

# Start all services (PostgreSQL, Redis, Kafka, Eureka, User Service)
docker-compose up -d

# Check if all services are running
docker-compose ps
```

### Step 2: Verify Services are Healthy

```bash
# Check PostgreSQL
docker-compose exec postgres pg_isready -U lmsuser

# Check Redis
docker-compose exec redis redis-cli -a redispassword ping

# Check Kafka
curl http://localhost:9092

# Check Eureka
curl http://localhost:8761

# Check User Service
curl http://localhost:8081/api/users/health
```

### Step 3: View Logs

```bash
# View logs for user-service
docker-compose logs -f user-service

# View logs for specific container
docker-compose logs -f postgres
docker-compose logs -f redis
docker-compose logs -f kafka
```

---

## 📝 Running PostgreSQL Container & Adding Certificates

### Option 1: Using Docker (Recommended)

The PostgreSQL container is already configured in docker-compose.yml:

```yaml
postgres:
  image: postgres:15-alpine
  container_name: lms-postgres
  environment:
    POSTGRES_USER: lmsuser
    POSTGRES_PASSWORD: lmspassword # Change this in production!
    POSTGRES_DB: lms_db
  ports:
    - "5432:5432"
  volumes:
    - postgres_data:/var/lib/postgresql/data
    - ./init-db.sql:/docker-entrypoint-initdb.d/init-db.sql
```

### Option 2: Connect to PostgreSQL Container

```bash
# Connect to PostgreSQL container
docker exec -it lms-postgres psql -U lmsuser -d lms_db

# List all tables
\dt

# List all users
SELECT * FROM users;

# Exit
\q
```

### Option 3: Use pgAdmin (Visual Interface)

```bash
# Add pgAdmin to docker-compose.yml
# Then access: http://localhost:5050
# Username: admin@example.com
# Password: admin
```

### Adding SSL/TLS Certificates to PostgreSQL

For production deployments, enable SSL certificates:

#### Step 1: Generate Self-Signed Certificate

```bash
# Create certificate directory
mkdir -p postgres-certs
cd postgres-certs

# Generate private key
openssl genrsa -out server.key 2048

# Generate certificate
openssl req -new -x509 -key server.key -out server.crt -days 365 \
  -subj "/C=US/ST=State/L=City/O=Company/CN=localhost"

# Set proper permissions
chmod 600 server.key
chmod 600 server.crt
```

#### Step 2: Update docker-compose.yml

```yaml
postgres:
  image: postgres:15-alpine
  environment:
    POSTGRES_USER: lmsuser
    POSTGRES_PASSWORD: lmspassword
    POSTGRES_INITDB_ARGS: "-c ssl=on -c ssl_cert_file=/var/lib/postgresql/server.crt -c ssl_key_file=/var/lib/postgresql/server.key"
  volumes:
    - ./postgres-certs/server.crt:/var/lib/postgresql/server.crt
    - ./postgres-certs/server.key:/var/lib/postgresql/server.key
    - postgres_data:/var/lib/postgresql/data
```

#### Step 3: Update application.yml for SSL

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/lms_db?sslmode=require
```

---

## 🧪 API Testing with cURL

### 1. Register a New User

```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@company.com",
    "password": "SecurePass@123",
    "firstName": "John",
    "lastName": "Doe",
    "employeeId": "EMP001",
    "department": "Engineering"
  }'
```

**Expected Response (201 Created):**

```json
{
  "status": 201,
  "message": "User registered successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "john.doe@company.com",
    "firstName": "John",
    "lastName": "Doe",
    "employeeId": "EMP001",
    "department": "Engineering",
    "status": "ACTIVE",
    "roles": ["EMPLOYEE"],
    "createdAt": "2024-12-12T10:30:00",
    "updatedAt": "2024-12-12T10:30:00"
  },
  "timestamp": "2024-12-12T10:30:00"
}
```

### 2. Login (Get JWT Token)

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@company.com",
    "password": "SecurePass@123"
  }'
```

**Expected Response (200 OK):**

```json
{
  "status": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "john.doe@company.com",
      "firstName": "John",
      "lastName": "Doe",
      "employeeId": "EMP001",
      "department": "Engineering",
      "status": "ACTIVE",
      "roles": ["EMPLOYEE"],
      "createdAt": "2024-12-12T10:30:00",
      "updatedAt": "2024-12-12T10:30:00"
    }
  },
  "timestamp": "2024-12-12T10:31:00"
}
```

### 3. Get Current User Profile

```bash
# Replace with actual access token from login response
ACCESS_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET http://localhost:8081/api/users/me \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

**Expected Response (200 OK):**

```json
{
  "status": 200,
  "message": "User profile retrieved",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "john.doe@company.com",
    "firstName": "John",
    "lastName": "Doe",
    "employeeId": "EMP001",
    "department": "Engineering",
    "status": "ACTIVE",
    "roles": ["EMPLOYEE"],
    "createdAt": "2024-12-12T10:30:00",
    "updatedAt": "2024-12-12T10:30:00"
  },
  "timestamp": "2024-12-12T10:32:00"
}
```

### 4. Update User Profile

```bash
curl -X PUT http://localhost:8081/api/users/me \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Smith",
    "department": "Management"
  }'
```

### 5. Refresh Access Token

```bash
curl -X POST http://localhost:8081/api/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

### 6. Validate Token

```bash
curl -X GET http://localhost:8081/api/auth/validate \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

**Expected Response (200 OK):**

```json
{
  "status": 200,
  "message": "Token validation result",
  "data": true,
  "timestamp": "2024-12-12T10:33:00"
}
```

### 7. Logout

```bash
curl -X POST http://localhost:8081/api/auth/logout \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 8. Get All Users (Admin Only)

```bash
# First login with admin account or user with ADMIN role
curl -X GET http://localhost:8081/api/users \
  -H "Authorization: Bearer $ADMIN_ACCESS_TOKEN"
```

### 9. Get User by ID

```bash
curl -X GET http://localhost:8081/api/users/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 10. Delete User (Admin Only)

```bash
curl -X DELETE http://localhost:8081/api/users/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer $ADMIN_ACCESS_TOKEN"
```

---

## 🧪 Testing with Postman

### Import Collection

Create a new Postman collection with the following requests:

1. **POST** `{{base_url}}/api/users/register`
2. **POST** `{{base_url}}/api/auth/login`
3. **GET** `{{base_url}}/api/users/me`
4. **PUT** `{{base_url}}/api/users/me`
5. **POST** `{{base_url}}/api/auth/refresh-token`
6. **GET** `{{base_url}}/api/auth/validate`
7. **POST** `{{base_url}}/api/auth/logout`
8. **GET** `{{base_url}}/api/users`
9. **GET** `{{base_url}}/api/users/{id}`
10. **DELETE** `{{base_url}}/api/users/{id}`

### Set Variables

```json
{
  "base_url": "http://localhost:8081",
  "access_token": "",
  "refresh_token": ""
}
```

### Pre-request Script for Token Refresh

```javascript
// Auto-refresh token if expired
if (
  pm.environment.get("access_token") &&
  pm.environment.get("token_expires_at") < Date.now()
) {
  // Token refresh logic
}
```

---

## 🔍 Testing with Insomnia

1. Create new folder: "LMS"
2. Create requests for all endpoints
3. Use environment variables for tokens
4. Test error scenarios

---

## 📊 Checking Kafka Events

```bash
# List Kafka topics
docker exec -it lms-kafka kafka-topics \
  --list \
  --bootstrap-server localhost:9092

# Consume user events
docker exec -it lms-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic user.registered \
  --from-beginning

# Check other topics
docker exec -it lms-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic user.updated \
  --from-beginning
```

---

## 🔐 Default Test Credentials

### Admin User (Created by init-db.sql)

```
Email: admin@company.com
Password: Admin@123
Role: SUPER_ADMIN
```

### Test User

```
Email: john.doe@company.com
Password: SecurePass@123
Role: EMPLOYEE
```

---

## ❌ Error Scenarios

### 1. Email Already Exists

```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@company.com",
    "password": "SecurePass@123",
    "firstName": "Jane",
    "lastName": "Doe",
    "employeeId": "EMP002",
    "department": "Engineering"
  }'

# Response (409 Conflict)
# "Email already registered: john.doe@company.com"
```

### 2. Invalid Password

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@company.com",
    "password": "WrongPassword"
  }'

# Response (401 Unauthorized)
# "Invalid email or password"
```

### 3. Invalid Token

```bash
curl -X GET http://localhost:8081/api/users/me \
  -H "Authorization: Bearer invalid.token.here"

# Response (401 Unauthorized)
# "JWT signature is invalid"
```

### 4. Missing Authorization

```bash
curl -X GET http://localhost:8081/api/users/me

# Response (401 Unauthorized)
# "Unauthorized: Full authentication is required"
```

### 5. Password Validation

```bash
# Password must be at least 8 characters with uppercase, lowercase, digit, and special character
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@company.com",
    "password": "weak",  # ❌ Too short, missing requirements
    "firstName": "Test",
    "lastName": "User",
    "employeeId": "EMP999",
    "department": "IT"
  }'

# Response (400 Bad Request)
# Validation errors for password
```

---

## 🚀 Building & Running Locally

### Build User Service

```bash
cd user-service

# Build with Maven
mvn clean package -DskipTests

# Run locally
mvn spring-boot:run
```

### Build Docker Image

```bash
cd user-service

# Build image
docker build -t lms-user-service:1.0.0 .

# Run container
docker run -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/lms_db \
  -e SPRING_REDIS_HOST=host.docker.internal \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:29092 \
  lms-user-service:1.0.0
```

---

## 📈 Performance Testing

### Load Testing with Apache Bench

```bash
# Test registration endpoint
ab -n 100 -c 10 -p register.json \
  -T application/json \
  http://localhost:8081/api/users/register

# Test login endpoint
ab -n 100 -c 10 -p login.json \
  -T application/json \
  http://localhost:8081/api/auth/login
```

### Load Testing with JMeter

1. Create test plan
2. Add HTTP Request sampler
3. Configure thread group
4. Run tests
5. Analyze results

---

## ✅ Checklist Before Production

- [ ] Change all default passwords
- [ ] Update JWT secret key
- [ ] Enable HTTPS/TLS
- [ ] Configure CORS properly
- [ ] Enable rate limiting
- [ ] Set up monitoring
- [ ] Configure backups
- [ ] Test disaster recovery
- [ ] Load testing completed
- [ ] Security audit passed

---

## 📞 Troubleshooting

### Services not starting

```bash
# Check logs
docker-compose logs user-service

# Restart service
docker-compose restart user-service

# Rebuild
docker-compose up -d --build user-service
```

### Database connection error

```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Check connection
docker exec -it lms-postgres psql -U lmsuser -c "SELECT 1"

# Recreate database
docker-compose down -v
docker-compose up -d postgres
```

### Redis connection error

```bash
# Check Redis is running
docker-compose ps redis

# Test connection
docker exec -it lms-redis redis-cli -a redispassword ping
```

### Kafka connection error

```bash
# Check Kafka is running
docker-compose ps kafka

# Check topics
docker exec -it lms-kafka kafka-topics --list --bootstrap-server localhost:9092
```

---

**For detailed documentation, see:**

- IMPLEMENTATION_GUIDE.md
- README.md
- learning-portal-architecture.md
