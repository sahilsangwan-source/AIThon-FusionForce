#!/bin/bash

# API Gateway Integration Test Script
# Tests authentication, routing, and inter-service communication

echo "================================================"
echo "  API GATEWAY INTEGRATION TEST"
echo "================================================"
echo ""

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
API_GATEWAY="http://localhost:8080"
TEST_EMAIL="testuser@example.com"
TEST_PASSWORD="Test@123"
TOKEN=""

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0

# Function to print test result
test_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ PASSED${NC}: $2"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}✗ FAILED${NC}: $2"
        ((TESTS_FAILED++))
    fi
    echo ""
}

echo "1. Testing API Gateway Health"
echo "   Endpoint: $API_GATEWAY/actuator/health"
HEALTH_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "$API_GATEWAY/actuator/health")
if [ "$HEALTH_RESPONSE" = "200" ]; then
    test_result 0 "API Gateway is healthy"
else
    test_result 1 "API Gateway health check failed (HTTP $HEALTH_RESPONSE)"
fi

echo "2. Testing User Registration (Public Endpoint)"
echo "   POST $API_GATEWAY/api/auth/register"
REGISTER_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$API_GATEWAY/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$TEST_EMAIL\",
    \"password\": \"$TEST_PASSWORD\",
    \"firstName\": \"Test\",
    \"lastName\": \"User\"
  }")

REGISTER_CODE=$(echo "$REGISTER_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
if [ "$REGISTER_CODE" = "201" ] || [ "$REGISTER_CODE" = "409" ]; then
    test_result 0 "User registration endpoint accessible (HTTP $REGISTER_CODE)"
else
    test_result 1 "User registration failed (HTTP $REGISTER_CODE)"
fi

echo "3. Testing User Login (Public Endpoint)"
echo "   POST $API_GATEWAY/api/auth/login"
LOGIN_RESPONSE=$(curl -s -X POST "$API_GATEWAY/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$TEST_EMAIL\",
    \"password\": \"$TEST_PASSWORD\"
  }")

TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('accessToken', ''))" 2>/dev/null)

if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    test_result 0 "User login successful, JWT token received"
    echo "   Token (first 50 chars): ${TOKEN:0:50}..."
else
    test_result 1 "User login failed or no token received"
    echo "   Response: $LOGIN_RESPONSE"
fi

echo "4. Testing Protected Endpoint WITHOUT Token"
echo "   GET $API_GATEWAY/api/users"
UNAUTH_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "$API_GATEWAY/api/users")
if [ "$UNAUTH_RESPONSE" = "401" ]; then
    test_result 0 "Protected endpoint correctly rejects unauthenticated requests (HTTP 401)"
else
    test_result 1 "Protected endpoint should return 401, got HTTP $UNAUTH_RESPONSE"
fi

if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    echo "5. Testing Protected Endpoint WITH Token"
    echo "   GET $API_GATEWAY/api/users"
    AUTH_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" "$API_GATEWAY/api/users?page=0&size=5" \
      -H "Authorization: Bearer $TOKEN")
    
    AUTH_CODE=$(echo "$AUTH_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
    if [ "$AUTH_CODE" = "200" ]; then
        test_result 0 "Protected user endpoint accessible with JWT token"
    else
        test_result 1 "Protected user endpoint failed with token (HTTP $AUTH_CODE)"
    fi

    echo "6. Testing Training Service via Gateway"
    echo "   GET $API_GATEWAY/api/trainings/published"
    TRAINING_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "$API_GATEWAY/api/trainings/published?page=0&size=5")
    if [ "$TRAINING_RESPONSE" = "200" ]; then
        test_result 0 "Training service accessible via gateway (public endpoint)"
    else
        test_result 1 "Training service access failed (HTTP $TRAINING_RESPONSE)"
    fi

    echo "7. Testing Protected Training Endpoint"
    echo "   GET $API_GATEWAY/api/trainings"
    TRAINING_AUTH_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "$API_GATEWAY/api/trainings?page=0&size=5" \
      -H "Authorization: Bearer $TOKEN")
    if [ "$TRAINING_AUTH_RESPONSE" = "200" ]; then
        test_result 0 "Protected training endpoint accessible with JWT token"
    else
        test_result 1 "Protected training endpoint failed (HTTP $TRAINING_AUTH_RESPONSE)"
    fi

    echo "8. Testing Enrollment Endpoint (Inter-service Communication)"
    echo "   GET $API_GATEWAY/api/enrollments/my-trainings"
    ENROLLMENT_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "$API_GATEWAY/api/enrollments/my-trainings?page=0&size=5" \
      -H "Authorization: Bearer $TOKEN")
    if [ "$ENROLLMENT_RESPONSE" = "200" ]; then
        test_result 0 "Enrollment endpoint accessible (training-service)"
    else
        test_result 1 "Enrollment endpoint failed (HTTP $ENROLLMENT_RESPONSE)"
    fi
else
    echo "Skipping authenticated endpoint tests (no valid token)"
    TESTS_FAILED=$((TESTS_FAILED + 4))
fi

echo "9. Testing Direct Service Access (Should Fail)"
echo "   Direct access to services should be blocked"
USER_DIRECT=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8081/api/users" 2>/dev/null)
TRAINING_DIRECT=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8082/api/trainings" 2>/dev/null)

if [ "$USER_DIRECT" = "000" ] || [ "$USER_DIRECT" = "" ]; then
    test_result 0 "User service not accessible directly (ports not exposed)"
else
    test_result 1 "User service should not be accessible directly (got HTTP $USER_DIRECT)"
fi

if [ "$TRAINING_DIRECT" = "000" ] || [ "$TRAINING_DIRECT" = "" ]; then
    test_result 0 "Training service not accessible directly (ports not exposed)"
else
    test_result 1 "Training service should not be accessible directly (got HTTP $TRAINING_DIRECT)"
fi

echo "================================================"
echo "  TEST SUMMARY"
echo "================================================"
echo -e "${GREEN}Tests Passed: $TESTS_PASSED${NC}"
echo -e "${RED}Tests Failed: $TESTS_FAILED${NC}"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}✗ Some tests failed${NC}"
    exit 1
fi
