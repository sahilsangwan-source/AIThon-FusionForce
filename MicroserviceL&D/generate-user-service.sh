#!/bin/bash

# Script to generate complete User Service structure
# This creates all necessary files for the User Service

echo "🚀 Generating User Service..."

# Create directory structure
mkdir -p user-service/src/main/java/com/lms/userservice/{entity,repository,service,controller,dto,config,security,util,kafka}
mkdir -p user-service/src/main/resources
mkdir -p user-service/src/test/java/com/lms/userservice

echo "✅ Directory structure created"

# The pom.xml and application.yml are already created
# Now we'll create a comprehensive README for implementing the service

cat > user-service/README.md << 'EOF'
# User Service Implementation

## Overview
User Service handles authentication, authorization, and user management.

## Files to Create

### 1. Entity Classes (entity/)
- `User.java` - Main user entity
- `Role.java` - Role entity
- `UserSession.java` - Session management
- `SsoProvider.java` - SSO configuration

### 2. Repository Classes (repository/)
- `UserRepository.java`
- `RoleRepository.java`
- `UserSessionRepository.java`

### 3. DTO Classes (dto/)
- `LoginRequest.java`
- `LoginResponse.java`
- `RegisterRequest.java`
- `UserResponse.java`
- `RefreshTokenRequest.java`

### 4. Service Classes (service/)
- `UserService.java`
- `AuthService.java`
- `JwtService.java`
- `RedisService.java`

### 5. Controller Classes (controller/)
- `AuthController.java`
- `UserController.java`

### 6. Security Configuration (security/)
- `SecurityConfig.java`
- `JwtAuthenticationFilter.java`
- `JwtAuthenticationEntryPoint.java`

### 7. Kafka Configuration (kafka/)
- `KafkaProducerConfig.java`
- `UserEventProducer.java`
- `UserEvent.java`

### 8. Utilities (util/)
- `JwtUtil.java`
- `PasswordUtil.java`

## Implementation Steps

1. Create all entity classes with JPA annotations
2. Create repository interfaces extending JpaRepository
3. Implement service layer with business logic
4. Create REST controllers with proper endpoints
5. Configure Spring Security with JWT
6. Set up Redis caching
7. Configure Kafka producers for events
8. Add validation and error handling
9. Write unit tests
10. Create Dockerfile

## Key Features

- JWT-based authentication
- Password encryption with BCrypt
- Redis caching for user profiles and sessions
- Kafka events for user lifecycle
- Role-based access control
- Session management
- SSO integration support

## API Endpoints

### Authentication
- POST /api/users/register - Register new user
- POST /api/users/login - Login with credentials
- POST /api/users/refresh-token - Refresh access token
- POST /api/users/logout - Logout user
- POST /api/users/sso/oauth2 - OAuth2 SSO login
- POST /api/users/sso/saml - SAML SSO login

### User Management
- GET /api/users/me - Get current user profile
- GET /api/users/{id} - Get user by ID
- PUT /api/users/{id} - Update user profile
- DELETE /api/users/{id} - Deactivate user
- GET /api/users/search - Search users (Admin)
- POST /api/users/roles - Assign roles (Super Admin)

## Environment Variables

```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/lms_db
SPRING_DATASOURCE_USERNAME: lmsuser
SPRING_DATASOURCE_PASSWORD: lmspassword
SPRING_REDIS_HOST: redis
SPRING_REDIS_PORT: 6379
SPRING_REDIS_PASSWORD: redispassword
SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
JWT_SECRET: your-secret-key-here
```

## Testing

```bash
# Build
mvn clean package

# Run tests
mvn test

# Run locally
mvn spring-boot:run

# Build Docker image
docker build -t lms-user-service .
```

## Kafka Events Published

- `user.created` - When new user registers
- `user.updated` - When user profile is updated
- `user.role.changed` - When user role is modified
- `user.deactivated` - When user is deactivated

## Redis Caching

- `user:{userId}` - User profile (TTL: 1 hour)
- `user:session:{token}` - Session data (TTL: token expiry)
- `user:email:{email}` - Email to user ID mapping (TTL: 1 hour)
EOF

echo "✅ User Service README created"

# Create Dockerfile
cat > user-service/Dockerfile << 'EOF'
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Add wait-for-it script for service dependencies
ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

EXPOSE 8081

# Wait for dependencies before starting
ENTRYPOINT ["/wait-for-it.sh", "postgres:5432", "--", "/wait-for-it.sh", "redis:6379", "--", "/wait-for-it.sh", "kafka:9092", "--", "java", "-jar", "app.jar"]
EOF

echo "✅ Dockerfile created"

echo "
🎉 User Service structure generated successfully!

📁 Next Steps:
1. Review user-service/README.md for implementation details
2. Implement entity classes in entity/ directory
3. Create repositories in repository/ directory
4. Implement services in service/ directory
5. Create controllers in controller/ directory
6. Configure security in security/ directory
7. Set up Kafka in kafka/ directory

📝 Quick Start:
cd user-service
mvn clean install

🐳 Docker Build:
cd user-service
docker build -t lms-user-service .

For detailed API documentation, see IMPLEMENTATION_GUIDE.md
"

EOF

chmod +x generate-user-service.sh

echo "✅ Script created successfully!"