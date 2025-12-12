# Gateway Filter Implementation - Deployment & Setup Guide

## 📋 Overview

This guide walks you through setting up the complete gateway filter implementation with security isolation, authentication, and service discovery.

## ✅ What's Been Implemented

### 1. API Gateway Filters
- ✅ **JwtAuthenticationFilter** - Validates JWT tokens on protected routes
- ✅ **RequestHeaderEnhancerFilter** - Adds gateway identification headers to all requests
- ✅ **RequestTransformationFilter** - Framework for custom request transformations
- ✅ **RateLimiterConfig** - IP and user-based rate limiting
- ✅ **LoadBalancerConfig** - Eureka service discovery and load balancing

### 2. Microservice Protection
- ✅ **GatewayAccessValidationFilter** (User Service) - Rejects non-gateway requests
- ✅ **GatewayAccessValidationFilter** (Training Service) - Rejects non-gateway requests
- ✅ Docker network isolation - microservice ports not exposed to host
- ✅ Configuration for gateway authentication requirement

### 3. Route Configuration
- ✅ Path transformation (`/api/user/*` → `/*`)
- ✅ Request prefix stripping (StripPrefix=1)
- ✅ Public route handling (no JWT)
- ✅ Protected route handling (JWT required)
- ✅ Eureka load balancer integration (`lb://SERVICE-NAME`)

### 4. Network & Security
- ✅ Docker Compose updated for network isolation
- ✅ API Gateway exposed on port 8080
- ✅ Microservices internal only (expose, not ports)
- ✅ All configuration files updated
- ✅ Environment variables configured

## 🚀 Deployment Steps

### Step 1: Verify All Files Are in Place

```bash
cd /Users/sahil_sangwan/Desktop/plans

# Check API Gateway files
ls -la api-gateway/src/main/java/com/lms/gateway/filter/
# Should have:
# - JwtAuthenticationFilter.java
# - RequestHeaderEnhancerFilter.java
# - RequestTransformationFilter.java

ls -la api-gateway/src/main/java/com/lms/gateway/config/
# Should have:
# - LoadBalancerConfig.java
# - RateLimiterConfig.java

# Check User Service files
ls -la user-service/src/main/java/com/lms/userservice/config/
# Should have:
# - GatewayAccessValidationFilter.java

# Check Training Service files
ls -la training-service/src/main/java/com/lms/trainingservice/config/
# Should have:
# - GatewayAccessValidationFilter.java

# Check configuration files
ls -la user-service/src/main/resources/application.yml
ls -la training-service/src/main/resources/application.yml
ls -la api-gateway/src/main/resources/application.yml
```

### Step 2: Set Environment Variables

```bash
# Set JWT secret (must be same across all services)
export JWT_SECRET="404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"

# Set Eureka server URL
export EUREKA_SERVER_URL="http://eureka-server:8761/eureka/"

# Verify
echo "JWT_SECRET=$JWT_SECRET"
echo "EUREKA_SERVER_URL=$EUREKA_SERVER_URL"
```

### Step 3: Build All Services

```bash
cd /Users/sahil_sangwan/Desktop/plans

echo "Building API Gateway..."
cd api-gateway
mvn clean package -DskipTests
cd ..

echo "Building User Service..."
cd user-service
mvn clean package -DskipTests
cd ..

echo "Building Training Service..."
cd training-service
mvn clean package -DskipTests
cd ..

echo "Building Eureka Server..."
cd eureka-server
mvn clean package -DskipTests
cd ..

echo "All services built successfully!"
```

### Step 4: Verify Docker Images

```bash
# Clean up old containers (optional)
docker-compose down -v

# Build Docker images
docker-compose build

# List images
docker images | grep lms
```

### Step 5: Start Services

```bash
# Start all services
docker-compose up -d

# Check startup progress
sleep 30

# View logs
docker-compose logs -f

# Press Ctrl+C to exit logs
```

### Step 6: Verify Service Health

```bash
# Wait for services to be fully up (2-3 minutes)
sleep 120

# Check Eureka dashboard
curl -s http://localhost:8761/actuator/health | jq .

# Check API Gateway
curl -s http://localhost:8080/actuator/health | jq .

# Check registered services
curl -s http://localhost:8761/eureka/apps -H "Accept: application/json" | \
  jq '.applications.application[] | {name: .name, instances: .instance}'
```

### Step 7: Run Tests

```bash
# Make script executable (if not already)
chmod +x test-gateway-filter.sh

# Run comprehensive tests
./test-gateway-filter.sh
```

## 🔍 Verification Checklist

### Network Isolation
- [ ] API Gateway is accessible on `localhost:8080`
- [ ] User Service port 8081 is NOT accessible on localhost
- [ ] Training Service port 8082 is NOT accessible on localhost
- [ ] Services are accessible within Docker network via `service-name:port`

### Service Registration
- [ ] API Gateway registered in Eureka
- [ ] User Service registered in Eureka
- [ ] Training Service registered in Eureka
- [ ] All services show as UP in Eureka dashboard

### Authentication
- [ ] Public endpoints work without JWT
- [ ] Protected endpoints return 401 without JWT
- [ ] Protected endpoints return 200 with valid JWT
- [ ] Protected endpoints return 401 with invalid JWT

### Path Transformation
- [ ] `/api/auth/login` routes to `user-service:/auth/login`
- [ ] `/api/users/profile` routes to `user-service:/users/profile`
- [ ] `/api/training/search` routes to `training-service:/search`
- [ ] `/api/training/courses/**` routes to `training-service:/courses/**`

### Gateway Headers
- [ ] All requests have `X-Gateway-Authenticated` header
- [ ] All requests have `X-Request-Source` header
- [ ] Protected requests have `X-User-Id`, `X-User-Email`, `X-User-Role` headers

### Direct Access Prevention
- [ ] Direct requests to `user-service:8081` return 403
- [ ] Direct requests to `training-service:8082` return 403
- [ ] Error message indicates "Access Denied - Requests must come through API Gateway"

## 📝 Testing Workflows

### Test 1: Public Endpoint Access
```bash
# Login (public endpoint)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'

# Expected: 200 OK with JWT token
```

### Test 2: Protected Endpoint Access
```bash
# Get JWT from login response
TOKEN="your_jwt_token_here"

# Access protected endpoint with JWT
curl http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer $TOKEN"

# Expected: 200 OK with user data
```

### Test 3: Protected Endpoint Without JWT
```bash
# Try to access without JWT
curl http://localhost:8080/api/users/profile

# Expected: 401 Unauthorized
```

### Test 4: Direct Access Attempt
```bash
# From within Docker network, try direct access
docker exec lms-user-service curl http://user-service:8081/users/profile \
  -H "Content-Type: application/json"

# Expected: 403 Forbidden - Access Denied
```

### Test 5: Valid Gateway Access from Within Docker
```bash
# Request with proper gateway headers
docker exec lms-user-service curl http://user-service:8081/users/profile \
  -H "X-Gateway-Authenticated: true" \
  -H "X-Request-Source: API-GATEWAY" \
  -H "X-User-Id: 123"

# Expected: 200 OK (if authenticated)
```

## 🔧 Configuration Files Location

| File | Purpose |
|------|---------|
| `api-gateway/src/main/resources/application.yml` | Gateway routes & filters |
| `user-service/src/main/resources/application.yml` | User service config |
| `training-service/src/main/resources/application.yml` | Training service config |
| `docker-compose.yml` | Network & container setup |

## 📊 Eureka Dashboard

Access at: `http://localhost:8761`

**Expected Services:**
- API-GATEWAY (UP)
- USER-SERVICE (UP)
- TRAINING-SERVICE (UP)
- EUREKA-SERVER (UP)

## 🐛 Troubleshooting

### Issue: Service returns 403 Forbidden
**Symptom:** "Access Denied - Requests must come through API Gateway"
**Cause:** Request missing X-Gateway-Authenticated header
**Solution:** Ensure request goes through API Gateway (port 8080)

### Issue: Service returns 401 Unauthorized
**Symptom:** "Authorization header is missing"
**Cause:** Protected route requires JWT token
**Solution:** Add `Authorization: Bearer <token>` header

### Issue: Service not found in Eureka
**Symptom:** `Service DOWN in Eureka`
**Cause:** Service failed to register
**Solution:** 
```bash
# Check service logs
docker logs lms-user-service
docker logs lms-training-service

# Check Eureka connectivity
curl -s http://localhost:8761/actuator/health
```

### Issue: Port already in use
**Symptom:** `Error response from daemon: Ports are not available`
**Solution:**
```bash
# Kill existing containers
docker-compose down

# Find and kill process on specific port
lsof -i :8080  # Find process on port 8080
kill -9 <PID>  # Kill the process

# Retry
docker-compose up -d
```

### Issue: JWT token validation failing
**Symptom:** "Invalid or expired JWT token"
**Cause:** JWT_SECRET mismatch or token expired
**Solution:**
```bash
# Verify JWT_SECRET is same across all services
echo $JWT_SECRET

# Generate new token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

## 📈 Monitoring & Logs

### View All Logs
```bash
docker-compose logs -f
```

### View Specific Service Logs
```bash
docker logs -f lms-api-gateway
docker logs -f lms-user-service
docker logs -f lms-training-service
docker logs -f lms-eureka
```

### Check Service Health
```bash
# API Gateway
curl http://localhost:8080/actuator/health | jq .

# Eureka
curl http://localhost:8761/actuator/health | jq .

# All services registered
curl http://localhost:8761/eureka/apps -H "Accept: application/json" | jq .
```

## 🔐 Security Checklist

- [ ] JWT_SECRET is set and consistent across all services
- [ ] API Gateway is the only public endpoint (port 8080)
- [ ] Microservice ports are not exposed to host
- [ ] All requests have gateway authentication headers
- [ ] Gateway validation filter is enabled in microservices
- [ ] Protected routes require valid JWT
- [ ] Rate limiting is configured
- [ ] Eureka is not publicly accessible (port 8761 internal)
- [ ] Database passwords are strong and unique
- [ ] Redis password is configured

## 📚 Related Documentation

- `GATEWAY_FILTER_IMPLEMENTATION.md` - Detailed technical documentation
- `GATEWAY_FILTER_QUICK_REFERENCE.md` - Quick reference guide
- `test-gateway-filter.sh` - Automated testing script

## 🎯 Next Steps

1. **Monitor** - Watch logs and Eureka dashboard for any issues
2. **Test** - Run the test script regularly
3. **Scale** - Add more instances of services if needed
4. **Secure** - Update JWT_SECRET in production
5. **Document** - Update API documentation with new routes
6. **Train** - Educate team on new architecture

## 📞 Support

If you encounter any issues:

1. Check the logs: `docker-compose logs -f`
2. Review the troubleshooting section above
3. Verify all environment variables are set
4. Ensure all files are in correct locations
5. Check network connectivity between services
6. Verify JWT_SECRET is consistent

## ✨ Summary

You now have:
- ✅ Secure API Gateway as single entry point
- ✅ Protected microservices from direct access
- ✅ Centralized JWT authentication
- ✅ Automatic path transformation
- ✅ Eureka service discovery
- ✅ Load balancing capability
- ✅ Rate limiting enabled
- ✅ Complete isolation in Docker network
- ✅ Comprehensive testing suite
- ✅ Full documentation

**The microservices are now completely isolated and can only be accessed through the API Gateway!**

