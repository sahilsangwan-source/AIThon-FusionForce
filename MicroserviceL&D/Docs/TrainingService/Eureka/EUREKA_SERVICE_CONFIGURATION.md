# Eureka Service Discovery Server

## Overview

**Eureka** is the Service Discovery server for the Learning & Development Portal microservices architecture. It allows all microservices (User Service, Training Service, etc.) to register themselves dynamically and discover each other without hardcoding service URLs.

### Key Benefits

- ✅ **Dynamic Service Registration** - Services register themselves at startup
- ✅ **Service Discovery** - Services can find each other using service names
- ✅ **Health Monitoring** - Continuous health checks via heartbeats
- ✅ **Load Balancing** - Eureka-aware clients can load balance requests
- ✅ **Fault Tolerance** - Handles network failures gracefully
- ✅ **Real-time Updates** - Services get updated service lists immediately

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    EUREKA SERVER (Port 8761)                    │
│                  Service Registry & Discovery                   │
└─────────────────────────────────────────────────────────────────┘
                              ▲
                ┌─────────────┼─────────────┐
                │             │             │
                ▼             ▼             ▼
        ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
        │ User Service │ │Training Svc  │ │ Assignment.. │
        │ (Port 8081)  │ │ (Port 8082)  │ │ (Port 8083)  │
        └──────────────┘ └──────────────┘ └──────────────┘
             │                 │                 │
             └─────────────────┼─────────────────┘
                      Register & Heartbeat
```

---

## Service Registration Flow

```
1. User Service Starts
   ↓
2. Reads eureka.client.serviceUrl.defaultZone
   ↓
3. Sends registration request to Eureka Server with:
   - Service Name: user-service
   - Instance ID: user-service:8081
   - IP Address: localhost
   - Port: 8081
   - Health Check URL: /actuator/health
   ↓
4. Eureka Server adds to registry
   ↓
5. User Service sends heartbeat every 10 seconds
   ↓
6. Other services can now discover User Service
```

---

## Configuration Details

### Eureka Server Configuration

**File**: `/eureka-server/src/main/resources/application.yml`

```yaml
eureka:
  instance:
    hostname: localhost # Eureka server hostname

  client:
    register-with-eureka: false # Don't register itself
    fetch-registry: false # Don't fetch registry
    service-url:
      defaultZone: http://localhost:8761/eureka/

  server:
    enable-self-preservation: false # Development mode
    eviction-interval-timer-in-ms: 5000 # Check every 5 seconds
```

### Microservice Client Configuration

**File**: `/user-service/src/main/resources/application.yml`

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true # Register this service
    fetch-registry: true # Get list of other services

  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 10 # Send heartbeat every 10 sec
    lease-expiration-duration-in-seconds: 30 # Remove if no heartbeat for 30 sec
    instance-id: ${spring.application.name}:${server.port}
```

---

## Running Eureka Server

### Option 1: Docker Compose (Recommended)

```bash
cd /Users/sahil_sangwan/Desktop/plans

# Start only Eureka server
docker-compose up -d eureka-server

# Check if running
docker-compose ps eureka-server

# View logs
docker-compose logs -f eureka-server
```

### Option 2: Local Maven

```bash
cd eureka-server

# Build
mvn clean package -DskipTests

# Run
mvn spring-boot:run
```

### Option 3: Docker

```bash
cd eureka-server

# Build image
docker build -t lms-eureka-server:1.0.0 .

# Run container
docker run -p 8761:8761 \
  --name lms-eureka \
  lms-eureka-server:1.0.0
```

---

## Accessing Eureka Dashboard

### URL

```
http://localhost:8761
```

### Dashboard Features

- 📊 **Instances Currently Registered** - See all registered services
- ❤️ **General Info** - Eureka server details
- 🔍 **Instance Info** - Details of each registered service
- 📈 **System Stats** - Memory, threads, requests

---

## Service Registration & Discovery Examples

### Example 1: User Service Registration

When User Service starts:

```bash
# It sends registration request like:
POST http://eureka-server:8761/eureka/apps/USER-SERVICE
{
  "instance": {
    "instanceId": "user-service:8081",
    "app": "USER-SERVICE",
    "ipAddr": "172.17.0.3",
    "port": {"$": 8081, "@enabled": "true"},
    "status": "UP",
    "healthCheckUrl": "http://172.17.0.3:8081/actuator/health",
    "statusPageUrl": "http://172.17.0.3:8081/actuator/info",
    "leaseInfo": {
      "renewalIntervalInSecs": 10,
      "durationInSecs": 30
    }
  }
}
```

Response: Service is now registered ✅

### Example 2: Other Services Discovering User Service

When Training Service wants to call User Service:

```bash
# Training Service queries Eureka for User Service instances
GET http://eureka-server:8761/eureka/apps/USER-SERVICE

# Eureka returns:
{
  "application": {
    "name": "USER-SERVICE",
    "instance": [
      {
        "instanceId": "user-service:8081",
        "app": "USER-SERVICE",
        "ipAddr": "172.17.0.3",
        "port": {"$": 8081},
        "status": "UP"
      }
    ]
  }
}

# Training Service can now call:
http://172.17.0.3:8081/api/users/...
```

---

## Heartbeat Mechanism

### How It Works

```
User Service
├─ Every 10 seconds (lease-renewal-interval-in-seconds)
└─ Sends heartbeat to Eureka:

   POST http://eureka-server:8761/eureka/apps/USER-SERVICE/user-service:8081

   Eureka receives heartbeat
   ├─ Updates "last heartbeat" timestamp
   ├─ Marks service as healthy
   └─ If no heartbeat for 30 seconds → Mark as DOWN

Continuous Monitoring:
└─ Eureka checks every 5 seconds (eviction-interval-timer-in-ms)
   ├─ If service UP for 30+ seconds without heartbeat → Remove from registry
   └─ Other services stop using that instance
```

### Monitoring in Dashboard

Visit `http://localhost:8761` to see:

- ✅ UP - Service is healthy and sending heartbeats
- ⚠️ DOWN - Service is not responding
- ❌ OUT_OF_SERVICE - Deliberately taken offline

---

## Self-Preservation Mode

### What Is It?

If Eureka detects that many services stop sending heartbeats simultaneously (potential network issue), it enters **self-preservation mode** to prevent cascading failures.

### Development vs Production

#### Development (Current)

```yaml
enable-self-preservation: false
eviction-interval-timer-in-ms: 5000
```

- ✅ Fast detection of dead services
- ❌ May be too aggressive for production

#### Production (Recommended)

```yaml
enable-self-preservation: true
eviction-interval-timer-in-ms: 60000
```

- ✅ Resilient to network hiccups
- ✅ Prevents service removal during outages
- ❌ Takes longer to detect truly dead services

### Switching to Production

```bash
# Set environment variables before starting Eureka
export EUREKA_SERVER_ENABLE_SELF_PRESERVATION=true
export EUREKA_SERVER_EVICTION_INTERVAL=60000

# Then start services
docker-compose up -d eureka-server
```

---

## Integration with User Service

### Current Configuration ✅

**User Service** automatically registers with **Eureka Server**:

```yaml
# application.yml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true

  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 10
    instance-id: user-service:8081
```

### What This Means

1. **Startup**: User Service registers itself with Eureka
2. **Heartbeat**: Every 10 seconds, sends "I'm alive" message
3. **Discovery**: Other services can find User Service via Eureka
4. **Failover**: If User Service crashes, Eureka removes it from registry

### Verification

```bash
# 1. Start Eureka
docker-compose up -d eureka-server

# 2. Wait 30 seconds for it to fully start
sleep 30

# 3. Start User Service
docker-compose up -d user-service

# 4. Check Eureka dashboard
curl http://localhost:8761/eureka/apps/

# Should see USER-SERVICE registered:
# {
#   "applications": {
#     "application": [
#       {
#         "name": "USER-SERVICE",
#         "instance": [...]
#       }
#     ]
#   }
# }
```

---

## Docker Compose Integration

### Services in docker-compose.yml

```yaml
eureka-server:
  build:
    context: ./eureka-server
    dockerfile: Dockerfile
  container_name: lms-eureka
  ports:
    - "8761:8761"
  networks:
    - lms-network
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
    interval: 10s
    timeout: 5s
    retries: 5

user-service:
  depends_on:
    eureka-server:
      condition: service_healthy # Wait for Eureka to be healthy
  environment:
    EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
```

### Key Points

- 🔗 **Service Name**: `eureka-server` (DNS resolves to container IP)
- 🚀 **Startup Order**: Eureka starts first, then other services
- ❤️ **Health Check**: Eureka must be healthy before User Service starts
- 📡 **Network**: All services on same Docker network (`lms-network`)

---

## Monitoring & Troubleshooting

### Check Service Status

```bash
# Get all registered applications
curl http://localhost:8761/eureka/apps/

# Get specific service (USER-SERVICE)
curl http://localhost:8761/eureka/apps/USER-SERVICE

# Get specific instance
curl http://localhost:8761/eureka/apps/USER-SERVICE/user-service:8081
```

### View Logs

```bash
# Eureka logs
docker-compose logs -f eureka-server

# User Service logs (should show Eureka registration)
docker-compose logs -f user-service | grep -i eureka
```

### Expected Log Messages

```
# User Service startup
Fetching all instance registries from the eureka server...
DiscoveryClient_USER-SERVICE/user-service:8081: sending initial request to ...
DiscoveryClient_USER-SERVICE/user-service:8081: registration status: 204

# Heartbeat every 10 seconds
DiscoveryClient_USER-SERVICE/user-service:8081: sending heartbeat
```

### Service Won't Register?

1. **Check Eureka is running**:

   ```bash
   docker-compose ps eureka-server
   # Should show "Up" and "healthy"
   ```

2. **Check network connectivity**:

   ```bash
   docker exec lms-user-service curl http://eureka-server:8761/actuator/health
   # Should return: {"status":"UP"}
   ```

3. **Check User Service logs**:

   ```bash
   docker-compose logs user-service | grep -i "eureka\|registration"
   ```

4. **Verify configuration**:
   ```bash
   docker exec lms-user-service cat application.yml | grep -A 5 eureka
   ```

---

## For Next Microservices

### Training Service Configuration

```yaml
# training-service/src/main/resources/application.yml
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE:http://localhost:8761/eureka/}
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${server.port}
```

### Docker Compose Update

```yaml
training-service:
  depends_on:
    eureka-server:
      condition: service_healthy
  environment:
    EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
```

---

## REST API Endpoints

### Eureka Server Endpoints

| Method | Endpoint                                | Purpose                         |
| ------ | --------------------------------------- | ------------------------------- |
| GET    | `/eureka/apps`                          | Get all registered applications |
| GET    | `/eureka/apps/{app-name}`               | Get specific application        |
| GET    | `/eureka/apps/{app-name}/{instance-id}` | Get specific instance           |
| POST   | `/eureka/apps/{app-name}`               | Register new application        |
| PUT    | `/eureka/apps/{app-name}/{instance-id}` | Send heartbeat                  |
| DELETE | `/eureka/apps/{app-name}/{instance-id}` | Deregister instance             |
| GET    | `/actuator/health`                      | Health check                    |
| GET    | `/actuator/info`                        | Server info                     |

---

## Production Deployment Checklist

- [ ] Enable self-preservation mode
- [ ] Set eviction-interval-timer to 60000 (1 minute)
- [ ] Use DNS hostname instead of localhost
- [ ] Configure multiple Eureka instances (peer replication)
- [ ] Enable SSL/TLS for Eureka
- [ ] Set up monitoring and alerting
- [ ] Configure backup and disaster recovery
- [ ] Test failover scenarios
- [ ] Document service discovery endpoints
- [ ] Train team on Eureka concepts

---

## Quick Reference

### Start Eureka + User Service

```bash
cd /Users/sahil_sangwan/Desktop/plans

# Start all services
docker-compose up -d eureka-server user-service

# Check status
docker-compose ps

# View Eureka dashboard
open http://localhost:8761

# Check registration
curl http://localhost:8761/eureka/apps/USER-SERVICE
```

### Verify Registration

```bash
# Should output JSON with USER-SERVICE instance
curl -s http://localhost:8761/eureka/apps/ | jq '.applications.application[] | {name: .name, instances: (.instance | length)}'

# Output:
# {
#   "name": "USER-SERVICE",
#   "instances": 1
# }
```

---

## Summary

✅ **Eureka Server** - Fully configured and ready
✅ **User Service** - Configured to register with Eureka
✅ **Service Discovery** - User Service discoverable by other services
✅ **Health Monitoring** - Heartbeat mechanism active
✅ **Dashboard** - Available at http://localhost:8761

All microservices can now:

- Discover each other dynamically
- Call each other using service names
- Handle service failovers automatically
- Scale without configuration changes

**Ready for all 8 microservices!** 🚀
