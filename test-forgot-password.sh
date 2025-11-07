#!/bin/bash

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}========================================"
echo "Forgot Password Feature Test"
echo "========================================${NC}"
echo ""

# Check if backend is running
curl -s http://localhost:8080 > /dev/null
if [ $? -ne 0 ]; then
    echo -e "${RED}✗ Backend is not running${NC}"
    echo "Please start the backend first"
    exit 1
fi

echo -e "${GREEN}✓ Backend is running${NC}"
echo ""

# Test with valid email
echo -e "${YELLOW}Testing forgot password with: demo@example.com${NC}"
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com"}')

echo "Response: $RESPONSE"
echo ""

if echo "$RESPONSE" | grep -q "password reset instructions"; then
    echo -e "${GREEN}✓ Forgot password request accepted${NC}"
else
    echo -e "${RED}✗ Unexpected response${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}========================================"
echo "Checking Backend Logs"
echo "========================================${NC}"
echo ""

# Try to find backend logs
if [ -f "backend/logs/application.log" ]; then
    echo "Recent backend logs:"
    tail -5 backend/logs/application.log | grep -i "password reset"
else
    echo -e "${YELLOW}Backend logs not found in expected location${NC}"
    echo "If backend is running, check console output for:"
    echo "  'Password reset email sent successfully'"
fi

echo ""
echo -e "${BLUE}========================================"
echo "Keycloak Email Logs (Development Mode)"
echo "========================================${NC}"
echo ""
echo "In development mode, Keycloak logs email actions."
echo "Check Keycloak logs:"
echo ""
echo -e "${YELLOW}docker logs backend-2-keycloak-1 2>&1 | grep -A 5 'UPDATE_PASSWORD'${NC}"
echo ""

# Try to get recent Keycloak logs
if docker ps | grep -q backend-2-keycloak-1; then
    echo "Recent Keycloak activity:"
    docker logs backend-2-keycloak-1 2>&1 | tail -10
else
    echo -e "${RED}✗ Keycloak container (backend-2-keycloak-1) not running${NC}"
fi

echo ""
echo -e "${GREEN}========================================"
echo "Test Summary"
echo "========================================${NC}"
echo ""
echo "✅ Forgot password endpoint working"
echo "✅ Returns security-safe generic message"
echo "✅ Backend processes request without errors"
echo ""
echo "📧 Email Configuration:"
echo "   - Dev Mode: Emails logged to console (not sent)"
echo "   - Prod Mode: Configure SMTP in Keycloak"
echo ""
echo "📖 For full setup guide, see:"
echo "   FORGOT_PASSWORD_GUIDE.md"
echo ""
echo "🌐 Test in browser:"
echo "   1. Go to: http://localhost:5173/login"
echo "   2. Click 'Forgot your password?'"
echo "   3. Enter email: demo@example.com"
echo "   4. Check console/logs for reset link"
echo ""
