#!/bin/bash

# Complete Deployment and Testing Script
# Rebuilds all services and tests the API Gateway setup

set -e  # Exit on error

echo "================================================"
echo "  LMS DEPLOYMENT & TESTING SCRIPT"
echo "================================================"
echo ""

# Color codes
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Change to script directory
cd "$(dirname "$0")"

echo -e "${YELLOW}Step 1: Stopping existing containers${NC}"
docker-compose down -v
echo -e "${GREEN}✓ Containers stopped${NC}"
echo ""

echo -e "${YELLOW}Step 2: Building services${NC}"
echo "Building Eureka Server..."
cd eureka-server && mvn clean package -DskipTests && cd ..

echo "Building User Service..."
cd user-service && mvn clean package -DskipTests && cd ..

echo "Building Training Service..."
cd training-service && mvn clean package -DskipTests && cd ..

echo "Building API Gateway..."
cd api-gateway && mvn clean package -DskipTests && cd ..

echo -e "${GREEN}✓ All services built successfully${NC}"
echo ""

echo -e "${YELLOW}Step 3: Starting infrastructure services${NC}"
docker-compose up -d postgres redis zookeeper kafka minio
echo "Waiting for infrastructure to be healthy (30 seconds)..."
sleep 30
echo -e "${GREEN}✓ Infrastructure services started${NC}"
echo ""

echo -e "${YELLOW}Step 4: Starting Eureka Server${NC}"
docker-compose up -d eureka-server
echo "Waiting for Eureka to be healthy (20 seconds)..."
sleep 20
echo -e "${GREEN}✓ Eureka Server started${NC}"
echo ""

echo -e "${YELLOW}Step 5: Starting API Gateway${NC}"
docker-compose up -d api-gateway
echo "Waiting for API Gateway to be healthy (25 seconds)..."
sleep 25
echo -e "${GREEN}✓ API Gateway started${NC}"
echo ""

echo -e "${YELLOW}Step 6: Starting Microservices${NC}"
docker-compose up -d user-service training-service
echo "Waiting for services to register with Eureka (40 seconds)..."
sleep 40
echo -e "${GREEN}✓ Microservices started${NC}"
echo ""

echo -e "${YELLOW}Step 7: Verifying service health${NC}"
echo ""

echo "Checking API Gateway..."
GATEWAY_HEALTH=$(curl -s http://localhost:8080/actuator/health | grep -o '"status":"UP"' || echo "DOWN")
if [[ "$GATEWAY_HEALTH" == *"UP"* ]]; then
    echo -e "${GREEN}✓ API Gateway is UP${NC}"
else
    echo -e "${RED}✗ API Gateway is DOWN${NC}"
fi

echo "Checking Eureka Server..."
curl -s http://localhost:8761/eureka/apps | grep -q "USER-SERVICE" && \
    echo -e "${GREEN}✓ User Service registered${NC}" || \
    echo -e "${RED}✗ User Service not registered${NC}"

curl -s http://localhost:8761/eureka/apps | grep -q "TRAINING-SERVICE" && \
    echo -e "${GREEN}✓ Training Service registered${NC}" || \
    echo -e "${RED}✗ Training Service not registered${NC}"

curl -s http://localhost:8761/eureka/apps | grep -q "API-GATEWAY" && \
    echo -e "${GREEN}✓ API Gateway registered${NC}" || \
    echo -e "${RED}✗ API Gateway not registered${NC}"

echo ""

echo -e "${YELLOW}Step 8: Running integration tests${NC}"
echo ""
chmod +x test-api-gateway.sh
./test-api-gateway.sh

echo ""
echo "================================================"
echo "  DEPLOYMENT COMPLETE"
echo "================================================"
echo ""
echo "Services running:"
echo "  • API Gateway:     http://localhost:8080"
echo "  • Eureka Dashboard: http://localhost:8761"
echo ""
echo "All requests must go through API Gateway:"
echo "  • Auth:     POST http://localhost:8080/api/auth/login"
echo "  • Users:    GET  http://localhost:8080/api/users"
echo "  • Trainings: GET  http://localhost:8080/api/trainings"
echo ""
echo "View logs: docker-compose logs -f [service-name]"
echo "Stop all:  docker-compose down"
echo ""
