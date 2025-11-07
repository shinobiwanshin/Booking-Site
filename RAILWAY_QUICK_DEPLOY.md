# 🚂 Railway Quick Deploy Guide

## Current Status

- ✅ Railway CLI installed and authenticated
- ✅ Production branch created and pushed
- ✅ PostgreSQL database already set up in `booking-site-keycloak` project

## 📊 Your Railway Setup

**Project**: `booking-site-keycloak`  
**Environment**: `production`  
**Database**: PostgreSQL (already running)  
**Database URL**: `postgresql://postgres:dSxZSajFrbhzSfuDakhTlyKdnYbMFIGZ@postgres.railway.internal:5432/railway`

---

## 🚀 Deploy Backend (Via Dashboard - Recommended)

### Step 1: Go to Railway Dashboard

Open: https://railway.app/dashboard

### Step 2: Select Your Project

- Click on `booking-site-keycloak` project

### Step 3: Create Backend Service

1. Click "+ New" button
2. Select "GitHub Repo"
3. Choose `shinobiwanshin/Booking-Site`
4. Select `production` branch
5. Railway will detect it's a Spring Boot app

### Step 4: Configure Backend Service

Click on the newly created service → Variables tab → Add these:

```bash
# Database Configuration (use internal URL)
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres.railway.internal:5432/railway
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=dSxZSajFrbhzSfuDakhTlyKdnYbMFIGZ
SPRING_JPA_HIBERNATE_DDL_AUTO=update

# Email Configuration
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=supersaiyan2k03@gmail.com
SPRING_MAIL_PASSWORD=tvxo sfox haqz ftph

# Keycloak Configuration (use your Keycloak Railway URL)
KEYCLOAK_ADMIN_URL=https://your-keycloak-service.up.railway.app
KEYCLOAK_ISSUER_URI=https://your-keycloak-service.up.railway.app/realms/event-ticket-platform
KEYCLOAK_REALM=event-ticket-platform
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=your_admin_password
KEYCLOAK_CLIENT_ID=event-ticket-client
KEYCLOAK_CLIENT_SECRET=nfCJmmoiCWbHW38m16bujSBIW3ujoJzN

# Frontend URL (update after frontend deployment)
FRONTEND_URL=https://your-frontend.vercel.app
```

### Step 5: Set Root Directory

- Settings tab → Root Directory → Enter: `backend`
- Railway will build from the backend folder

### Step 6: Generate Domain

- Settings tab → Generate Domain
- Copy the URL (e.g., `https://backend-production-xxxx.up.railway.app`)

### Step 7: Deploy

- Railway auto-deploys on git push
- Or click "Deploy" button manually

---

## 🔐 Deploy Keycloak (If Not Already Running)

### Check if Keycloak is Running

In Railway dashboard, check if you have a Keycloak service.

### If Keycloak is NOT running:

1. Click "+ New" in your project
2. Select "Docker Image"
3. Image: `quay.io/keycloak/keycloak:23.0`
4. Add environment variables:

```bash
KC_DB=postgres
KC_DB_URL=jdbc:postgresql://postgres.railway.internal:5432/railway
KC_DB_USERNAME=postgres
KC_DB_PASSWORD=dSxZSajFrbhzSfuDakhTlyKdnYbMFIGZ
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=YourSecurePassword123!
KC_HOSTNAME_STRICT=false
KC_PROXY=edge
KC_HTTP_ENABLED=true
```

5. Start command: `start --optimized`
6. Generate domain and copy URL

---

## 🎨 Deploy Frontend (Vercel)

### Step 1: Install Vercel CLI

```bash
npm install -g vercel
```

### Step 2: Deploy

```bash
cd frontend
vercel login
vercel --prod
```

### Step 3: Set Environment Variables in Vercel

```bash
VITE_API_URL=https://your-backend.up.railway.app
VITE_KEYCLOAK_URL=https://your-keycloak.up.railway.app
VITE_KEYCLOAK_REALM=event-ticket-platform
VITE_KEYCLOAK_CLIENT_ID=event-ticket-client
```

---

## 🔄 Alternative: Deploy Everything via CLI

### Backend

```bash
cd backend

# Link to project
railway link

# Add environment variables (one by one)
railway variables set SPRING_DATASOURCE_URL="jdbc:postgresql://postgres.railway.internal:5432/railway"
railway variables set SPRING_DATASOURCE_USERNAME="postgres"
railway variables set SPRING_DATASOURCE_PASSWORD="dSxZSajFrbhzSfuDakhTlyKdnYbMFIGZ"
railway variables set SPRING_JPA_HIBERNATE_DDL_AUTO="update"
railway variables set SPRING_MAIL_HOST="smtp.gmail.com"
railway variables set SPRING_MAIL_PORT="587"
railway variables set SPRING_MAIL_USERNAME="supersaiyan2k03@gmail.com"
railway variables set SPRING_MAIL_PASSWORD="tvxo sfox haqz ftph"

# Deploy
railway up
```

---

## ✅ Verify Deployment

### Check Backend Health

```bash
# Get backend URL from Railway dashboard
curl https://your-backend.up.railway.app/actuator/health

# Should return:
# {"status":"UP"}
```

### Check Logs

```bash
railway logs
```

### Test API

```bash
# Test login endpoint
curl -X POST https://your-backend.up.railway.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}'
```

---

## 🎯 Configure Keycloak

1. Access Keycloak Admin: `https://your-keycloak.up.railway.app/admin`
2. Login with admin credentials
3. Create realm: `event-ticket-platform`
4. Create client: `event-ticket-client`
5. Configure redirect URIs:
   - Backend: `https://your-backend.up.railway.app/*`
   - Frontend: `https://your-frontend.vercel.app/*`
6. Create roles: `ROLE_ATTENDEE`, `ROLE_ORGANIZER`, `ROLE_STAFF`

---

## 💰 Cost Estimate

**Railway Free Tier:**

- $5 free credit/month
- ~500 hours of runtime
- Perfect for development

**Production Estimate:**

- Backend: ~$5-10/month
- PostgreSQL: ~$5/month
- Keycloak: ~$5-10/month
- **Total**: ~$15-25/month

---

## 🔧 Troubleshooting

### Build Fails

```bash
railway logs
```

Check for missing dependencies or environment variables

### Database Connection Issues

- Verify `DATABASE_URL` is using internal URL: `postgres.railway.internal`
- Check PostgreSQL service is running

### Port Issues

- Railway automatically sets `PORT` environment variable
- Spring Boot reads from `${PORT:8080}`

---

## 📝 Update Deployment

### Push Changes

```bash
git checkout production
git merge main
git push origin production
```

Railway will auto-deploy on push to production branch!

---

## 🌐 Your URLs After Deployment

```
Backend:  https://backend-production-xxxx.up.railway.app
Keycloak: https://keycloak-production-xxxx.up.railway.app
Frontend: https://your-app.vercel.app
```

Update these in your environment variables once deployed!

---

Happy Deploying! 🚀
