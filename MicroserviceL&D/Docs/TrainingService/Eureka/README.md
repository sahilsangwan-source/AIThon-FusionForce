# 🔍 Eureka Service Discovery Documentation

Service discovery and registration documentation for Eureka Server.

## 📖 Files in This Directory

### 1. **EUREKA_START_HERE.txt** ⭐ BEGIN HERE
- Quick start guide for Eureka setup
- Getting started checklist
- Basic commands
- **Read Time:** 5 minutes

### 2. **EUREKA_QUICK_REFERENCE.md**
- Quick reference for common tasks
- Service registration overview
- Dashboard access
- Common commands
- **Read Time:** 5 minutes

### 3. **EUREKA_SERVICE_CONFIGURATION.md**
- Detailed configuration guide
- Property explanations
- Client configuration
- Server configuration
- **Read Time:** 15 minutes

### 4. **EUREKA_STARTUP_VERIFICATION.md**
- Verification procedures
- Health check commands
- Service registration verification
- Dashboard navigation
- **Read Time:** 10 minutes

### 5. **EUREKA_IMPLEMENTATION_COMPLETE.md**
- Implementation summary
- What was done
- Configuration details
- **Read Time:** 10 minutes

### 6. **EUREKA_FINAL_STATUS.md**
- Final status report
- Verification checklist
- All services status
- **Read Time:** 5 minutes

### 7. **EUREKA_COMPLETE_SUMMARY.txt**
- Complete summary
- Features overview
- Quick reference

---

## 🎯 Quick Start

### Access Eureka Dashboard
```
http://localhost:8761
```

### Check Service Registration
```bash
curl http://localhost:8761/eureka/apps \
  -H "Accept: application/json"
```

### View Service Health
```bash
curl http://localhost:8761/actuator/health
```

---

## 🏗️ What is Eureka?

Eureka is Netflix's service discovery tool that:

✅ **Auto-discovers services** - Services register themselves
✅ **Monitors health** - Continuous health checks
✅ **Enables load balancing** - Distributes requests across instances
✅ **Handles failover** - Automatic instance failover
✅ **Scales easily** - Add/remove instances dynamically

---

## 📊 Services Registered

### API Gateway
- **Service ID:** API-GATEWAY
- **Port:** 8080
- **Status:** UP
- **URL:** http://localhost:8080

### User Service
- **Service ID:** USER-SERVICE
- **Port:** 8081 (Internal)
- **Status:** UP
- **URL:** http://user-service:8081

### Training Service
- **Service ID:** TRAINING-SERVICE
- **Port:** 8082 (Internal)
- **Status:** UP
- **URL:** http://training-service:8082

### Eureka Server
- **Service ID:** EUREKA-SERVER
- **Port:** 8761
- **Status:** UP
- **URL:** http://localhost:8761

---

## 🔧 Key Configuration

### Server Configuration
```yaml
eureka:
  server:
    enable-self-preservation: true
    eviction-interval-timer-in-ms: 60000
```

### Client Configuration
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
    fetch-registry: true
    register-with-eureka: true
```

---

## 📈 Dashboard Features

### Services View
- List of all registered services
- Status (UP/DOWN)
- Instance count
- Links to service details

### Instance Details
- Service name
- Status
- Last heartbeat
- Health information

### Configuration
- Service URLs
- Port information
- Metadata

---

## 🚀 How to Use This Documentation

### For Setup (10 minutes)
1. **EUREKA_START_HERE.txt** - Getting started
2. **EUREKA_STARTUP_VERIFICATION.md** - Verify setup

### For Configuration (20 minutes)
1. **EUREKA_SERVICE_CONFIGURATION.md** - Detailed configuration
2. **EUREKA_QUICK_REFERENCE.md** - Quick commands

### For Verification
1. **EUREKA_FINAL_STATUS.md** - Verify all services
2. Dashboard at http://localhost:8761

---

## 🔍 Common Tasks

### Check if Service is Registered
```bash
# Method 1: API
curl http://localhost:8761/eureka/apps/SERVICE-NAME

# Method 2: Dashboard
open http://localhost:8761
```

### Check Service Health
```bash
curl http://localhost:8761/eureka/apps/SERVICE-NAME/status
```

### Deregister Service
```bash
curl -X DELETE http://localhost:8761/eureka/apps/SERVICE-NAME/INSTANCE-ID
```

### Force Service Status Update
```bash
curl -X PUT http://localhost:8761/eureka/apps/SERVICE-NAME/INSTANCE-ID/status?value=UP
```

---

## 📊 Service Registration Flow

```
Service Startup
    ↓
Register with Eureka
    ↓
Send Heartbeat (every 30s)
    ↓
Eureka Monitoring
    ↓
Health Status Available to Clients
    ↓
Service Ready for Load Balancing
```

---

## 🔄 Load Balancing

Eureka enables automatic load balancing:

```
API Gateway Request
    ↓
Query Eureka for SERVICE-NAME
    ↓
Get List of Available Instances
    ↓
Select Instance (Round-robin)
    ↓
Route to Selected Instance
    ↓
Response Back to Client
```

---

## ❌ Troubleshooting

### Service Not Showing in Eureka

**Check:**
1. Service is running
2. EUREKA_SERVER_URL is correct
3. Network connectivity
4. Service logs for errors

**Solutions:**
- Restart the service
- Check EUREKA_SERVER_URL environment variable
- Review service logs
- Verify network connectivity

### Service Shows as DOWN

**Check:**
1. Health check endpoint is working
2. Service logs for errors
3. Network connectivity

**Solutions:**
- Check service health endpoint
- Review service logs
- Restart service if needed

### Cannot Access Dashboard

**Check:**
1. Eureka server is running
2. Port 8761 is accessible
3. Network connectivity

**Solutions:**
- Verify Eureka is running: `docker-compose ps | grep eureka`
- Check logs: `docker logs lms-eureka`
- Restart Eureka if needed

---

## 📈 Statistics

- **Services Managed:** 4
- **Total Instances:** 4+
- **Health Check Interval:** 30 seconds
- **Renewal Interval:** 10 seconds
- **Eviction Timeout:** 90 seconds

---

## 🎯 Key Features

✅ **Auto-Discovery** - Services register automatically
✅ **Health Monitoring** - Continuous health checks
✅ **Load Balancing** - Distribute requests
✅ **Failover** - Automatic instance failover
✅ **Scaling** - Dynamic instance addition
✅ **Dashboard** - Visual management
✅ **REST API** - Programmatic access

---

## 📞 Quick Reference

| Task | Command |
|------|---------|
| View Dashboard | http://localhost:8761 |
| Check Services | curl http://localhost:8761/eureka/apps -H "Accept: application/json" |
| Service Health | curl http://localhost:8761/actuator/health |
| Service Status | curl http://localhost:8761/eureka/apps/SERVICE-NAME |

---

## ✨ Configuration Best Practices

1. **Set correct EUREKA_SERVER_URL** - Must match server location
2. **Configure health checks** - Ensure endpoint is working
3. **Monitor heartbeats** - Check service connectivity
4. **Set proper timeouts** - Adjust for your environment
5. **Use self-preservation** - Protect against network issues
6. **Monitor dashboard** - Check service status regularly

---

## 📚 Related Documentation

- **API Gateway:** See `../APIGateway/` for gateway implementation
- **User Service:** See `../UserService/` for user service details
- **Training Service:** See `../TrainingService/` for training details
- **General:** See `../General/` for project overview

---

## 🎉 Status

✅ **Eureka Server** - Running and configured
✅ **All Services** - Registered and UP
✅ **Load Balancing** - Enabled
✅ **Health Monitoring** - Active

---

**Start Reading:** EUREKA_START_HERE.txt

**View Dashboard:** http://localhost:8761

For navigation to all docs, see the parent directory: `../README.md`

