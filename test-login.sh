#!/bin/bash

# Color codes for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "========================================"
echo "Booking Site - Login Test Script"
echo "========================================"
echo ""

# Check if backend is running
echo -n "Checking backend... "
BACKEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/auth/login 2>&1)
if [[ "$BACKEND_STATUS" == "401" ]] || [[ "$BACKEND_STATUS" == "400" ]]; then
    echo -e "${GREEN}✓ Running${NC}"
else
    echo -e "${RED}✗ Not running or not accessible${NC}"
    echo "  Please start the backend: cd backend && ./mvnw spring-boot:run"
    exit 1
fi

# Check if Keycloak is running
echo -n "Checking Keycloak... "
KEYCLOAK_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:9090 2>&1)
if [[ "$KEYCLOAK_STATUS" == "302" ]] || [[ "$KEYCLOAK_STATUS" == "200" ]]; then
    echo -e "${GREEN}✓ Running${NC}"
else
    echo -e "${RED}✗ Not running${NC}"
    echo "  Please start Docker services: cd backend && docker-compose up -d"
    exit 1
fi

# Check if PostgreSQL is running
echo -n "Checking PostgreSQL... "
if docker ps | grep -q "backend-2-db-1"; then
    echo -e "${GREEN}✓ Running${NC}"
else
    echo -e "${RED}✗ Not running${NC}"
    echo "  Please start Docker services: cd backend && docker-compose up -d"
    exit 1
fi

# Check if frontend is running
echo -n "Checking frontend... "
FRONTEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:5173 2>&1)
if [[ "$FRONTEND_STATUS" == "200" ]]; then
    echo -e "${GREEN}✓ Running${NC}"
else
    echo -e "${YELLOW}⚠ Not running${NC}"
    echo "  Start frontend: cd frontend && npm run dev"
fi

echo ""
echo "========================================"
echo "All services are ready!"
echo "========================================"
echo ""

# Prompt for test account creation
echo "Would you like to create a test account? (y/n)"
read -r response

if [[ "$response" =~ ^[Yy]$ ]]; then
    echo ""
    echo "Creating test account..."
    
    # Generate a random suffix to avoid conflicts
    RANDOM_SUFFIX=$((1000 + RANDOM % 9000))
    USERNAME="testuser${RANDOM_SUFFIX}"
    EMAIL="testuser${RANDOM_SUFFIX}@example.com"
    PASSWORD="testpass123"
    
    REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
      -H "Content-Type: application/json" \
      -d "{
        \"username\": \"${USERNAME}\",
        \"email\": \"${EMAIL}\",
        \"password\": \"${PASSWORD}\",
        \"name\": \"Test User ${RANDOM_SUFFIX}\",
        \"role\": \"ATTENDEE\"
      }")
    
    if echo "$REGISTER_RESPONSE" | grep -q "registered successfully"; then
        echo -e "${GREEN}✓ Account created successfully!${NC}"
        echo ""
        echo "========================================"
        echo "Test Credentials:"
        echo "========================================"
        echo "Username: ${USERNAME}"
        echo "Password: ${PASSWORD}"
        echo "========================================"
        echo ""
        
        # Try to login with the new account
        echo "Testing login..."
        LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
          -H "Content-Type: application/json" \
          -d "{
            \"username\": \"${USERNAME}\",
            \"password\": \"${PASSWORD}\"
          }")
        
        if echo "$LOGIN_RESPONSE" | grep -q "access_token"; then
            echo -e "${GREEN}✓ Login successful!${NC}"
            echo ""
            echo "You can now login at: http://localhost:5173/login"
            echo "Use the credentials above."
        else
            echo -e "${RED}✗ Login failed${NC}"
            echo "Response: $LOGIN_RESPONSE"
        fi
    else
        echo -e "${RED}✗ Account creation failed${NC}"
        echo "Response: $REGISTER_RESPONSE"
        echo ""
        echo "This might be because:"
        echo "  - Keycloak is not fully initialized yet (wait 30 seconds and try again)"
        echo "  - Database connection issue"
        echo "  - A user with similar details already exists"
    fi
fi

echo ""
echo "Done!"
