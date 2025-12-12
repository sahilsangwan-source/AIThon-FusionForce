#!/bin/bash

# Eureka Service Verification Script
# Comprehensive checks for Eureka Server and User Service registration

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
EUREKA_URL="http://localhost:8761"
USER_SERVICE_URL="http://localhost:8081"
EUREKA_CONTAINER="lms-eureka"
USER_SERVICE_CONTAINER="lms-user-service"

# Helper functions
print_header() {
    echo -e "\n${BLUE}=== $1 ===${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Check function
check_container() {
    local container=$1
    local name=$2
    
    if docker-compose ps $container 2>/dev/null | grep -q "Up"; then
        print_success "$name container is running"
        return 0
    else
        print_error "$name container is NOT running"
        return 1
    fi
}

# Check endpoint
check_endpoint() {
    local url=$1
    local name=$2
    
    if curl -s -f -m 5 "$url" > /dev/null 2>&1; then
        print_success "$name is accessible ($url)"
        return 0
    else
        print_error "$name is NOT accessible ($url)"
        return 1
    fi
}

# Get JSON from endpoint
get_json() {
    local url=$1
    curl -s "$url" 2>/dev/null || echo "{}"
}

# Main verification flow
main() {
    echo -e "${BLUE}╔════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║     Eureka Service & User Service Verification    ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════════════╝${NC}"
    
    local all_passed=true
    
    # Check 1: Container Status
    print_header "1. Container Status"
    
    if ! check_container "eureka-server" "Eureka Server"; then
        all_passed=false
    fi
    
    if ! check_container "user-service" "User Service"; then
        all_passed=false
    fi
    
    # Check 2: Eureka Accessibility
    print_header "2. Eureka Server Accessibility"
    
    if ! check_endpoint "$EUREKA_URL/actuator/health" "Eureka Health"; then
        all_passed=false
        echo -e "${YELLOW}Trying to start Eureka...${NC}"
        docker-compose up -d eureka-server 2>/dev/null || true
        sleep 10
        if ! check_endpoint "$EUREKA_URL/actuator/health" "Eureka Health (retry)"; then
            all_passed=false
        fi
    fi
    
    # Check 3: Eureka Health Status
    print_header "3. Eureka Health Status"
    
    eureka_health=$(get_json "$EUREKA_URL/actuator/health" | grep -o '"status":"[^"]*"' || echo '"status":"UNKNOWN"')
    if [[ $eureka_health == *"UP"* ]]; then
        print_success "Eureka health status: UP"
    else
        print_warning "Eureka health status: $eureka_health"
    fi
    
    # Check 4: User Service Accessibility
    print_header "4. User Service Accessibility"
    
    if ! check_endpoint "$USER_SERVICE_URL/actuator/health" "User Service Health"; then
        all_passed=false
        echo -e "${YELLOW}Trying to start User Service...${NC}"
        docker-compose up -d user-service 2>/dev/null || true
        sleep 15
        if ! check_endpoint "$USER_SERVICE_URL/actuator/health" "User Service Health (retry)"; then
            all_passed=false
        fi
    fi
    
    # Check 5: Service Registration with Eureka
    print_header "5. Service Registration Status"
    
    # Get registered applications
    registered_apps=$(get_json "$EUREKA_URL/eureka/apps/" | grep -o '"name":"[^"]*"' | head -10)
    
    if [[ $registered_apps == *"USER-SERVICE"* ]]; then
        print_success "User Service is registered with Eureka"
        
        # Get instance details
        instance_info=$(get_json "$EUREKA_URL/eureka/apps/USER-SERVICE")
        instance_status=$(echo "$instance_info" | grep -o '"status":"[^"]*"' | head -1)
        
        if [[ $instance_status == *"UP"* ]]; then
            print_success "User Service instance status: UP"
        else
            print_warning "User Service instance status: $instance_status"
        fi
    else
        print_error "User Service is NOT registered with Eureka"
        print_warning "Registered apps: $registered_apps"
        all_passed=false
    fi
    
    # Check 6: User Service Instance Details
    print_header "6. User Service Instance Details"
    
    instance_json=$(get_json "$EUREKA_URL/eureka/apps/USER-SERVICE")
    
    if [[ ! -z "$instance_json" && $instance_json != "{}" ]]; then
        echo "Instance Details:"
        echo "$instance_json" | jq '.application.instance[0] | {
            instanceId: .instanceId,
            app: .app,
            ipAddr: .ipAddr,
            port: .port."$",
            status: .status,
            leaseInfo: {
                renewalIntervalInSecs: .leaseInfo.renewalIntervalInSecs,
                durationInSecs: .leaseInfo.durationInSecs
            }
        }' 2>/dev/null || print_warning "Could not parse instance details"
    fi
    
    # Check 7: User Service Health Details
    print_header "7. User Service Health Components"
    
    health_json=$(get_json "$USER_SERVICE_URL/actuator/health")
    
    if [[ ! -z "$health_json" && $health_json != "{}" ]]; then
        echo "Health Status:"
        echo "$health_json" | jq '{
            status: .status,
            components: [.components | keys[]]
        }' 2>/dev/null || echo "$health_json"
    fi
    
    # Check 8: Network Connectivity
    print_header "8. Network Connectivity"
    
    if docker exec $USER_SERVICE_CONTAINER curl -s http://eureka-server:8761/actuator/health > /dev/null 2>&1; then
        print_success "User Service can reach Eureka via Docker network"
    else
        print_error "User Service cannot reach Eureka"
        all_passed=false
    fi
    
    # Check 9: Docker Logs for Errors
    print_header "9. Recent Log Analysis"
    
    echo "Eureka logs (last 5 lines with 'eureka' or 'ERROR'):"
    docker-compose logs eureka-server 2>/dev/null | grep -i "eureka\|error\|started\|registration" | tail -5 || echo "No logs found"
    
    echo -e "\nUser Service logs (last 5 lines with 'eureka' or 'ERROR'):"
    docker-compose logs user-service 2>/dev/null | grep -i "eureka\|error\|started\|registration" | tail -5 || echo "No logs found"
    
    # Check 10: API Test
    print_header "10. API Endpoint Tests"
    
    # Test health endpoint
    if curl -s -f "$USER_SERVICE_URL/actuator/health" > /dev/null 2>&1; then
        print_success "/actuator/health endpoint is working"
    else
        print_error "/actuator/health endpoint is NOT working"
        all_passed=false
    fi
    
    # Test registration endpoint (no auth required)
    if curl -s "$USER_SERVICE_URL/api/auth" > /dev/null 2>&1; then
        print_success "API base path is accessible"
    else
        print_warning "API base path returned an error (this may be expected)"
    fi
    
    # Summary
    print_header "Summary"
    
    if [ "$all_passed" = true ]; then
        echo -e "${GREEN}╔════════════════════════════════════════════════════╗${NC}"
        echo -e "${GREEN}║          ✓ All Checks PASSED!                     ║${NC}"
        echo -e "${GREEN}║   Eureka and User Service are working correctly   ║${NC}"
        echo -e "${GREEN}╚════════════════════════════════════════════════════╝${NC}"
        return 0
    else
        echo -e "${RED}╔════════════════════════════════════════════════════╗${NC}"
        echo -e "${RED}║          ✗ Some Checks FAILED!                    ║${NC}"
        echo -e "${RED}║    Please review the errors above and fix them    ║${NC}"
        echo -e "${RED}╚════════════════════════════════════════════════════╝${NC}"
        return 1
    fi
}

# Run main function
main
exit $?
