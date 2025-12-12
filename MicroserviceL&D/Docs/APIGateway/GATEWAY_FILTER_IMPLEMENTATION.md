# API Gateway Filter & Security Implementation

## Overview
This document describes the comprehensive gateway filter and security implementation that prevents direct access to microservices and centralizes authentication at the API Gateway level.

## Architecture

### Request Flow
```
External Client
    ↓
    ├─→ API Gateway (Port 8080) ← ONLY PUBLIC ENDPOINT
    │   ├─ JWT Authentication
    │   ├─ Request Path Transformation
    │   ├─ Header Enrichment
    │   └─ Rate Limiting
    │
    └─→ Internal Network (Docker)
        ├─→ User Service (Port 8081 - INTERNAL ONLY)
        │   ├─ Validates X-Gateway-Authenticated Header
        │   └─ Receives User Info via Headers
        │
        └─→ Training Service (Port 8082 - INTERNAL ONLY)
            ├─ Validates X-Gateway-Authenticated Header
            └─ Receives User Info via Headers
```

## Components

### 1. API Gateway Filters

#### a) JwtAuthenticationFilter
**Location:** `api-gateway/src/main/java/com/lms/gateway/filter/JwtAuthenticationFilter.java`

**Purpose:** Validates JWT tokens on protected routes

**Features:**
- Extracts Bearer token from Authorization header
- Validates token signature and expiration
- Extracts user information (userId, email, role)
- Adds custom headers to requests for downstream services
- Rejects requests without valid JWT

**Headers Added:**
```
X-User-Id: <user_id>
X-User-Email: <user_email>
X-User-Role: <user_role>
X-Authenticated-By: API-GATEWAY
X-Authentication-Time: <timestamp>
```

#### b) RequestHeaderEnhancerFilter
**Location:** `api-gateway/src/main/java/com/lms/gateway/filter/RequestHeaderEnhancerFilter.java`

**Purpose:** Marks all requests as coming from the API Gateway

**Features:**
- Adds X-Gateway-Authenticated header
- Adds X-Request-Source header
- Adds X-Forwarded-By header
- Applied to ALL routes as default filter

**Headers Added:**
```
X-Gateway-Authenticated: true
X-Request-Source: API-GATEWAY
X-Forwarded-By: api-gateway
```

#### c) RequestTransformationFilter
**Location:** `api-gateway/src/main/java/com/lms/gateway/filter/RequestTransformationFilter.java`

**Purpose:** Placeholder for custom request transformations

**Features:**
- Available for future path transformations
- Works with StripPrefix to transform paths

### 2. Microservice Validation Filters

#### GatewayAccessValidationFilter
**Location (User Service):** `user-service/src/main/java/com/lms/userservice/config/GatewayAccessValidationFilter.java`
**Location (Training Service):** `training-service/src/main/java/com/lms/trainingservice/config/GatewayAccessValidationFilter.java`

**Purpose:** Ensures all requests come from the API Gateway

**Features:**
- Checks for X-Gateway-Authenticated header
- Checks for X-Request-Source header
- Rejects requests with HTTP 403 if not from gateway
- Skips validation for health checks and actuator endpoints
- Can be disabled via `gateway.require-gateway-auth: false` in config

**Response on Direct Access Attempt:**
```json
{
  "error": "Access Denied",
  "message": "Requests must come through API Gateway"
}
```

## Route Configuration

### User Service Routes

#### Public Routes (No Authentication Required)
```yaml
/api/auth/register  → POST to user-service:/auth/register
/api/auth/login     → POST to user-service:/auth/login
/api/auth/refresh   → POST to user-service:/auth/refresh
```

#### Protected Routes (JWT Required)
```yaml
/api/users/**       → user-service:/users/**
/api/user/profile/** → user-service:/user/profile/**
```

**Route Definition Example:**
```yaml
- id: user-auth-login
  uri: lb://USER-SERVICE
  predicates:
    - Path=/api/auth/login
    - Method=POST
  filters:
    - StripPrefix=1
```

- `uri: lb://USER-SERVICE` - Load-balanced route to USER-SERVICE registered in Eureka
- `StripPrefix=1` - Removes `/api` prefix before forwarding
- So `/api/auth/login` becomes `/auth/login` on the service

### Training Service Routes

#### Public Routes
```yaml
/api/training/public/**   → TRAINING-SERVICE:/public/**
/api/training/search      → TRAINING-SERVICE:/search
/api/training/published   → TRAINING-SERVICE:/published
```

#### Protected Routes
```yaml
/api/training/courses/**       → TRAINING-SERVICE:/courses/**
/api/training/modules/**       → TRAINING-SERVICE:/modules/**
/api/training/assignments/**   → TRAINING-SERVICE:/assignments/**
/api/training/completion/**    → TRAINING-SERVICE:/completion/**
/api/training/enrollments/**   → TRAINING-SERVICE:/enrollments/**
```

### Catch-All Route
Any request not matching above routes is **rejected**.
```yaml
- id: reject-unknown-routes
  uri: no://op
  predicates:
    - Path=/**
```

## Path Transformation Examples

### Example 1: User Login
```
Client Request:
  POST /api/auth/login
  
API Gateway:
  1. Receives request
  2. Matches route: user-auth-login
  3. StripPrefix=1 removes "/api"
  4. Adds X-Gateway-Authenticated: true
  
User Service:
  1. Receives request at /auth/login
  2. Validates X-Gateway-Authenticated header
  3. Processes login request
  4. Returns response
```

### Example 2: Get User Profile (Protected)
```
Client Request:
  GET /api/users/profile
  Authorization: Bearer <jwt_token>
  
API Gateway:
  1. Receives request
  2. Matches route: user-service-protected
  3. JwtAuthenticationFilter validates JWT
  4. Extracts: userId=123, email=user@example.com, role=STUDENT
  5. StripPrefix=1 removes "/api"
  6. Adds headers:
     - X-User-Id: 123
     - X-User-Email: user@example.com
     - X-User-Role: STUDENT
     - X-Gateway-Authenticated: true
  
User Service:
  1. Receives request at /users/profile
  2. Validates X-Gateway-Authenticated header
  3. Reads user info from headers
  4. No need to re-validate JWT
  5. Returns user profile
```

### Example 3: Direct Access Attempt (BLOCKED)
```
Client Request:
  GET http://localhost:8081/users/profile
  
Result: CONNECTION REFUSED
Reason: Port 8081 not exposed to host (only exposed in Docker network)

OR if somehow accessing from within Docker network:

Client Request from Docker:
  GET http://user-service:8081/users/profile
  (No X-Gateway-Authenticated header)
  
User Service:
  1. Validates X-Gateway-Authenticated header
  2. Header missing or not "true"
  3. Returns HTTP 403 Forbidden
  {
    "error": "Access Denied",
    "message": "Requests must come through API Gateway"
  }
```

## Security Features

### 1. Network Isolation
- **Docker Compose Configuration:**
  - API Gateway: `ports: ["8080:8080"]` - Exposed to host
  - User Service: `expose: ["8081"]` - Internal only
  - Training Service: `expose: ["8082"]` - Internal only
  
- **Result:**
  - External clients can ONLY access port 8080 (API Gateway)
  - Microservice ports are not accessible from host
  - Services only accessible within Docker network

### 2. Gateway Authentication Headers
- **X-Gateway-Authenticated:** Marks request as coming from gateway
- **X-Request-Source:** Identifies source as API-GATEWAY
- **X-Forwarded-By:** Specifies forwarding service

### 3. JWT Token Validation
- Centralized at API Gateway
- Invalid tokens rejected before reaching services
- User information extracted and forwarded via headers
- Services don't need to re-validate tokens

### 4. Header-Based Authorization
- User details passed via headers to services
- Services trust headers from API Gateway
- Can extract user context without JWT parsing

### 5. Path Transformation
- `/api` prefix stripped at gateway
- Services receive clean paths
- Prevents direct path-based access

## Configuration

### API Gateway (application.yml)
```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - RequestHeaderEnhancerFilter
      routes:
        - id: user-service-protected
          uri: lb://USER-SERVICE
          predicates:
            - Path=/api/users/**
          filters:
            - JwtAuthenticationFilter
            - StripPrefix=1

eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
```

### User Service (application.yml)
```yaml
gateway:
  require-gateway-auth: true
  allowed-hosts:
    - api-gateway
    - localhost

eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
  instance:
    hostname: user-service
```

### Docker Compose
```yaml
api-gateway:
  ports:
    - "8080:8080"  # PUBLIC

user-service:
  expose:
    - "8081"       # INTERNAL ONLY
  environment:
    GATEWAY_REQUIRE_GATEWAY_AUTH: "true"
```

## Eureka Load Balancer Integration

### Service Discovery
- All services register with Eureka Server
- API Gateway discovers services from Eureka
- Routes use `lb://SERVICE-NAME` for load balancing

### Load Balancing
- When multiple instances of a service exist, Spring Cloud Load Balancer distributes requests
- Uses Ribbon behind the scenes
- Round-robin by default

### Configuration
```yaml
eureka:
  client:
    fetch-registry: true
    register-with-eureka: true
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
  instance:
    lease-renewal-interval-in-seconds: 10
    lease-expiration-duration-in-seconds: 30
```

## Rate Limiting

### Configured Resolvers
1. **ipAddressKeyResolver:** Limits requests per IP address
2. **userIdKeyResolver:** Limits requests per authenticated user
3. **routeKeyResolver:** Limits requests per route

### Example Usage
```yaml
routes:
  - id: reject-unknown-routes
    filters:
      - name: RequestRateLimiter
        args:
          redis-rate-limiter.replenishRate: 100
          redis-rate-limiter.burstCapacity: 200
          key-resolver: "#{@ipAddressKeyResolver}"
```

## Testing

### 1. Test Microservice Isolation
```bash
# This should FAIL (port not exposed)
curl http://localhost:8081/auth/login -X POST

# This should SUCCEED (through gateway)
curl http://localhost:8080/api/auth/login -X POST
```

### 2. Test Direct Access Rejection
```bash
# From within Docker network, test direct access
docker exec lms-user-service curl http://user-service:8081/users/profile

# Response: 403 Forbidden - Access Denied
```

### 3. Test Gateway Access Validation
```bash
# Request with proper gateway headers (simulated from inside)
curl http://user-service:8081/users/profile \
  -H "X-Gateway-Authenticated: true" \
  -H "X-Request-Source: API-GATEWAY"

# Response: 200 OK (if authenticated)
```

### 4. Test JWT Validation
```bash
# Without token
curl http://localhost:8080/api/users/profile

# Response: 401 Unauthorized - Authorization header is missing

# With invalid token
curl http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer invalid_token"

# Response: 401 Unauthorized - Invalid or expired JWT token
```

## Troubleshooting

### Issue: Service returns 403 Forbidden
**Cause:** Request missing X-Gateway-Authenticated header
**Solution:** Ensure RequestHeaderEnhancerFilter is applied or request comes through API Gateway

### Issue: Service not found in Eureka
**Cause:** Service not registered with Eureka
**Solution:** Check service logs for Eureka registration errors, verify EUREKA_SERVER_URL environment variable

### Issue: Requests timing out
**Cause:** Service not responding or health check failing
**Solution:** Check service logs, verify database/redis connectivity, check health endpoints

### Issue: JWT validation failing
**Cause:** Token expired or invalid signature
**Solution:** Generate new token from login endpoint, verify JWT_SECRET matches across services

## Environment Variables

```bash
# API Gateway
EUREKA_SERVER_URL=http://eureka-server:8761/eureka/
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# User Service
GATEWAY_REQUIRE_GATEWAY_AUTH=true
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# Training Service
GATEWAY_REQUIRE_GATEWAY_AUTH=true
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
```

## Best Practices

1. **Always use `/api` prefix** in client requests
2. **Use JWT for authentication** on protected routes
3. **Monitor gateway logs** for direct access attempts
4. **Regularly check Eureka dashboard** at `http://localhost:8761`
5. **Keep JWT_SECRET consistent** across all services
6. **Update microservice ports** if changing them
7. **Test with gateway validation** enabled in production

## Security Checklist

- [x] API Gateway is the only public endpoint
- [x] Microservice ports are internal only
- [x] JWT validation at gateway level
- [x] Gateway authentication headers on all requests
- [x] Direct access validation at service level
- [x] Path transformation to prevent direct access
- [x] Rate limiting enabled
- [x] Health checks configured
- [x] Proper error responses
- [x] Logging for monitoring

## API Endpoints Summary

### Authentication (Public)
```
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/refresh
```

### User Management (Protected)
```
GET    /api/users/profile           (requires JWT)
PUT    /api/users/profile           (requires JWT)
GET    /api/users/{id}              (requires JWT)
```

### Training (Public)
```
GET    /api/training/search
GET    /api/training/published
GET    /api/training/public/**
```

### Training (Protected)
```
GET    /api/training/courses/**           (requires JWT)
GET    /api/training/modules/**           (requires JWT)
POST   /api/training/assignments/**       (requires JWT)
GET    /api/training/completion/**        (requires JWT)
POST   /api/training/enrollments/**       (requires JWT)
```

