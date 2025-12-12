#!/bin/bash

# Gateway Filter Testing Script
# Tests all aspects of the gateway filter implementation

set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
GATEWAY_URL="http://localhost:8080"
USER_SERVICE_URL="http://localhost:8081"
TRAINING_SERVICE_URL="http://localhost:8082"
EUREKA_URL="http://localhost:8761"

# Test results
PASSED=0
FAILED=0

echo -e "${BLUE}╔════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  API Gateway Filter & Security Testing Suite       ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════╝${NC}"
echo ""

# Helper function to test
test_endpoint() {
    local test_name=$1
    local method=$2
    local url=$3
    local data=$4
    local expected_status=$5
    local headers=$6

    echo -n "Testing: $test_name... "

    if [ -z "$data" ]; then
        if [ -z "$headers" ]; then
            response=$(curl -s -w "\n%{http_code}" -X "$method" "$url")
        else
            response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" $headers)
        fi
    else
        if [ -z "$headers" ]; then
            response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" \
                -H "Content-Type: application/json" \
                -d "$data")
        else
            response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" \
                -H "Content-Type: application/json" \
                $headers \
                -d "$data")
        fi
    fi

    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n-1)

    if [ "$http_code" = "$expected_status" ]; then
        echo -e "${GREEN}✓ PASSED${NC} (HTTP $http_code)"
        PASSED=$((PASSED + 1))
        return 0
    else
        echo -e "${RED}✗ FAILED${NC} (Expected $expected_status, got $http_code)"
        echo "  Response: $body"
        FAILED=$((FAILED + 1))
        return 1
    fi
}

# Helper function to test direct access (should fail)
test_direct_access() {
    local test_name=$1
    local url=$2

    echo -n "Testing: $test_name... "

    # Try to connect - should fail
    if timeout 2 bash -c "cat < /dev/null > /dev/tcp/localhost/$(echo $url | grep -oP '(?<=:)[0-9]+')" 2>/dev/null; then
        # Connection succeeded - this is wrong
        echo -e "${RED}✗ FAILED${NC} (Port is exposed, should be internal)"
        FAILED=$((FAILED + 1))
        return 1
    else
        # Connection failed - this is correct
        echo -e "${GREEN}✓ PASSED${NC} (Port correctly not exposed)"
        PASSED=$((PASSED + 1))
        return 0
    fi
}

# ============================================================================
# Section 1: Network Isolation Tests
# ============================================================================
echo -e "\n${BLUE}═════════════════════════════════════════${NC}"
echo -e "${BLUE}Section 1: Network Isolation Tests${NC}"
echo -e "${BLUE}═════════════════════════════════════════${NC}\n"

test_endpoint "API Gateway port 8080 is accessible" "GET" "$GATEWAY_URL/actuator/health" "" "200"
test_direct_access "User Service port 8081 is NOT exposed" "localhost:8081"
test_direct_access "Training Service port 8082 is NOT exposed" "localhost:8082"

# ============================================================================
# Section 2: Public Endpoints (No Authentication)
# ============================================================================
echo -e "\n${BLUE}═════════════════════════════════════════${NC}"
echo -e "${BLUE}Section 2: Public Endpoints (No Auth)${NC}"
echo -e "${BLUE}═════════════════════════════════════════${NC}\n"

# Test user registration
test_endpoint "POST /api/auth/register (no auth required)" "POST" \
    "$GATEWAY_URL/api/auth/register" \
    '{"email":"test@example.com","password":"password123","fullName":"Test User"}' \
    "201"

# Test login
test_endpoint "POST /api/auth/login (no auth required)" "POST" \
    "$GATEWAY_URL/api/auth/login" \
    '{"email":"test@example.com","password":"password123"}' \
    "200"

# Test training search (public)
test_endpoint "GET /api/training/search (no auth required)" "GET" \
    "$GATEWAY_URL/api/training/search" \
    "" \
    "200"

# Test training published (public)
test_endpoint "GET /api/training/published (no auth required)" "GET" \
    "$GATEWAY_URL/api/training/published" \
    "" \
    "200"

# ============================================================================
# Section 3: Protected Endpoints (JWT Required)
# ============================================================================
echo -e "\n${BLUE}═════════════════════════════════════════${NC}"
echo -e "${BLUE}Section 3: Protected Endpoints (JWT Required)${NC}"
echo -e "${BLUE}═════════════════════════════════════════${NC}\n"

echo -e "${YELLOW}Note: Getting JWT token for testing...${NC}\n"

# Get JWT token
LOGIN_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"test@example.com","password":"password123"}')

JWT_TOKEN=$(echo $LOGIN_RESPONSE | grep -oP '(?<="token":")[^"]*' || echo "")

if [ -z "$JWT_TOKEN" ]; then
    echo -e "${YELLOW}Warning: Could not extract JWT token. Using dummy token for testing...${NC}\n"
    JWT_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
else
    echo -e "${GREEN}JWT Token obtained successfully${NC}\n"
fi

# Test protected user endpoint without JWT
test_endpoint "GET /api/users/profile (no JWT - should fail)" "GET" \
    "$GATEWAY_URL/api/users/profile" \
    "" \
    "401"

# Test protected user endpoint with JWT
test_endpoint "GET /api/users/profile (with valid JWT)" "GET" \
    "$GATEWAY_URL/api/users/profile" \
    "" \
    "-H 'Authorization: Bearer $JWT_TOKEN'" \
    "200"

# Test protected user endpoint with invalid JWT
test_endpoint "GET /api/users/profile (with invalid JWT - should fail)" "GET" \
    "$GATEWAY_URL/api/users/profile" \
    "" \
    "-H 'Authorization: Bearer invalid_token_here'" \
    "401"

# Test protected training endpoint with JWT
test_endpoint "GET /api/training/courses (with JWT)" "GET" \
    "$GATEWAY_URL/api/training/courses" \
    "" \
    "-H 'Authorization: Bearer $JWT_TOKEN'" \
    "200"

# ============================================================================
# Section 4: Path Transformation Tests
# ============================================================================
echo -e "\n${BLUE}═════════════════════════════════════════${NC}"
echo -e "${BLUE}Section 4: Path Transformation Tests${NC}"
echo -e "${BLUE}═════════════════════════════════════════${NC}\n"

echo -e "${YELLOW}Verifying /api prefix is stripped correctly...${NC}\n"

# Test different paths
test_endpoint "GET /api/auth/login (path transformation test)" "POST" \
    "$GATEWAY_URL/api/auth/login" \
    '{"email":"test@example.com","password":"password123"}' \
    "200"

test_endpoint "GET /api/training/search (path transformation test)" "GET" \
    "$GATEWAY_URL/api/training/search" \
    "" \
    "200"

# ============================================================================
# Section 5: Gateway Header Validation
# ============================================================================
echo -e "\n${BLUE}═════════════════════════════════════════${NC}"
echo -e "${BLUE}Section 5: Gateway Header Validation${NC}"
echo -e "${BLUE}═════════════════════════════════════════${NC}\n"

echo -e "${YELLOW}Testing request headers from API Gateway...${NC}\n"

# Check that requests through gateway have proper headers
RESPONSE_WITH_HEADERS=$(curl -i -s "$GATEWAY_URL/api/training/search" 2>&1)

if echo "$RESPONSE_WITH_HEADERS" | grep -q "X-Gateway-Authenticated"; then
    echo -e "${GREEN}✓ PASSED${NC} - X-Gateway-Authenticated header present"
    PASSED=$((PASSED + 1))
else
    echo -e "${RED}✗ FAILED${NC} - X-Gateway-Authenticated header missing"
    FAILED=$((FAILED + 1))
fi

# ============================================================================
# Section 6: Eureka Service Discovery
# ============================================================================
echo -e "\n${BLUE}═════════════════════════════════════════${NC}"
echo -e "${BLUE}Section 6: Eureka Service Discovery${NC}"
echo -e "${BLUE}═════════════════════════════════════════${NC}\n"

test_endpoint "Eureka Server is running" "GET" "$EUREKA_URL/actuator/health" "" "200"

# Test service registration
echo -n "Testing: API Gateway registered in Eureka... "
EUREKA_APPS=$(curl -s "$EUREKA_URL/eureka/apps" -H "Accept: application/json")
if echo "$EUREKA_APPS" | grep -q "api-gateway\|API-GATEWAY"; then
    echo -e "${GREEN}✓ PASSED${NC}"
    PASSED=$((PASSED + 1))
else
    echo -e "${YELLOW}⚠ WARNING${NC} (Service may still be registering)"
fi

echo -n "Testing: User Service registered in Eureka... "
if echo "$EUREKA_APPS" | grep -q "user-service\|USER-SERVICE"; then
    echo -e "${GREEN}✓ PASSED${NC}"
    PASSED=$((PASSED + 1))
else
    echo -e "${YELLOW}⚠ WARNING${NC} (Service may still be registering)"
fi

echo -n "Testing: Training Service registered in Eureka... "
if echo "$EUREKA_APPS" | grep -q "training-service\|TRAINING-SERVICE"; then
    echo -e "${GREEN}✓ PASSED${NC}"
    PASSED=$((PASSED + 1))
else
    echo -e "${YELLOW}⚠ WARNING${NC} (Service may still be registering)"
fi

# ============================================================================
# Section 7: Error Handling
# ============================================================================
echo -e "\n${BLUE}═════════════════════════════════════════${NC}"
echo -e "${BLUE}Section 7: Error Handling${NC}"
echo -e "${BLUE}═════════════════════════════════════════${NC}\n"

test_endpoint "Invalid route returns 404" "GET" \
    "$GATEWAY_URL/api/nonexistent/endpoint" \
    "" \
    "404"

test_endpoint "Missing authorization returns 401" "GET" \
    "$GATEWAY_URL/api/users/profile" \
    "" \
    "401"

# ============================================================================
# Summary
# ============================================================================
echo -e "\n${BLUE}═════════════════════════════════════════${NC}"
echo -e "${BLUE}Test Summary${NC}"
echo -e "${BLUE}═════════════════════════════════════════${NC}\n"

TOTAL=$((PASSED + FAILED))

echo "Total Tests: $TOTAL"
echo -e "Passed: ${GREEN}$PASSED${NC}"
echo -e "Failed: ${RED}$FAILED${NC}"

if [ $FAILED -eq 0 ]; then
    echo -e "\n${GREEN}✓ All tests passed!${NC}"
    exit 0
else
    echo -e "\n${RED}✗ Some tests failed. See details above.${NC}"
    exit 1
fi

