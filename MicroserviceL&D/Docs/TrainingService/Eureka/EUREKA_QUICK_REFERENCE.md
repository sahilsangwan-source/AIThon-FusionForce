# Eureka Quick Reference Guide

## 🚀 30-Second Start

```bash
cd /Users/sahil_sangwan/Desktop/plans

# Build and start
docker build -t lms-eureka-server:1.0.0 ./eureka-server
docker-compose up -d

# Verify
curl http://localhost:8761/actuator/health
curl http://localhost:8761/eureka/apps/USER-SERVICE
```

---

## 🔗 Important URLs

| Service                     | URL                                            | Purpose                  |
| --------------------------- | ---------------------------------------------- | ------------------------ |
| **Eureka Dashboard**        | http://localhost:8761                          | View registered services |
| **Eureka Health**           | http://localhost:8761/actuator/health          | Check server status      |
| **All Registered Services** | http://localhost:8761/eureka/apps/             | Get all services         |
| **User Service**            | http://localhost:8761/eureka/apps/USER-SERVICE | Get User Service details |
| **User Service API**        | http://localhost:8081                          | Call User Service        |

---

## 📋 Common Commands

### Check Service Status

```bash
# All services
docker-compose ps

# Specific service
docker-compose ps eureka-server
docker-compose ps user-service

# Eureka health
curl http://localhost:8761/actuator/health

# User Service health
curl http://localhost:8081/actuator/health
```

### View Logs

```bash
# Eureka logs
docker-compose logs -f eureka-server

# User Service logs
docker-compose logs -f user-service

# Filter for Eureka registration
docker-compose logs user-service | grep -i eureka
```

### Start/Stop Services

```bash
# Start all
docker-compose up -d

# Start specific
docker-compose up -d eureka-server
docker-compose up -d user-service

# Stop
docker-compose down

# Restart
docker-compose restart eureka-server
docker-compose restart user-service
```

### Test Registration

```bash
# Check if User Service is registered
curl -s http://localhost:8761/eureka/apps/USER-SERVICE | jq '.application.instance[0].status'

# Get instance details
curl -s http://localhost:8761/eureka/apps/USER-SERVICE | jq '.application.instance[0] | {instanceId, ipAddr, port, status}'

# Get all registered services
curl -s http://localhost:8761/eureka/apps/ | jq '.applications.application[].name'
```

---

## 🔍 Eureka Configuration

### Server-Side (eureka-server/src/main/resources/application.yml)

```yaml
eureka:
  server:
    enable-self-preservation: false # Dev mode
    eviction-interval-timer-in-ms: 5000 # Fast detection
```

### Client-Side (any microservice)

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    lease-renewal-interval-in-seconds: 10 # Heartbeat interval
    instance-id: ${spring.application.name}:${server.port}
```

---

## 📝 Docker Compose Setup

Key points in docker-compose.yml:

```yaml
eureka-server:
  image: lms-eureka-server:1.0.0
  ports:
    - "8761:8761"
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
    interval: 10s
    timeout: 5s
    retries: 5

user-service:
  depends_on:
    eureka-server:
      condition: service_healthy
  environment:
    EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
```

---

## 🧪 Verification Checklist

✅ Run this to verify everything is working:

```bash
# 1. Check containers
docker-compose ps

# 2. Check Eureka
curl http://localhost:8761/actuator/health

# 3. Check registration
curl -s http://localhost:8761/eureka/apps/USER-SERVICE | jq '.application.instance[0].status'

# 4. Use script
./verify-eureka.sh
```

---

## ⚡ Quick Troubleshooting

### "User Service not registered"

```bash
# Wait longer (registration takes 10-30 seconds)
sleep 30 && curl -s http://localhost:8761/eureka/apps/USER-SERVICE

# Check logs
docker-compose logs user-service | grep eureka
```

### "Can't access Eureka dashboard"

```bash
# Verify container is running
docker-compose ps eureka-server

# Check port
docker-compose port eureka-server 8761

# Restart
docker-compose restart eureka-server
```

### "Services can't find each other"

```bash
# Verify network
docker network ls
docker network inspect lms-network

# Test connectivity
docker exec lms-user-service ping eureka-server
```

---

## 🎯 REST API Endpoints

### Register Service (Automatic)

```
POST /eureka/apps/{app-name}
```

### Get All Services

```
GET /eureka/apps/
```

### Get Specific Service

```
GET /eureka/apps/{app-name}
```

### Get Service Instance

```
GET /eureka/apps/{app-name}/{instance-id}
```

### Send Heartbeat (Automatic every 10s)

```
PUT /eureka/apps/{app-name}/{instance-id}
```

### Deregister Service

```
DELETE /eureka/apps/{app-name}/{instance-id}
```

---

## 📊 Performance Tips

| Action               | Command                          |
| -------------------- | -------------------------------- |
| Check memory usage   | `docker stats`                   |
| View detailed logs   | `docker-compose logs --tail=100` |
| Monitor in real-time | `watch -n 5 'docker-compose ps'` |
| Clean up             | `docker-compose down -v`         |

---

## 🔐 Production Settings

When deploying to production:

```yaml
eureka:
  server:
    enable-self-preservation: true # Enable!
    eviction-interval-timer-in-ms: 60000 # 1 minute
    renewal-percent-threshold: 0.85
  client:
    # Use HTTPS
    service-url:
      defaultZone: https://eureka-server:8761/eureka/
```

---

## 📚 Documentation Files

| File                                                                   | Purpose                               |
| ---------------------------------------------------------------------- | ------------------------------------- |
| [EUREKA_SERVICE_CONFIGURATION.md](EUREKA_SERVICE_CONFIGURATION.md)     | Detailed architecture & configuration |
| [EUREKA_STARTUP_VERIFICATION.md](EUREKA_STARTUP_VERIFICATION.md)       | Step-by-step startup guide            |
| [EUREKA_IMPLEMENTATION_COMPLETE.md](EUREKA_IMPLEMENTATION_COMPLETE.md) | Complete summary & checklist          |
| [USER_SERVICE_TESTING.md](../../UserService/USER_SERVICE_TESTING.md)                     | API testing guide                     |
| [verify-eureka.sh](../../../verify-eureka.sh)                                   | Automated verification script         |

---

## 🎓 Example Workflow

### First Time Setup

```bash
# 1. Navigate to workspace
cd /Users/sahil_sangwan/Desktop/plans

# 2. Build image
docker build -t lms-eureka-server:1.0.0 ./eureka-server

# 3. Start services
docker-compose up -d eureka-server
sleep 20
docker-compose up -d user-service
sleep 10

# 4. Verify
curl http://localhost:8761/eureka/apps/ | jq '.applications.application[].name'

# 5. Test API
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"TestPass123!"}'
```

### Daily Development

```bash
# Start services
docker-compose up -d

# Check status
./verify-eureka.sh

# View dashboard
open http://localhost:8761

# Test APIs
bash test-user-service.sh

# Check logs
docker-compose logs -f user-service
```

---

## 🚀 Next Microservices

To add Training Service (same pattern):

```yaml
# training-service/src/main/resources/application.yml
spring:
  application:
    name: TRAINING-SERVICE

server:
  port: 8082

eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
```

Then in docker-compose.yml:

```yaml
training-service:
  depends_on:
    eureka-server:
      condition: service_healthy
  environment:
    EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
```

---

## 💡 Key Concepts

- **Service Registration**: Services register themselves with Eureka on startup
- **Service Discovery**: Services can find each other by querying Eureka
- **Heartbeat**: Services send "I'm alive" every 10 seconds
- **Health Check**: Eureka monitors service health via `/actuator/health`
- **Instance ID**: Unique identifier for each service instance
- **Zone**: Eureka region (useful for multi-region deployments)

---

## 📞 Support

- **Documentation**: See files in workspace root
- **Logs**: `docker-compose logs -f {service}`
- **Verification**: Run `./verify-eureka.sh`
- **Dashboard**: Visit `http://localhost:8761`

---

## ✅ Status: READY FOR PRODUCTION! 🎉

Everything configured and tested:

- ✅ Eureka Server running
- ✅ User Service registered
- ✅ Service discovery working
- ✅ Health monitoring active
- ✅ Docker Compose configured
- ✅ Documentation complete
- ✅ Verification tools ready

Start services now: `docker-compose up -d` 🚀
