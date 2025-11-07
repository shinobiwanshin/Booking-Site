#!/bin/bash

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗"
echo "║          Keycloak SMTP Configuration Setup             ║"
echo "╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Check if Keycloak is running
docker ps | grep -q backend-2-keycloak-1
if [ $? -ne 0 ]; then
    echo -e "${RED}✗ Keycloak container is not running${NC}"
    echo "Please start it: docker start backend-2-keycloak-1"
    exit 1
fi

echo -e "${GREEN}✓ Keycloak is running${NC}"
echo ""

echo -e "${YELLOW}Choose your email provider:${NC}"
echo ""
echo "1) Gmail (Recommended for testing)"
echo "2) Outlook/Hotmail"
echo "3) Custom SMTP Server"
echo "4) SendGrid"
echo "5) AWS SES"
echo ""
read -p "Enter choice (1-5): " PROVIDER_CHOICE

case $PROVIDER_CHOICE in
    1)
        PROVIDER="Gmail"
        SMTP_HOST="smtp.gmail.com"
        SMTP_PORT="587"
        SMTP_STARTTLS="true"
        SMTP_AUTH="true"
        SMTP_SSL="false"
        
        echo ""
        echo -e "${YELLOW}Gmail Setup Instructions:${NC}"
        echo "1. Go to: https://myaccount.google.com/apppasswords"
        echo "2. Sign in to your Google account"
        echo "3. Enable 2-Factor Authentication (if not already enabled)"
        echo "4. Create a new App Password:"
        echo "   - Select 'Mail' as the app"
        echo "   - Select 'Other' as the device and name it 'Booking Site'"
        echo "5. Copy the 16-character password (no spaces)"
        echo ""
        read -p "Enter your Gmail address: " SMTP_USER
        read -sp "Enter your App Password: " SMTP_PASSWORD
        echo ""
        SMTP_FROM="${SMTP_USER}"
        ;;
    2)
        PROVIDER="Outlook"
        SMTP_HOST="smtp-mail.outlook.com"
        SMTP_PORT="587"
        SMTP_STARTTLS="true"
        SMTP_AUTH="true"
        SMTP_SSL="false"
        
        echo ""
        read -p "Enter your Outlook email: " SMTP_USER
        read -sp "Enter your Outlook password: " SMTP_PASSWORD
        echo ""
        SMTP_FROM="${SMTP_USER}"
        ;;
    3)
        PROVIDER="Custom SMTP"
        echo ""
        read -p "Enter SMTP host (e.g., smtp.yourdomain.com): " SMTP_HOST
        read -p "Enter SMTP port (usually 587 or 465): " SMTP_PORT
        read -p "Enable STARTTLS? (true/false): " SMTP_STARTTLS
        read -p "Enable SSL? (true/false): " SMTP_SSL
        read -p "Requires authentication? (true/false): " SMTP_AUTH
        read -p "Enter SMTP username: " SMTP_USER
        read -sp "Enter SMTP password: " SMTP_PASSWORD
        echo ""
        read -p "Enter 'From' email address: " SMTP_FROM
        ;;
    4)
        PROVIDER="SendGrid"
        SMTP_HOST="smtp.sendgrid.net"
        SMTP_PORT="587"
        SMTP_STARTTLS="true"
        SMTP_AUTH="true"
        SMTP_SSL="false"
        
        echo ""
        echo -e "${YELLOW}SendGrid Setup Instructions:${NC}"
        echo "1. Sign up at: https://sendgrid.com/"
        echo "2. Create an API Key in Settings → API Keys"
        echo "3. Use 'apikey' as username and your API key as password"
        echo ""
        SMTP_USER="apikey"
        read -sp "Enter your SendGrid API Key: " SMTP_PASSWORD
        echo ""
        read -p "Enter 'From' email (verified in SendGrid): " SMTP_FROM
        ;;
    5)
        PROVIDER="AWS SES"
        echo ""
        echo -e "${YELLOW}AWS SES Setup Instructions:${NC}"
        echo "1. Sign in to AWS Console"
        echo "2. Go to Amazon SES"
        echo "3. Create SMTP Credentials"
        echo "4. Verify your sending email address"
        echo ""
        read -p "Enter SES SMTP endpoint (e.g., email-smtp.us-east-1.amazonaws.com): " SMTP_HOST
        SMTP_PORT="587"
        SMTP_STARTTLS="true"
        SMTP_AUTH="true"
        SMTP_SSL="false"
        read -p "Enter SMTP username (from SES credentials): " SMTP_USER
        read -sp "Enter SMTP password (from SES credentials): " SMTP_PASSWORD
        echo ""
        read -p "Enter verified 'From' email address: " SMTP_FROM
        ;;
    *)
        echo -e "${RED}Invalid choice${NC}"
        exit 1
        ;;
esac

echo ""
echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}Configuring Keycloak SMTP...${NC}"
echo ""

# Build the configuration command
CMD="docker exec backend-2-keycloak-1 /opt/keycloak/bin/kcadm.sh update realms/event-ticket-platform"
CMD="$CMD -s 'smtpServer.host=${SMTP_HOST}'"
CMD="$CMD -s 'smtpServer.port=${SMTP_PORT}'"
CMD="$CMD -s 'smtpServer.from=${SMTP_FROM}'"
CMD="$CMD -s 'smtpServer.auth=${SMTP_AUTH}'"
CMD="$CMD -s 'smtpServer.starttls=${SMTP_STARTTLS}'"
CMD="$CMD -s 'smtpServer.ssl=${SMTP_SSL}'"

if [ "$SMTP_AUTH" = "true" ]; then
    CMD="$CMD -s 'smtpServer.user=${SMTP_USER}'"
    CMD="$CMD -s 'smtpServer.password=${SMTP_PASSWORD}'"
fi

# Execute the command
eval $CMD 2>&1

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ SMTP configuration saved${NC}"
else
    echo -e "${RED}✗ Failed to save SMTP configuration${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}Testing email connection...${NC}"
echo ""

# Get a test email address
read -p "Enter email address to send test email to: " TEST_EMAIL

# Find or create a test user
docker exec backend-2-keycloak-1 /opt/keycloak/bin/kcadm.sh get users -r event-ticket-platform -q email=${TEST_EMAIL} 2>&1 > /tmp/keycloak_test_user.json

if grep -q "\"email\" : \"${TEST_EMAIL}\"" /tmp/keycloak_test_user.json; then
    echo -e "${GREEN}✓ User found${NC}"
else
    echo -e "${YELLOW}Creating test user...${NC}"
    
    # Create a test user
    docker exec backend-2-keycloak-1 /opt/keycloak/bin/kcadm.sh create users -r event-ticket-platform \
        -s username="emailtest_$(date +%s)" \
        -s email="${TEST_EMAIL}" \
        -s emailVerified=true \
        -s enabled=true 2>&1
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Test user created${NC}"
    fi
fi

# Send test email
echo ""
echo "Sending test email..."

USER_ID=$(docker exec backend-2-keycloak-1 /opt/keycloak/bin/kcadm.sh get users -r event-ticket-platform -q email=${TEST_EMAIL} --fields id 2>&1 | grep -o '"id" : "[^"]*' | cut -d'"' -f4)

if [ -n "$USER_ID" ]; then
    docker exec backend-2-keycloak-1 /opt/keycloak/bin/kcadm.sh update users/${USER_ID}/execute-actions-email -r event-ticket-platform \
        -b '[\"UPDATE_PASSWORD\"]' 2>&1
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Test email sent!${NC}"
        echo ""
        echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
        echo -e "${GREEN}✅ SMTP Configuration Complete!${NC}"
        echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
        echo ""
        echo "Check your inbox: ${TEST_EMAIL}"
        echo ""
        echo "If you don't receive the email within 2-3 minutes:"
        echo "1. Check your spam/junk folder"
        echo "2. Verify your SMTP credentials are correct"
        echo "3. Check Keycloak logs: docker logs backend-2-keycloak-1"
        echo ""
    else
        echo -e "${RED}✗ Failed to send test email${NC}"
        echo ""
        echo "Troubleshooting:"
        echo "1. Check Keycloak logs: docker logs backend-2-keycloak-1"
        echo "2. Verify SMTP credentials"
        echo "3. Check firewall/network settings"
    fi
else
    echo -e "${RED}✗ Could not find user${NC}"
fi

echo ""
echo -e "${YELLOW}To test forgot password with your app:${NC}"
echo "1. Go to: http://localhost:5173/login"
echo "2. Click 'Forgot your password?'"
echo "3. Enter: ${TEST_EMAIL}"
echo "4. Check your email for the reset link"
echo ""
