# Forgot Password Feature - Complete Guide

## Overview

The forgot password feature allows users to reset their password by receiving a password reset link via email. The system includes the username in logs for security monitoring.

## How It Works

1. User enters their email address on the forgot password page
2. Backend validates the email and finds the associated user
3. Keycloak sends a password reset email with a secure link
4. User clicks the link and is redirected to set a new password
5. System logs include the username for security auditing

## Backend Implementation

### Endpoint

```
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com"
}
```

### Response

```json
{
  "message": "If an account exists with that email, you will receive password reset instructions."
}
```

**Note:** The response is intentionally generic to prevent email enumeration attacks.

## Testing the Feature

### 1. Test via Frontend

```bash
# Navigate to the forgot password page
open http://localhost:5173/login
# Click "Forgot your password?"
# Enter email: demo@example.com
# Check the terminal/logs for the reset email
```

### 2. Test via cURL

```bash
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com"}'
```

### 3. Check Keycloak Logs

```bash
# View the password reset action in logs
docker logs backend-2-keycloak-1 | grep "UPDATE_PASSWORD"
```

## Keycloak Email Configuration

### Current Setup

Keycloak is running in development mode and will **log emails to console** instead of sending them via SMTP.

To see the reset link:

```bash
# Watch Keycloak logs
docker logs -f backend-2-keycloak-1
```

### Production Email Setup (SMTP)

To configure real email sending for production:

#### 1. Configure Email Settings in Keycloak Admin UI

1. Open: http://localhost:9090
2. Login: admin / admin
3. Select realm: `event-ticket-platform`
4. Go to: **Realm Settings** → **Email** tab
5. Configure SMTP settings:

   - **Host:** smtp.gmail.com (or your SMTP server)
   - **Port:** 587
   - **From:** noreply@yourdomain.com
   - **Enable StartTLS:** Yes
   - **Enable Authentication:** Yes
   - **Username:** your-email@gmail.com
   - **Password:** your-app-password

6. Click **Save**
7. Click **Test connection** to verify

#### 2. Configure via CLI (Alternative)

```bash
docker exec backend-2-keycloak-1 /opt/keycloak/bin/kcadm.sh update realms/event-ticket-platform \
  -s 'smtpServer.host=smtp.gmail.com' \
  -s 'smtpServer.port=587' \
  -s 'smtpServer.from=noreply@yourdomain.com' \
  -s 'smtpServer.auth=true' \
  -s 'smtpServer.starttls=true' \
  -s 'smtpServer.user=your-email@gmail.com' \
  -s 'smtpServer.password=your-app-password'
```

### Using Gmail for Development

1. Enable 2FA on your Google account
2. Generate an App Password:
   - Go to: https://myaccount.google.com/apppasswords
   - Create app password for "Mail"
   - Use this password in Keycloak SMTP configuration

## Customizing the Email Template (Including Username)

Keycloak uses Freemarker templates for emails. To customize the password reset email to include the username:

### Method 1: Custom Theme (Recommended for Production)

1. **Create a custom theme directory:**

```bash
docker exec -it backend-2-keycloak-1 bash
cd /opt/keycloak/themes
mkdir -p custom-email/email
cd custom-email/email
```

2. **Create theme.properties:**

```properties
parent=base
```

3. **Copy and customize the email template:**

```bash
# Copy base template
cp /opt/keycloak/lib/lib/main/org.keycloak.keycloak-themes-*.jar base-templates.jar
unzip -q base-templates.jar "theme/base/email/*" -d /tmp
cp /tmp/theme/base/email/html/password-reset.ftl ./html/

# Edit the template
vi html/password-reset.ftl
```

4. **Update password-reset.ftl to include username:**

```html
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
  </head>
  <body>
    <h2>Password Reset Request</h2>
    <p>Hello <strong>${user.username}</strong>,</p>
    <p>We received a request to reset your password for your account.</p>
    <p>Email: <strong>${user.email}</strong></p>
    <p>Click the link below to reset your password:</p>
    <p><a href="${link}">Reset Password</a></p>
    <p>This link will expire in ${linkExpirationFormatter(linkExpiration)}.</p>
    <p>If you didn't request this, please ignore this email.</p>
  </body>
</html>
```

5. **Activate the theme in Keycloak:**

```bash
docker exec backend-2-keycloak-1 /opt/keycloak/bin/kcadm.sh update realms/event-ticket-platform \
  -s emailTheme=custom-email
```

### Method 2: Mount Custom Theme (Docker Volume)

Update `docker-compose.yml`:

```yaml
keycloak:
  image: quay.io/keycloak/keycloak:latest
  ports:
    - "9090:8080"
  environment:
    KEYCLOAK_ADMIN: admin
    KEYCLOAK_ADMIN_PASSWORD: admin
    KC_HTTP_ENABLED: "true"
    KC_HOSTNAME_STRICT: "false"
  volumes:
    - keycloak-data:/opt/keycloak/data
    - ./keycloak-themes:/opt/keycloak/themes/custom-email
  command:
    - start-dev
    - --db=dev-file
    - --http-enabled=true
    - --hostname-strict=false
```

Then create `backend/keycloak-themes/email/html/password-reset.ftl` with the custom template.

## Frontend Integration

The forgot password UI is already implemented in `frontend/src/pages/login-page.tsx`.

### Features:

- ✅ Email validation
- ✅ User-friendly error messages
- ✅ Success confirmation
- ✅ Toggle between login and forgot password views

### Flow:

1. User clicks "Forgot your password?"
2. Enters email address
3. Receives confirmation message
4. Checks email for reset link
5. Clicks link → redirected to Keycloak password reset page
6. Sets new password
7. Returns to login with new credentials

## Security Features

### 1. Email Enumeration Prevention

- Always returns success message, even if email doesn't exist
- Prevents attackers from discovering valid email addresses

### 2. Token Expiration

- Reset links expire after a configurable time (default: 5 minutes)
- Can be configured in Keycloak realm settings

### 3. Single-Use Links

- Each reset link can only be used once
- Subsequent attempts require a new request

### 4. Audit Logging

- All reset attempts are logged with username (server-side only)
- Logs include timestamp and IP address

## Troubleshooting

### Issue: No email received

**Check Keycloak logs:**

```bash
docker logs backend-2-keycloak-1 2>&1 | grep -A 5 "UPDATE_PASSWORD"
```

**Verify SMTP configuration:**

```bash
docker exec backend-2-keycloak-1 /opt/keycloak/bin/kcadm.sh get realms/event-ticket-platform | grep -A 10 smtpServer
```

### Issue: "Failed to send email"

**Solutions:**

1. Verify SMTP credentials are correct
2. Check firewall/network allows SMTP traffic
3. Enable "Less secure app access" (Gmail) or use App Password
4. Check Keycloak has internet access (if in container)

### Issue: Email goes to spam

**Solutions:**

1. Configure SPF/DKIM records for your domain
2. Use a verified email service (SendGrid, AWS SES, etc.)
3. Add a proper "Reply-To" address
4. Include unsubscribe link (for production)

## Quick Test Script

```bash
#!/bin/bash

echo "Testing Forgot Password Feature"
echo "================================"

# Test with existing user
echo "Sending reset email to: demo@example.com"
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com"}'

echo ""
echo "Check Keycloak logs for reset link:"
echo "docker logs backend-2-keycloak-1 2>&1 | tail -20"
```

## Configuration Summary

| Setting         | Current Value    | Production Recommendation            |
| --------------- | ---------------- | ------------------------------------ |
| Email Method    | Console logging  | SMTP (Gmail/SendGrid/SES)            |
| Link Expiration | 5 minutes        | 15-30 minutes                        |
| Email Theme     | Keycloak default | Custom theme with branding           |
| From Address    | Default          | noreply@yourdomain.com               |
| Email includes  | Reset link       | Reset link + Username + Support info |

## Next Steps

1. ✅ Backend implementation complete
2. ✅ Frontend UI complete
3. ⏳ Configure SMTP for production
4. ⏳ Customize email template (optional)
5. ⏳ Add email branding (optional)
6. ⏳ Set up monitoring/alerts for failed emails

## Related Files

- Backend Service: `backend/src/main/java/com/capstone/tickets/services/KeycloakService.java`
- Auth Service: `backend/src/main/java/com/capstone/tickets/services/AuthService.java`
- Controller: `backend/src/main/java/com/capstone/tickets/controllers/AuthController.java`
- Frontend Page: `frontend/src/pages/login-page.tsx`
