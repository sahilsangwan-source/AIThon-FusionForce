# Gateway Filter Quick Reference Guide

## 🎯 Quick Summary

✅ **What's Implemented:**
- API Gateway is the ONLY public endpoint (port 8080)
- Microservices are INTERNAL ONLY (ports 8081, 8082 not exposed)
- All requests transformed: `/api/user/*` → `/*` at gateway
- JWT validated at gateway level
- User info passed via headers to microservices
- Direct access attempts are REJECTED with 403
- Eureka load balancer configured for service discovery
- Rate limiting implemented

## 📋 Key Components

### 1. API Gateway Filters
```
RequestHeaderEnhancerFilter
├─ X-Gateway-Authenticated: true
├─ X-Request-Source: API-GATEWAY
└─ X-Forwarded-By: api-gateway

JwtAuthenticationFilter (on protected routes)
├─ Validates JWT token
├─ Extracts: userId, email, role
└─ Adds headers for downstream services

RequestTransformationFilter
└─ Available for custom transformations
```

### 2. Microservice Validation
```
GatewayAccessValidationFilter
├─ Checks X-Gateway-Authenticated header
├─ Rejects if not from gateway (403)
└─ Skips for health/actuator endpoints
```

## 🛣️ Route Examples

### User Service
```
GET  /api/auth/login              → NO AUTH
POST /api/auth/register           → NO AUTH
POST /api/auth/refresh            → NO AUTH
GET  /api/users/profile           → REQUIRES JWT
PUT  /api/users/{id}              → REQUIRES JWT
```

### Training Service
```
GET  /api/training/search         → NO AUTH
GET  /api/training/published      → NO AUTH
GET  /api/training/courses/**     → REQUIRES JWT
POST /api/training/enrollments/** → REQUIRES JWT
```

## 🔄 Request Flow

```
1. Client sends request
   GET /api/users/profile
   Authorization: Bearer <token>

2. API Gateway
   - Receives on port 8080
   - Validates JWT (JwtAuthenticationFilter)
   - Extracts user info
   - Adds headers
   - Strips /api prefix
   - Routes to USER-SERVICE via Eureka LB

3. User Service
   - Receives at /users/profile
   - Validates X-Gateway-Authenticated header
   - Reads user info from headers
   - Returns response

4. Client receives response
```

## 🔐 Security Features

| Feature | Location | Purpose |
|---------|----------|---------|
| Network Isolation | Docker Compose | Only API Gateway exposed |
| JWT Validation | API Gateway | Authenticate users |
| Gateway Headers | JwtAuthenticationFilter | Mark gateway requests |
| Direct Access Validation | GatewayAccessValidationFilter | Reject non-gateway requests |
| Path Transformation | StripPrefix filter | Remove /api prefix |
| Rate Limiting | RateLimiterConfig | Prevent abuse |
| Eureka Integration | LoadBalancerConfig | Service discovery & LB |

## 📝 Configuration Files

### Docker Compose
```yaml
# EXPOSED to clients
api-gateway:
  ports: ["8080:8080"]

# INTERNAL ONLY
user-service:
  expose: ["8081"]

training-service:
  expose: ["8082"]
```

### API Gateway (application.yml)
```yaml
spring.cloud.gateway:
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
```

### Microservices (application.yml)
```yaml
gateway:
  require-gateway-auth: true

eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
```

## 🧪 Testing Commands

### 1. Test Public Endpoint (No Auth)
```bash
curl http://localhost:8080/api/auth/login \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'
```

### 2. Test Protected Endpoint (With JWT)
```bash
curl http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer <your_jwt_token>"
```

### 3. Test Direct Access (Should Fail)
```bash
# Try to bypass gateway - FAILS because port not exposed
curl http://localhost:8081/users/profile
# Connection refused - port not exposed to host

# From inside Docker network
docker exec lms-user-service curl http://user-service:8081/users/profile
# Returns 403 Forbidden - Access Denied
```

### 4. View Eureka Dashboard
```
http://localhost:8761
```

## 🚀 Deployment Steps

### 1. Build All Services
```bash
cd api-gateway && mvn clean package
cd ../user-service && mvn clean package
cd ../training-service && mvn clean package
cd ..
```

### 2. Start Services
```bash
docker-compose up -d
```

### 3. Verify Startup
```bash
# Check Eureka
curl http://localhost:8761/actuator/health

# Check API Gateway
curl http://localhost:8080/actuator/health

# Check service registration
curl http://localhost:8761/eureka/apps
```

### 4. Test Gateway Access
```bash
# Login
curl http://localhost:8080/api/auth/login -X POST \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'

# Use returned JWT token
TOKEN=<your_jwt_token>

# Access protected resource
curl http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer $TOKEN"
```

## 📊 Network Architecture

```
External World
    ↓
[API Gateway - Port 8080] ← ONLY PUBLIC
    ↓
Docker Network (lms-network)
    ├─ [User Service - Port 8081 - INTERNAL]
    ├─ [Training Service - Port 8082 - INTERNAL]
    ├─ [Eureka Server - Port 8761]
    ├─ [PostgreSQL - Port 5432]
    ├─ [Redis - Port 6379]
    ├─ [Kafka - Port 9092]
    └─ [Other Services...]
```

## 🔑 Important Environment Variables

```bash
# Set before docker-compose up
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
EUREKA_SERVER_URL=http://eureka-server:8761/eureka/
```

## ⚠️ Common Issues & Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| 403 Access Denied | No gateway header | Use API Gateway (port 8080) |
| 401 Unauthorized | Invalid/missing JWT | Login first, get token |
| Service Not Found | Eureka not registered | Check service startup logs |
| Connection Refused | Trying direct port | Use gateway (port 8080) |
| 404 Not Found | Wrong path | Use `/api/...` format |

## 📂 File Structure

```
api-gateway/src/main/java/com/lms/gateway/
├── filter/
│   ├── JwtAuthenticationFilter.java         (validates JWT)
│   ├── RequestHeaderEnhancerFilter.java     (adds gateway headers)
│   └── RequestTransformationFilter.java     (future use)
├── config/
│   ├── LoadBalancerConfig.java              (Eureka integration)
│   └── RateLimiterConfig.java               (rate limiting)
└── util/
    └── JwtUtil.java                         (JWT operations)

user-service/src/main/java/com/lms/userservice/
└── config/
    └── GatewayAccessValidationFilter.java   (validates gateway access)

training-service/src/main/java/com/lms/trainingservice/
└── config/
    └── GatewayAccessValidationFilter.java   (validates gateway access)
```

## 🎓 Learning Path

1. **Understand Request Flow** → Read GATEWAY_FILTER_IMPLEMENTATION.md
2. **Check Filter Code** → Review JwtAuthenticationFilter.java
3. **Verify Microservices** → Look at GatewayAccessValidationFilter.java
4. **Test Setup** → Run docker-compose and test endpoints
5. **Monitor** → Check logs and Eureka dashboard

## 🔗 Related Documentation

- `GATEWAY_FILTER_IMPLEMENTATION.md` - Detailed implementation guide
- `docker-compose.yml` - Network and service configuration
- `api-gateway/src/main/resources/application.yml` - Gateway routes
- `user-service/src/main/resources/application.yml` - User service config
- `training-service/src/main/resources/application.yml` - Training service config

## ✅ Verification Checklist

- [ ] API Gateway runs on port 8080
- [ ] User Service runs on port 8081 (internal only)
- [ ] Training Service runs on port 8082 (internal only)
- [ ] Eureka shows all services registered
- [ ] Public endpoints work without JWT
- [ ] Protected endpoints require JWT
- [ ] Direct access attempts return 403
- [ ] Path transformation works (/api/user/* → /*)
- [ ] User info in headers for microservices
- [ ] Rate limiting works

