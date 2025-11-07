#!/bin/bash

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "========================================"
echo "Keycloak Configuration Fix"
echo "========================================"
echo ""

# Wait for Keycloak to be ready
echo "Checking if Keycloak is running..."
for i in {1..10}; do
    if curl -s http://localhost:9090 > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Keycloak is running${NC}"
        break
    fi
    if [ $i -eq 10 ]; then
        echo -e "${RED}✗ Keycloak not responding${NC}"
        echo "Please start it: docker start backend-2-keycloak-1"
        exit 1
    fi
    sleep 2
done

echo ""
echo "========================================"
echo "The issue: Keycloak requires HTTPS"
echo "========================================"
echo ""
echo "Your backend-2-keycloak-1 container was started without HTTP enabled."
echo "Here are your options:"
echo ""
echo "Option 1: Configure via Keycloak Admin UI (Manual)"
echo "  1. Go to: http://localhost:9090"
echo "  2. Login: admin / admin"
echo "  3. Select 'master' realm"
echo "  4. Go to: Realm Settings → General"
echo "  5. Set 'Require SSL' to: none"
echo "  6. Click Save"
echo "  7. Repeat for 'event-ticket-platform' realm (create it first if needed)"
echo ""
echo "Option 2: Use Keycloak CLI (Automated)"
echo "  Run these commands:"
echo "  docker exec backend-2-keycloak-1 /opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user admin --password admin"
echo "  docker exec backend-2-keycloak-1 /opt/keycloak/bin/kcadm.sh update realms/master -s sslRequired=NONE"
echo ""
echo "Option 3: Recreate container with proper config (Recommended)"
echo "  cd backend && docker compose down && docker compose up -d"
echo ""
echo -n "Would you like to try Option 2 (automated fix)? (y/n): "
read -r response

if [[ "$response" =~ ^[Yy]$ ]]; then
    echo ""
    echo "Attempting automated fix..."
    
    # Configure kcadm
    echo "Configuring Keycloak CLI..."
    docker exec backend-2-keycloak-1 /opt/keycloak/bin/kcadm.sh config credentials \
        --server http://localhost:8080 \
        --realm master \
        --user admin \
        --password admin 2>&1
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ CLI configured${NC}"
        
        # Disable SSL for master realm
        echo "Disabling SSL requirement for master realm..."
        docker exec backend-2-keycloak-1 /opt/keycloak/bin/kcadm.sh update realms/master \
            -s sslRequired=NONE 2>&1
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ Master realm updated${NC}"
        else
            echo -e "${RED}✗ Failed to update master realm${NC}"
        fi
        
        # Check if event-ticket-platform realm exists
        echo "Checking for event-ticket-platform realm..."
        docker exec backend-2-keycloak-1 /opt/keycloak/bin/kcadm.sh get realms/event-ticket-platform 2>&1 > /dev/null
        
        if [ $? -eq 0 ]; then
            echo "Updating event-ticket-platform realm..."
            docker exec backend-2-keycloak-1 /opt/keycloak/bin/kcadm.sh update realms/event-ticket-platform \
                -s sslRequired=NONE 2>&1
            echo -e "${GREEN}✓ event-ticket-platform realm updated${NC}"
        else
            echo -e "${YELLOW}⚠ event-ticket-platform realm doesn't exist yet${NC}"
            echo "You'll need to create it manually in the Keycloak admin UI"
        fi
        
        echo ""
        echo -e "${GREEN}========================================"
        echo "Configuration updated!"
        echo "========================================${NC}"
        echo ""
        echo "Now test admin authentication:"
        echo ""
        
        # Test admin auth
        RESULT=$(curl -s -X POST "http://localhost:9090/realms/master/protocol/openid-connect/token" \
            -H "Content-Type: application/x-www-form-urlencoded" \
            -d "grant_type=password&client_id=admin-cli&username=admin&password=admin")
        
        if echo "$RESULT" | grep -q "access_token"; then
            echo -e "${GREEN}✓ Admin authentication works!${NC}"
            echo ""
            echo "Next steps:"
            echo "1. Open Keycloak admin: http://localhost:9090"
            echo "2. Create 'event-ticket-platform' realm"
            echo "3. Follow the rest of KEYCLOAK_SETUP.md"
        else
            echo -e "${RED}✗ Authentication still not working${NC}"
            echo "Response: $RESULT"
            echo ""
            echo "You may need to restart Keycloak:"
            echo "docker restart backend-2-keycloak-1"
        fi
    else
        echo -e "${RED}✗ Failed to configure CLI${NC}"
        echo "Try Option 1 (manual configuration) instead"
    fi
else
    echo "Skipped automated fix. Please follow one of the manual options above."
fi
