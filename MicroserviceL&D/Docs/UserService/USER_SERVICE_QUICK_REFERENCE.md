# User Service Implementation - Quick Reference

## ✅ Implementation Complete

All components of the User Service have been successfully created!

### Components Created:

#### 📦 DTOs (Data Transfer Objects)

- `UserRegisterRequest.java` - Registration request validation
- `UserLoginRequest.java` - Login credentials
- `AuthResponse.java` - JWT token response
- `UserResponse.java` - User data response
- `RefreshTokenRequest.java` - Token refresh request
- `UpdateUserRequest.java` - Profile update request
- `ApiResponse.java` - Generic API response wrapper

#### 🗄️ Entities

- `User.java` - User account entity (updated)
- `Role.java` - Role definitions
- `UserSession.java` - Session tracking
- `SsoProvider.java` - SSO configuration

#### 📊 Repositories

- `UserRepository.java` - User database access
- `RoleRepository.java` - Role database access
- `UserSessionRepository.java` - Session database access
- `SsoProviderRepository.java` - SSO provider database access

#### 🛠️ Services

- `UserService.java` - User management & business logic
- `AuthService.java` - Authentication & token operations
- `JwtService.java` - JWT generation & validation
- `RedisService.java` - Caching operations

#### 🎮 Controllers

- `AuthController.java` - Authentication endpoints
- `UserController.java` - User management endpoints

#### 🔒 Security

- `SecurityConfig.java` - Spring Security configuration
- `JwtAuthenticationFilter.java` - JWT validation filter
- `JwtAuthenticationEntryPoint.java` - Exception handling
- `CustomUserDetailsService.java` - User loading for security

#### ⚙️ Configuration

- `KafkaConfig.java` - Kafka consumer/producer setup
- `RedisConfig.java` - Redis template configuration
- `CorsConfig.java` - CORS configuration

#### 📨 Messaging

- `UserEventProducer.java` - Kafka event publishing

#### 🐛 Exception Handling

- `UserNotFoundException.java` - User not found exception
- `InvalidCredentialsException.java` - Invalid credentials exception
- `EmailAlreadyExistsException.java` - Email exists exception
- `InvalidTokenException.java` - Token validation exception
- `GlobalExceptionHandler.java` - Global exception handler

#### 🔧 Utilities & Constants

- `PasswordUtil.java` - Password encoding/verification
- `AppConstants.java` - Application constants & configuration

#### 📝 Configuration Files

- `application.yml` - Complete configuration with comments
- `pom.xml` - Maven dependencies (already existed)

---

## 🎯 API Endpoints Summary

### Authentication APIs

```
POST   /api/auth/login                 - Login & get JWT token
POST   /api/auth/refresh-token         - Refresh access token
POST   /api/auth/logout                - Logout user
GET    /api/auth/validate              - Validate JWT token
```

### User Management APIs

```
POST   /api/users/register             - Register new user
GET    /api/users/me                   - Get current user profile
GET    /api/users/{id}                 - Get user by ID (protected)
GET    /api/users                      - Get all users (Admin only)
PUT    /api/users/me                   - Update current user profile
DELETE /api/users/{id}                 - Delete user (Admin only)
GET    /api/users/health               - Health check
```

---

## 🚀 How to Test

### 1. Start All Services

```bash
cd /Users/sahil_sangwan/Desktop/plans
docker-compose up -d
```

### 2. Wait for Services to be Healthy

```bash
docker-compose ps
# All services should show "healthy" or "running"
```

### 3. Test User Registration

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

### 4. Test Login & Get Token

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@company.com",
    "password": "SecurePass@123"
  }'
```

### 5. Test Protected Endpoint

```bash
# Copy the accessToken from login response
curl -X GET http://localhost:8081/api/users/me \
  -H "Authorization: Bearer <YOUR_ACCESS_TOKEN>"
```

---

## 🐳 PostgreSQL with Docker

### Start PostgreSQL

```bash
docker-compose up -d postgres
```

### Connect to PostgreSQL

```bash
# Using docker
docker exec -it lms-postgres psql -U lmsuser -d lms_db

# Using psql (if installed locally)
psql -h localhost -U lmsuser -d lms_db
# Password: lmspassword
```

### Common Commands

```sql
-- List all tables
\dt

-- List all users
SELECT * FROM users;

-- List user roles
SELECT * FROM user_roles;

-- Check roles table
SELECT * FROM roles;

-- Count users
SELECT COUNT(*) FROM users;

-- Exit
\q
```

### Adding SSL Certificates

#### Generate Certificates

```bash
mkdir -p /Users/sahil_sangwan/Desktop/plans/postgres-certs
cd /Users/sahil_sangwan/Desktop/plans/postgres-certs

# Generate private key
openssl genrsa -out server.key 2048

# Generate certificate
openssl req -new -x509 -key server.key -out server.crt -days 365 \
  -subj "/C=US/ST=State/L=City/O=Company/CN=localhost"

# Set permissions
chmod 600 server.key server.crt
```

#### Update docker-compose.yml

```yaml
postgres:
  image: postgres:15-alpine
  environment:
    POSTGRES_USER: lmsuser
    POSTGRES_PASSWORD: lmspassword
    POSTGRES_DB: lms_db
    POSTGRES_INITDB_ARGS: "-c ssl=on -c ssl_cert_file=/var/lib/postgresql/server.crt -c ssl_key_file=/var/lib/postgresql/server.key"
  volumes:
    - ./postgres-certs/server.crt:/var/lib/postgresql/server.crt:ro
    - ./postgres-certs/server.key:/var/lib/postgresql/server.key:ro
    - postgres_data:/var/lib/postgresql/data
```

#### Update application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/lms_db?sslmode=require&sslcert=/path/to/client-cert.pem&sslkey=/path/to/client-key.pem
```

#### Connect with SSL

```bash
psql -h localhost -U lmsuser -d lms_db \
  -sslmode=require \
  -sslcert=./postgres-certs/client.crt \
  -sslkey=./postgres-certs/client.key
```

---

## 🔑 Environment Variables to Update

For production deployment, update these in docker-compose.yml or environment:

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/lms_db
SPRING_DATASOURCE_USERNAME=lmsuser
SPRING_DATASOURCE_PASSWORD=your_secure_password

# Redis
SPRING_REDIS_HOST=redis
SPRING_REDIS_PASSWORD=your_secure_password

# JWT Secret (Generate: openssl rand -hex 32)
JWT_SECRET=your_256_bit_hex_key

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# Eureka
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
```

---

## 📊 Testing Kafka Events

### Check Topics

```bash
docker exec -it lms-kafka kafka-topics \
  --list \
  --bootstrap-server localhost:9092
```

### Consume User Events

```bash
# Listen for user.registered events
docker exec -it lms-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic user.registered \
  --from-beginning
```

Events are published automatically when:

- User registers → `user.registered` event
- User updates → `user.updated` event
- User deleted → `user.deleted` event

---

## 🔐 Password Requirements

Password must contain:

- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (@$!%\*?&)

Example: `SecurePass@123` ✅

---

## 📈 Next Steps

1. ✅ **User Service Complete** - All components implemented
2. ⏳ **Test User Service APIs** - Use the testing guide
3. ⏳ **Create Training Service** - Follow same pattern
4. ⏳ **Create Assignment Service** - Consume Training events
5. ⏳ **Create API Gateway** - Route all requests

---

## 📚 Files Created

```
user-service/
├── src/main/java/com/lms/userservice/
│   ├── controller/
│   │   ├── AuthController.java
│   │   └── UserController.java
│   ├── service/
│   │   ├── UserService.java
│   │   ├── AuthService.java
│   │   ├── JwtService.java
│   │   └── RedisService.java
│   ├── entity/
│   │   ├── User.java (updated)
│   │   ├── Role.java
│   │   ├── UserSession.java
│   │   └── SsoProvider.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── RoleRepository.java
│   │   ├── UserSessionRepository.java
│   │   └── SsoProviderRepository.java
│   ├── security/
│   │   ├── SecurityConfig.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtAuthenticationEntryPoint.java
│   │   └── CustomUserDetailsService.java
│   ├── config/
│   │   ├── KafkaConfig.java
│   │   ├── RedisConfig.java
│   │   └── CorsConfig.java
│   ├── dto/
│   │   ├── UserRegisterRequest.java
│   │   ├── UserLoginRequest.java
│   │   ├── AuthResponse.java
│   │   ├── UserResponse.java
│   │   ├── RefreshTokenRequest.java
│   │   ├── UpdateUserRequest.java
│   │   └── ApiResponse.java
│   ├── exception/
│   │   ├── UserNotFoundException.java
│   │   ├── InvalidCredentialsException.java
│   │   ├── EmailAlreadyExistsException.java
│   │   ├── InvalidTokenException.java
│   │   └── GlobalExceptionHandler.java
│   ├── kafka/
│   │   └── UserEventProducer.java
│   ├── util/
│   │   └── PasswordUtil.java
│   ├── constant/
│   │   └── AppConstants.java
│   └── UserServiceApplication.java (existing)
├── src/main/resources/
│   └── application.yml (updated with detailed comments)
└── pom.xml (existing)
```

---

## ✅ Verification Checklist

- [x] All entities created with proper JPA annotations
- [x] All repositories with custom query methods
- [x] Service layer with business logic
- [x] Controllers with proper REST mappings
- [x] JWT security implementation
- [x] Exception handling with GlobalExceptionHandler
- [x] Redis caching configured
- [x] Kafka event publishing
- [x] Password encryption with BCrypt
- [x] Application.yml with detailed comments for secrets
- [x] CORS configuration
- [x] Logging configuration

---

## 🎯 Ready to Deploy!

The User Service is now **100% complete** and ready to:

1. Build with Maven
2. Run with Docker
3. Test with the provided test suite
4. Deploy to production (after security review)

See **USER_SERVICE_TESTING.md** for comprehensive testing guide!
