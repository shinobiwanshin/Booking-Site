# 🚀 Deployment Steps - Keycloak & Frontend

## Current Status

✅ Backend - Deploying on Railway  
⏳ Keycloak - Ready to deploy  
⏳ Frontend - Ready to deploy

---

## 🔐 Step 1: Deploy Keycloak on Railway

### Option A: Via Railway Dashboard (Recommended)

1. **Go to Railway Dashboard**: https://railway.app/dashboard
2. **Select** your project: `booking-site-keycloak`
3. **Click** "+ New" button
4. **Select** "GitHub Repo"
5. **Choose** `shinobiwanshin/Booking-Site`
6. **Select** branch: `production`
7. **After creation**, go to **Settings** tab:

   - **Root Directory**: `keycloak`
   - Click **Save**

8. **Add Environment Variables** (Variables tab):

   ```
   KC_DB=postgres
   KC_DB_URL=jdbc:postgresql://postgres.railway.internal:5432/railway
   KC_DB_USERNAME=postgres
   KC_DB_PASSWORD=dSxZSajFrbhzSfuDakhTlyKdnYbMFIGZ
   KEYCLOAK_ADMIN=admin
   KEYCLOAK_ADMIN_PASSWORD=Admin123!SecurePassword
   KC_HOSTNAME_STRICT=false
   KC_PROXY=edge
   KC_HTTP_ENABLED=true
   ```

9. **Generate Domain** (Settings → Networking):

   - Click "Generate Domain"
   - **Copy the Keycloak URL** (e.g., `https://keycloak-production-xxxx.up.railway.app`)

10. **Deploy**: Railway will automatically deploy

### Option B: Via CLI

```bash
cd keycloak
railway link
# Select: booking-site-keycloak → production → Create new service
railway up
```

---

## 🎨 Step 2: Configure Keycloak

1. **Access Keycloak Admin Console**:

   - URL: `https://your-keycloak-url.up.railway.app/admin`
   - Username: `admin`
   - Password: `Admin123!SecurePassword`

2. **Create Realm**:

   - Click "Create realm"
   - Name: `event-ticket-platform`
   - Click "Create"

3. **Create Client**:

   - Go to Clients → Create client
   - Client ID: `event-ticket-platform-app`
   - Client authentication: OFF (public client)
   - Valid redirect URIs:
     ```
     http://localhost:5173/*
     https://your-frontend-url.vercel.app/*
     https://your-backend-url.up.railway.app/*
     ```
   - Web origins: `*`
   - Click "Save"

4. **Create Roles**:

   - Go to Realm roles → Create role
   - Create these roles:
     - `ROLE_ATTENDEE`
     - `ROLE_ORGANIZER`
     - `ROLE_STAFF`

5. **Create Test Users** (Optional):
   - Go to Users → Add user
   - Username: `testattendee`
   - Email: `test@example.com`
   - Click "Create"
   - Go to Credentials tab → Set password
   - Go to Role mapping → Assign `ROLE_ATTENDEE`

---

## 🎨 Step 3: Deploy Frontend on Vercel

### Prepare Environment Variables

First, gather these URLs:

- **Backend URL**: Get from Railway dashboard (backend service)
- **Keycloak URL**: Get from Railway dashboard (keycloak service)

### Deploy to Vercel

1. **Install Vercel CLI** (if not already installed):

   ```bash
   npm install -g vercel
   ```

2. **Login to Vercel**:

   ```bash
   vercel login
   ```

3. **Deploy**:

   ```bash
   cd frontend
   vercel --prod
   ```

4. **During deployment**, Vercel will ask for environment variables. Set these:

   ```
   VITE_API_URL=https://your-backend-url.up.railway.app
   VITE_KEYCLOAK_URL=https://your-keycloak-url.up.railway.app
   VITE_KEYCLOAK_REALM=event-ticket-platform
   VITE_KEYCLOAK_CLIENT_ID=event-ticket-platform-app
   ```

   **OR** set them in Vercel Dashboard:

   - Go to your project → Settings → Environment Variables
   - Add each variable above

5. **Copy the Vercel URL** (e.g., `https://your-app.vercel.app`)

---

## 🔄 Step 4: Update Backend Environment Variables

Once you have the **Frontend URL** from Vercel:

1. **Go to Railway Dashboard**
2. **Select** backend service
3. **Go to** Variables tab
4. **Add/Update**:

   ```
   FRONTEND_URL=https://your-app.vercel.app
   KEYCLOAK_ADMIN_URL=https://your-keycloak-url.up.railway.app
   KEYCLOAK_ISSUER_URI=https://your-keycloak-url.up.railway.app/realms/event-ticket-platform
   KEYCLOAK_REALM=event-ticket-platform
   KEYCLOAK_ADMIN_USERNAME=admin
   KEYCLOAK_ADMIN_PASSWORD=Admin123!SecurePassword
   KEYCLOAK_CLIENT_ID=event-ticket-platform-app
   KEYCLOAK_CLIENT_SECRET=nfCJmmoiCWbHW38m16bujSBIW3ujoJzN
   ```

5. **Railway will automatically redeploy** the backend

---

## 🔄 Step 5: Update Keycloak Redirect URIs

1. **Go back to Keycloak Admin Console**
2. **Navigate to**: Clients → `event-ticket-platform-app`
3. **Update Valid redirect URIs** with your actual Vercel URL:
   ```
   https://your-actual-app.vercel.app/*
   ```
4. **Click** "Save"

---

## ✅ Step 6: Verify Deployment

### Test Backend

```bash
curl https://your-backend-url.up.railway.app/actuator/health
```

Expected: `{"status":"UP"}`

### Test Keycloak

Open: `https://your-keycloak-url.up.railway.app/realms/event-ticket-platform/.well-known/openid-configuration`

### Test Frontend

Open: `https://your-app.vercel.app`

---

## 📝 Your Final URLs

After deployment, you'll have:

```
Backend:  https://[backend-service].up.railway.app
Keycloak: https://[keycloak-service].up.railway.app
Frontend: https://[your-app].vercel.app
Database: postgres.railway.internal:5432 (internal only)
```

**Update these in**:

- Backend environment variables
- Frontend environment variables
- Keycloak client redirect URIs

---

## 🐛 Troubleshooting

### Backend won't start

```bash
railway logs --service backend
```

Check for missing environment variables

### Keycloak connection issues

- Verify `KC_DB_URL` uses `postgres.railway.internal`
- Check database credentials match

### Frontend CORS errors

- Ensure `FRONTEND_URL` is set in backend
- Check Keycloak redirect URIs include frontend URL

### Can't login

- Verify Keycloak realm and client are configured
- Check browser console for errors
- Ensure `VITE_KEYCLOAK_URL` is correct

---

## 🎯 Next Steps

1. Deploy Keycloak on Railway ✨
2. Configure Keycloak realm and client ✨
3. Deploy Frontend on Vercel ✨
4. Update all environment variables ✨
5. Test the full application flow ✨

Happy Deploying! 🚀
