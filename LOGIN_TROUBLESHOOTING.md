# Login Troubleshooting Guide

## Common Login Issues and Solutions

### Issue 1: "Invalid username or password"

**Cause:** You haven't registered a user yet, or the credentials are incorrect.

**Solution:**

1. Go to the signup page: http://localhost:5173/signup
2. Register a new account with:
   - Username (min 3 characters)
   - Email (valid format)
   - Password (min 8 characters)
   - Name
   - Role (ATTENDEE or ORGANIZER)
3. After registration, you'll be redirected to login
4. Use the same username and password to login

### Issue 2: "Unable to connect to server"

**Cause:** Backend server is not running or not accessible on port 8080.

**Solution:**

```bash
# Start the backend server
cd backend
./mvnw spring-boot:run

# Or if on Windows:
mvnw.cmd spring-boot:run
```

### Issue 3: Backend starts but login still fails

**Cause:** Required services (PostgreSQL or Keycloak) are not running.

**Check services:**

```bash
# Check if Docker containers are running
docker ps

# You should see these containers:
# - backend-2-db-1 (PostgreSQL on port 5433)
# - backend-2-keycloak-1 (Keycloak on port 9090)
# - backend-2-adminer-1 (Adminer on port 8889)
```

**Start services:**

```bash
cd backend
docker-compose up -d
```

### Issue 4: Frontend not loading or on wrong port

**Cause:** Frontend dev server is not running or on wrong port.

**Solution:**

```bash
# Start the frontend
cd frontend
npm install  # First time only
npm run dev

# Frontend should start on http://localhost:5173
```

### Issue 5: CORS errors in browser console

**Cause:** Frontend is running on a different port than expected.

**Solution:**
The backend expects frontend on `http://localhost:5173`. If you're using a different port, update `backend/src/main/java/com/capstone/tickets/config/AppConfig.java`:

```java
.allowedOrigins("http://localhost:5173")  // Change this if needed
```

## Complete Setup Checklist

Before attempting to login, ensure:

- [ ] Docker containers are running (PostgreSQL + Keycloak)

  ```bash
  cd backend
  docker-compose up -d
  ```

- [ ] Backend server is running on port 8080

  ```bash
  cd backend
  ./mvnw spring-boot:run
  ```

- [ ] Frontend dev server is running on port 5173

  ```bash
  cd frontend
  npm run dev
  ```

- [ ] You have registered a user account at http://localhost:5173/signup

## Quick Test

To verify your setup is working:

1. Check backend health:

   ```bash
   curl http://localhost:8080/api/auth/login
   # Should return: {"message":"Invalid username or password"}
   # This means the endpoint is accessible
   ```

2. Register a test user:

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

3. Login with test user:
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "username": "testuser",
       "password": "testpass123"
     }'
   # Should return JSON with access_token and user info
   ```

## Debug Mode

The auth context now includes console logging. Open your browser's DevTools (F12) → Console tab to see:

- "Login successful:" when login works
- "Login failed:" with error details when it fails
- "Login error:" for network or unexpected errors

## Still Having Issues?

Check the backend console logs for detailed error messages. The logs will show:

- Authentication attempts
- Keycloak communication errors
- Database connection issues
- User not found errors

Common backend error messages:

- "User not found in database" → User exists in Keycloak but not in your database (registration partially failed)
- "Authentication failed" → Keycloak rejected the credentials
- "Connection refused" → Keycloak or PostgreSQL not running
