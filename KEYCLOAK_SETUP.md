# Keycloak Setup Guide for Event Ticket Platform

## Problem Identified

Your login isn't working because:

1. **Keycloak requires HTTPS by default** but your app uses HTTP (localhost development)
2. **The `event-ticket-platform` realm doesn't exist** in Keycloak
3. **Google and GitHub OAuth providers aren't configured** in Keycloak

## Solution Steps

### Step 1: Update Keycloak Container to Allow HTTP

The backend-2-keycloak-1 container needs to be recreated with proper HTTP settings.

**Option A: Recreate from scratch (Recommended - Clean Start)**

```bash
# Stop and remove the old container
docker stop backend-2-keycloak-1 backend-2-db-1 backend-2-adminer-1
docker rm backend-2-keycloak-1 backend-2-db-1 backend-2-adminer-1

# Go to backend directory
cd backend

# Start with the updated docker-compose.yml
docker compose up -d
```

**Option B: Exec into container and configure (Temporary fix)**

This won't persist after container restart but useful for testing:

```bash
docker exec -it backend-2-keycloak-1 /opt/keycloak/bin/kc.sh config credentials --server http://localhost:8080 --realm master --user admin --password admin
```

### Step 2: Access Keycloak Admin Console

1. Open: http://localhost:9090
2. Click "Administration Console"
3. Login with:
   - Username: `admin`
   - Password: `admin`

### Step 3: Create the `event-ticket-platform` Realm

1. In the top-left corner, click the dropdown that says "master"
2. Click "Create Realm"
3. Enter:
   - **Realm name**: `event-ticket-platform`
4. Click "Create"

### Step 4: Create the Client Application

1. In the left sidebar, click "Clients"
2. Click "Create client"
3. Enter:
   - **Client type**: OpenID Connect
   - **Client ID**: `event-ticket-platform-app`
4. Click "Next"
5. Enable:
   - ✅ Client authentication: OFF (public client)
   - ✅ Authorization: OFF
   - ✅ Standard flow: ON
   - ✅ Direct access grants: ON
6. Click "Next"
7. Enter:
   - **Root URL**: `http://localhost:5173`
   - **Valid redirect URIs**: `http://localhost:5173/*`
   - **Web origins**: `http://localhost:5173`
8. Click "Save"

### Step 5: Create Another Client for Direct Login (Backend)

1. Click "Clients" → "Create client"
2. Enter:
   - **Client ID**: `event-ticket-client`
3. Click "Next"
4. Enable:
   - ✅ Client authentication: ON
   - ✅ Authorization: OFF
   - ✅ Standard flow: OFF
   - ✅ Direct access grants: ON
   - ✅ Service accounts roles: ON
5. Click "Save"
6. Go to the "Credentials" tab
7. Copy the **Client secret** value
8. Update `backend/src/main/resources/application.properties`:
   ```properties
   keycloak.client.secret=<paste-the-secret-here>
   ```

### Step 6: Create Realm Roles

1. In the left sidebar, click "Realm roles"
2. Click "Create role"
3. Create these roles one by one:
   - Name: `ROLE_ATTENDEE` → Save
   - Name: `ROLE_ORGANIZER` → Save
   - Name: `ROLE_STAFF` → Save

### Step 7: Configure Google OAuth

1. **Get Google OAuth Credentials:**

   - Go to: https://console.cloud.google.com/
   - Create a new project or select existing
   - Go to "APIs & Services" → "Credentials"
   - Click "Create Credentials" → "OAuth 2.0 Client ID"
   - Application type: "Web application"
   - Authorized redirect URIs: `http://localhost:9090/realms/event-ticket-platform/broker/google/endpoint`
   - Copy the **Client ID** and **Client Secret**

2. **Add Google Identity Provider in Keycloak:**
   - In Keycloak admin (event-ticket-platform realm)
   - Click "Identity providers" in left sidebar
   - Click "Google"
   - Enter:
     - **Alias**: `google`
     - **Client ID**: (paste from Google Console)
     - **Client Secret**: (paste from Google Console)
   - Click "Save"

### Step 8: Configure GitHub OAuth

1. **Get GitHub OAuth Credentials:**

   - Go to: https://github.com/settings/developers
   - Click "New OAuth App"
   - Application name: `Event Ticket Platform Dev`
   - Homepage URL: `http://localhost:5173`
   - Authorization callback URL: `http://localhost:9090/realms/event-ticket-platform/broker/github/endpoint`
   - Copy the **Client ID**
   - Generate and copy the **Client Secret**

2. **Add GitHub Identity Provider in Keycloak:**
   - In Keycloak admin (event-ticket-platform realm)
   - Click "Identity providers" in left sidebar
   - Click "GitHub"
   - Enter:
     - **Alias**: `github`
     - **Client ID**: (paste from GitHub)
     - **Client Secret**: (paste from GitHub)
   - Click "Save"

### Step 9: Test the Setup

1. **Test Admin Access:**

   ```bash
   curl -X POST "http://localhost:9090/realms/master/protocol/openid-connect/token" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "grant_type=password&client_id=admin-cli&username=admin&password=admin"
   ```

   Should return JSON with `access_token`

2. **Register a Test User:**

   ```bash
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "username": "testuser",
       "email": "test@example.com",
       "password": "testpass123",
       "name": "Test User",
       "role": "ATTENDEE"
     }'
   ```

3. **Test Login:**

   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "username": "testuser",
       "password": "testpass123"
     }'
   ```

   Should return JSON with `access_token`

4. **Test OAuth in Browser:**
   - Go to: http://localhost:5173/login
   - Click "Google" or "GitHub" button
   - Should redirect to Keycloak → OAuth provider → back to your app

## Common Issues

### Issue: "HTTPS required"

**Solution:** Restart Keycloak with HTTP enabled (see Step 1)

### Issue: "Realm does not exist"

**Solution:** Create the `event-ticket-platform` realm (see Step 3)

### Issue: "Client not found"

**Solution:** Create the clients (see Steps 4 & 5)

### Issue: "Invalid redirect_uri"

**Solution:** Make sure the redirect URIs in Keycloak match your frontend URL exactly

### Issue: OAuth buttons don't work

**Solution:**

- Check that identity providers are configured in Keycloak
- Verify the `kc_idp_hint` values match the provider aliases (`google`, `github`)
- Check browser console for errors

### Issue: "Failed to authenticate with Keycloak admin"

**Solution:**

- Make sure Keycloak is fully started (wait 20-30 seconds after `docker compose up`)
- Verify admin credentials are `admin`/`admin`
- Check Keycloak logs: `docker logs backend-2-keycloak-1`

## Quick Verification Script

Run this to check if everything is set up:

```bash
#!/bin/bash
echo "Checking Keycloak..."
curl -s http://localhost:9090 > /dev/null && echo "✓ Keycloak running" || echo "✗ Keycloak not running"

echo "Checking if realm exists..."
TOKEN=$(curl -s -X POST "http://localhost:9090/realms/master/protocol/openid-connect/token" \
  -d "grant_type=password&client_id=admin-cli&username=admin&password=admin" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)

if [ -n "$TOKEN" ]; then
  echo "✓ Admin auth works"
  curl -s -H "Authorization: Bearer $TOKEN" \
    "http://localhost:9090/admin/realms/event-ticket-platform" > /dev/null && \
    echo "✓ Realm exists" || echo "✗ Realm doesn't exist - create it!"
else
  echo "✗ Admin auth failed - HTTP not enabled?"
fi
```

## Next Steps After Setup

Once Keycloak is properly configured:

1. Restart your backend Spring Boot application
2. Go to http://localhost:5173/signup to create an account
3. Go to http://localhost:5173/login to test:
   - Username/password login
   - Google OAuth login
   - GitHub OAuth login

All three methods should work!
