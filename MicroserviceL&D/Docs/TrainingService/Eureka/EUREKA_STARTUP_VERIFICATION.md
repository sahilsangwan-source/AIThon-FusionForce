# Eureka Service - Startup & Verification Guide

## ✅ Pre-Startup Checklist

Before starting the services, verify all files are in place:

```bash
# Check Eureka server files
ls -la eureka-server/src/main/resources/application.yml
ls -la eureka-server/src/main/java/com/lms/eureka/EurekaServerApplication.java
ls -la eureka-server/pom.xml

# Check User Service Eureka configuration
grep -A 10 "eureka:" user-service/src/main/resources/application.yml

# Check Docker Compose
grep -A 10 "eureka-server:" docker-compose.yml
```

---

## 🚀 Step 1: Build Docker Images

```bash
# Navigate to workspace
cd /Users/sahil_sangwan/Desktop/plans

# Build Eureka Server image
docker build -t lms-eureka-server:1.0.0 ./eureka-server

# Verify image was created
docker images | grep lms-eureka
```

**Expected Output:**

```
REPOSITORY                TAG      IMAGE ID       CREATED        SIZE
lms-eureka-server         1.0.0    abc123def456   2 minutes ago   500MB
```

---

## 🎯 Step 2: Start Services

### Option A: Start All Services (Recommended)

```bash
# Start entire stack
docker-compose up -d

# Wait for services to become healthy
echo "Waiting for services to initialize..."
sleep 30

# Check status
docker-compose ps
```

### Option B: Start Eureka First, Then User Service

```bash
# Start only Eureka
docker-compose up -d eureka-server

# Wait for Eureka to be healthy
echo "Waiting for Eureka to be healthy..."
sleep 20

# Verify Eureka is running
docker-compose ps eureka-server
# Status should be "Up (healthy)"

# Start User Service
docker-compose up -d user-service

# Wait for registration
sleep 10

# Check status
docker-compose ps
```

### Option C: Start Services in Foreground (Debugging)

```bash
# Terminal 1: Start Eureka
docker-compose up eureka-server

# Terminal 2: Start User Service (after Eureka is healthy)
docker-compose up user-service

# Terminal 3: Run tests
bash test-user-service.sh
```

---

## 📊 Step 3: Verify Eureka Server is Running

### 3.1 Check Container Status

```bash
# List containers
docker-compose ps eureka-server

# Expected output:
# NAME               STATUS              PORTS
# lms-eureka         Up 30 seconds       8761/tcp
```

### 3.2 Check Eureka Dashboard

```bash
# Open in browser
open http://localhost:8761

# Or check via curl
curl -s http://localhost:8761 | head -20
```

**Expected Output:**

- Page title: "Eureka"
- "Instances currently registered with Eureka"
- System stats showing memory and request count

### 3.3 Check Eureka Health

```bash
# Check health endpoint
curl -s http://localhost:8761/actuator/health | jq '.'

# Expected output:
# {
#   "status": "UP",
#   "components": {
#     ...
#   }
# }
```

### 3.4 View Eureka Logs

```bash
# Show last 50 lines of logs
docker-compose logs eureka-server

# Follow logs in real-time
docker-compose logs -f eureka-server

# Expected key messages:
# - "Started EurekaServerApplication"
# - "Eureka Server started successfully"
# - "Registered with Eureka"
```

---

## 👤 Step 4: Verify User Service Registration

### 4.1 Check User Service Container

```bash
# List User Service container
docker-compose ps user-service

# Expected output:
# NAME               STATUS              PORTS
# lms-user-service   Up 20 seconds       8081/tcp
```

### 4.2 Check User Service Logs for Eureka Registration

```bash
# Show logs
docker-compose logs user-service

# Look for these messages:
# - "Fetching all instance registries from the eureka server"
# - "registration status: 204"
# - "DiscoveryClient_USER-SERVICE"
```

### 4.3 Query Eureka Registry for User Service

```bash
# Get all registered applications
curl -s http://localhost:8761/eureka/apps/ | jq '.applications.application[] | {name: .name, instances: (.instance | length)}'

# Expected output:
# {
#   "name": "USER-SERVICE",
#   "instances": 1
# }
```

### 4.4 Get Specific User Service Instance Details

```bash
# Get User Service details
curl -s http://localhost:8761/eureka/apps/USER-SERVICE | jq '.'

# Expected output includes:
# - name: "USER-SERVICE"
# - instanceId: "user-service:8081"
# - app: "USER-SERVICE"
# - ipAddr: "172.17.0.X" (Docker IP)
# - port: 8081
# - status: "UP"
```

### 4.5 Check Instance Health

```bash
# Get health info from Eureka
curl -s http://localhost:8761/eureka/apps/USER-SERVICE/user-service:8081 | jq '.instance.status'

# Expected output:
# "UP"
```

---

## 🧪 Step 5: Test Service Discovery

### 5.1 Access User Service from Docker Network

```bash
# Test from within Docker network
docker exec lms-user-service curl http://localhost:8081/actuator/health

# Expected output:
# {"status":"UP","components":{...}}
```

### 5.2 Access User Service via Eureka Discovery

```bash
# Get User Service instance info
INSTANCE_IP=$(curl -s http://localhost:8761/eureka/apps/USER-SERVICE | jq -r '.application.instance[0].ipAddr')
INSTANCE_PORT=$(curl -s http://localhost:8761/eureka/apps/USER-SERVICE | jq -r '.application.instance[0].port."$"')

echo "User Service is at: $INSTANCE_IP:$INSTANCE_PORT"

# Test health endpoint
curl -s http://$INSTANCE_IP:$INSTANCE_PORT/actuator/health | jq '.'
```

### 5.3 Test User Service API Endpoints

```bash
# Register a new user
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "eureka-test@example.com",
    "password": "TestPass123!",
    "firstName": "Eureka",
    "lastName": "Test",
    "employeeId": "EMP-EUREKA-001",
    "department": "IT"
  }'

# Expected: 201 Created with user details

# Login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "eureka-test@example.com",
    "password": "TestPass123!"
  }'

# Expected: 200 OK with access token and refresh token
```

---

## 📈 Step 6: Monitor Service Discovery

### 6.1 Real-Time Service Status

```bash
# Watch services in real-time
watch -n 5 'docker-compose ps'

# Press Ctrl+C to stop
```

### 6.2 Eureka Dashboard Refresh

```bash
# The dashboard at http://localhost:8761 shows:
# - Instances currently registered
# - Instance status (UP/DOWN)
# - Last heartbeat timestamp
# - Available zones

# Refresh browser to see live updates
```

### 6.3 Monitor Heartbeats

```bash
# Watch User Service sending heartbeats
docker-compose logs -f user-service | grep -i "heartbeat\|registration\|eureka"

# Expected: Heartbeat sent every 10 seconds
# - "DiscoveryClient: sending heartbeat"
# - "Registering with Eureka server"
```

---

## 🔍 Troubleshooting

### Issue: User Service Not Registering with Eureka

**Problem**: User Service exists but doesn't appear in Eureka registry

**Solution**:

```bash
# 1. Check User Service logs for errors
docker-compose logs user-service | grep -i "eureka\|error\|exception"

# 2. Verify Eureka server is healthy
docker-compose ps eureka-server
# Should show "Up (healthy)"

# 3. Check network connectivity
docker exec lms-user-service curl -v http://eureka-server:8761/actuator/health
# Should get 200 OK

# 4. Verify configuration
docker exec lms-user-service cat application.yml | grep -A 5 eureka:

# 5. Check Eureka server logs
docker-compose logs eureka-server | grep -i "registration\|received"

# 6. Restart services
docker-compose down
docker-compose up -d eureka-server
sleep 20
docker-compose up -d user-service
```

### Issue: Eureka Dashboard Not Accessible

**Problem**: Can't access http://localhost:8761

**Solution**:

```bash
# 1. Check if container is running
docker-compose ps eureka-server

# 2. Check port binding
docker-compose port eureka-server 8761
# Should show "0.0.0.0:8761"

# 3. Check if port is in use
lsof -i :8761
# Kill if conflicting: kill -9 <PID>

# 4. Restart Eureka
docker-compose restart eureka-server

# 5. Test curl directly
curl -v http://localhost:8761
```

### Issue: User Service Crashes on Startup

**Problem**: User Service starts but immediately crashes

**Solution**:

```bash
# 1. View full logs
docker-compose logs user-service

# 2. Check PostgreSQL is running
docker-compose ps postgres

# 3. Check Redis is running
docker-compose ps redis

# 4. Check Eureka is healthy
docker-compose ps eureka-server

# 5. Check database connection
docker exec lms-user-service curl -v http://postgres:5432/
# Should timeout (port not open) but shows network connectivity

# 6. Check environment variables
docker-compose config | grep -A 20 "user-service:"
```

### Issue: Services Can't Find Each Other

**Problem**: Service discovery works in Eureka but services can't communicate

**Solution**:

```bash
# 1. Verify both services registered
curl -s http://localhost:8761/eureka/apps/ | jq '.applications.application[].name'

# 2. Test network connectivity between containers
docker exec lms-user-service ping eureka-server
# Should get responses

# 3. Check service names
curl -s http://localhost:8761/eureka/apps/ | jq '.applications.application[] | {name: .name, app: .instance[0].app}'

# 4. Verify port mappings
docker-compose config | grep -B 5 -A 5 "ports:"
```

---

## 📝 Health Check Details

### Eureka Health Check

```bash
# Full health check
curl -s http://localhost:8761/actuator/health/livenessState | jq '.'
curl -s http://localhost:8761/actuator/health/readinessState | jq '.'

# Expected: "UP"
```

### User Service Health Check

```bash
# Full health check
curl -s http://localhost:8081/actuator/health | jq '.'

# Components should show:
# - db: UP
# - redis: UP
# - kafka: UP
# - discoveryClient: UP
```

---

## 📊 Performance Monitoring

### Monitor Resource Usage

```bash
# CPU and Memory usage
docker stats lms-eureka lms-user-service

# Network I/O
docker stats --no-stream --format "table {{.Container}}\t{{.NetIO}}"
```

### Eureka Metrics

```bash
# Get server metrics
curl -s http://localhost:8761/actuator/metrics | jq '.names | sort'

# Get specific metric (e.g., JVM memory)
curl -s http://localhost:8761/actuator/metrics/jvm.memory.used | jq '.'
```

---

## 🧹 Cleanup Commands

### Stop All Services

```bash
docker-compose down
```

### Stop Specific Service

```bash
docker-compose stop user-service
```

### Remove Volumes (Clean Database)

```bash
docker-compose down -v
```

### View Logs After Shutdown

```bash
# Logs are preserved even after shutdown
docker-compose logs eureka-server
```

---

## ✅ Success Indicators

You know everything is working when:

- ✅ `docker-compose ps` shows all services "Up"
- ✅ Eureka dashboard at `http://localhost:8761` displays USER-SERVICE
- ✅ `curl http://localhost:8761/eureka/apps/USER-SERVICE` returns instance details
- ✅ User Service logs show "Registering with Eureka"
- ✅ Can authenticate and call User Service APIs
- ✅ User Service responds to `/api/auth/login` and `/api/users/register`

---

## 📋 Quick Verification Script

```bash
#!/bin/bash

echo "=== Eureka Service Verification ==="

# Check containers running
echo "1. Container Status:"
docker-compose ps eureka-server user-service

# Check Eureka health
echo -e "\n2. Eureka Health:"
curl -s http://localhost:8761/actuator/health | jq '.status'

# Check registered services
echo -e "\n3. Registered Services:"
curl -s http://localhost:8761/eureka/apps/ | jq '.applications.application[] | {name: .name, instances: (.instance | length)}'

# Check User Service status in Eureka
echo -e "\n4. User Service Status:"
curl -s http://localhost:8761/eureka/apps/USER-SERVICE | jq '.application.instance[0] | {status: .status, ipAddr: .ipAddr, port: .port}'

# Test User Service health
echo -e "\n5. User Service Health:"
curl -s http://localhost:8081/actuator/health | jq '.status'

echo -e "\n=== All Checks Complete ==="
```

Save as `verify-eureka.sh` and run:

```bash
chmod +x verify-eureka.sh
./verify-eureka.sh
```

---

## 🎓 Next Steps

1. ✅ Verify Eureka server and User Service are working
2. 📚 Read `USER_SERVICE_TESTING.md` to test all endpoints
3. 🏗️ Create Training Service (Port 8082) following User Service pattern
4. 📡 Create API Gateway (Port 8080) to route requests through Eureka
5. 📊 Set up monitoring and alerting for services

---

## 📞 Summary

**Eureka Service** is now:

- ✅ Fully configured and running on port 8761
- ✅ Accepting service registrations
- ✅ Providing service discovery
- ✅ Monitoring service health via heartbeats
- ✅ Ready for additional microservices

**User Service** is now:

- ✅ Registered with Eureka
- ✅ Discoverable by other services
- ✅ Sending heartbeats every 10 seconds
- ✅ Ready to communicate with other microservices

All microservices can now discover and communicate with each other dynamically! 🚀
