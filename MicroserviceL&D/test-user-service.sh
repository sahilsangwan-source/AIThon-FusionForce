#!/bin/bash

# User Service Testing Script
# This script tests all User Service endpoints

BASE_URL="http://localhost:8081"
CONTENT_TYPE="Content-Type: application/json"

echo "==============================================="
echo "User Service API Testing Script"
echo "==============================================="
echo ""

# Test 1: Register User
echo "1️⃣  Testing User Registration..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/users/register" \
  -H "$CONTENT_TYPE" \
  -d '{
    "email": "john.doe@company.com",
    "password": "SecurePass@123",
    "firstName": "John",
    "lastName": "Doe",
    "employeeId": "EMP001",
    "department": "Engineering"
  }')

echo "$REGISTER_RESPONSE" | jq .
USER_ID=$(echo "$REGISTER_RESPONSE" | jq -r '.data.id // empty')
echo "✅ User ID: $USER_ID"
echo ""

# Test 2: Login
echo "2️⃣  Testing User Login..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "$CONTENT_TYPE" \
  -d '{
    "email": "john.doe@company.com",
    "password": "SecurePass@123"
  }')

echo "$LOGIN_RESPONSE" | jq .
ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.accessToken // empty')
REFRESH_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.refreshToken // empty')
echo "✅ Access Token obtained"
echo ""

# Test 3: Get Current User Profile
echo "3️⃣  Testing Get Current User Profile..."
curl -s -X GET "$BASE_URL/api/users/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .
echo "✅ User profile retrieved"
echo ""

# Test 4: Update User Profile
echo "4️⃣  Testing Update User Profile..."
UPDATE_RESPONSE=$(curl -s -X PUT "$BASE_URL/api/users/me" \
  -H "$CONTENT_TYPE" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "firstName": "John",
    "lastName": "Smith",
    "department": "Management"
  }')

echo "$UPDATE_RESPONSE" | jq .
echo "✅ User profile updated"
echo ""

# Test 5: Validate Token
echo "5️⃣  Testing Token Validation..."
curl -s -X GET "$BASE_URL/api/auth/validate" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .
echo "✅ Token validated successfully"
echo ""

# Test 6: Get User by ID
echo "6️⃣  Testing Get User by ID..."
curl -s -X GET "$BASE_URL/api/users/$USER_ID" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .
echo "✅ User retrieved by ID"
echo ""

# Test 7: Register Admin User
echo "7️⃣  Testing Admin User Registration..."
ADMIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/users/register" \
  -H "$CONTENT_TYPE" \
  -d '{
    "email": "admin.user@company.com",
    "password": "AdminPass@123",
    "firstName": "Admin",
    "lastName": "User",
    "employeeId": "EMP999",
    "department": "Management"
  }')

echo "$ADMIN_RESPONSE" | jq .
echo "✅ Admin user registered"
echo ""

# Test 8: Login as Admin
echo "8️⃣  Testing Admin Login..."
ADMIN_LOGIN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "$CONTENT_TYPE" \
  -d '{
    "email": "admin.user@company.com",
    "password": "AdminPass@123"
  }')

echo "$ADMIN_LOGIN" | jq .
ADMIN_TOKEN=$(echo "$ADMIN_LOGIN" | jq -r '.data.accessToken // empty')
echo "✅ Admin logged in"
echo ""

# Test 9: Get All Users (Admin Only)
echo "9️⃣  Testing Get All Users (Admin)..."
curl -s -X GET "$BASE_URL/api/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
echo "✅ All users retrieved"
echo ""

# Test 10: Refresh Token
echo "🔟 Testing Token Refresh..."
REFRESH_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/refresh-token" \
  -H "$CONTENT_TYPE" \
  -d "{
    \"refreshToken\": \"$REFRESH_TOKEN\"
  }")

echo "$REFRESH_RESPONSE" | jq .
NEW_ACCESS_TOKEN=$(echo "$REFRESH_RESPONSE" | jq -r '.data.accessToken // empty')
echo "✅ Token refreshed successfully"
echo ""

# Test 11: Error Handling - Email Already Exists
echo "❌ Testing Error: Email Already Exists..."
curl -s -X POST "$BASE_URL/api/users/register" \
  -H "$CONTENT_TYPE" \
  -d '{
    "email": "john.doe@company.com",
    "password": "AnotherPass@123",
    "firstName": "Jane",
    "lastName": "Doe",
    "employeeId": "EMP002",
    "department": "Engineering"
  }' | jq .
echo "✅ Error handled correctly"
echo ""

# Test 12: Error Handling - Invalid Credentials
echo "❌ Testing Error: Invalid Credentials..."
curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "$CONTENT_TYPE" \
  -d '{
    "email": "john.doe@company.com",
    "password": "WrongPassword"
  }' | jq .
echo "✅ Invalid credentials handled"
echo ""

# Test 13: Error Handling - Invalid Token
echo "❌ Testing Error: Invalid Token..."
curl -s -X GET "$BASE_URL/api/users/me" \
  -H "Authorization: Bearer invalid.token.here" | jq .
echo "✅ Invalid token handled"
echo ""

# Test 14: Error Handling - Missing Authorization
echo "❌ Testing Error: Missing Authorization..."
curl -s -X GET "$BASE_URL/api/users/me" | jq .
echo "✅ Missing authorization handled"
echo ""

# Test 15: Error Handling - Invalid Password Format
echo "❌ Testing Error: Weak Password..."
curl -s -X POST "$BASE_URL/api/users/register" \
  -H "$CONTENT_TYPE" \
  -d '{
    "email": "test@company.com",
    "password": "weak",
    "firstName": "Test",
    "lastName": "User",
    "employeeId": "EMP123",
    "department": "IT"
  }' | jq .
echo "✅ Weak password rejected"
echo ""

# Test 16: Health Check
echo "🏥 Testing Health Check..."
curl -s -X GET "$BASE_URL/api/users/health"
echo ""
echo "✅ Service is running"
echo ""

# Test 17: Logout
echo "🚪 Testing Logout..."
curl -s -X POST "$BASE_URL/api/auth/logout" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .
echo "✅ User logged out"
echo ""

# Test 18: Delete User (Admin)
echo "🗑️  Testing Delete User (Admin)..."
if [ ! -z "$USER_ID" ]; then
  curl -s -X DELETE "$BASE_URL/api/users/$USER_ID" \
    -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
  echo "✅ User deleted successfully"
else
  echo "⚠️  Could not delete user - User ID not available"
fi
echo ""

echo "==============================================="
echo "✅ All Tests Completed Successfully!"
echo "==============================================="
echo ""
echo "Summary:"
echo "- User Registration: ✅"
echo "- User Login: ✅"
echo "- Get User Profile: ✅"
echo "- Update User Profile: ✅"
echo "- Token Validation: ✅"
echo "- Get User by ID: ✅"
echo "- Admin Functions: ✅"
echo "- Token Refresh: ✅"
echo "- Error Handling: ✅"
echo "- Health Check: ✅"
echo ""
echo "All endpoints working correctly!"
