# Vercel Environment Variables Setup Guide

To make your deployed site work properly, you need to configure environment variables in Vercel that point to your production backend and Keycloak services.

## Step 1: Get Your Production URLs

You'll need these URLs from your deployments:

### Backend URL (Railway)

1. Go to [Railway Dashboard](https://railway.app/dashboard)
2. Open your backend service
3. Go to the **Settings** tab
4. Find the **Public Networking** section
5. Copy the domain (e.g., `https://booking-backend-production-xxxx.up.railway.app`)

### Keycloak URL (Render)

1. Go to your [Render Dashboard](https://dashboard.render.com/)
2. Open your Keycloak service
3. Copy the URL from the top of the page (e.g., `https://keycloak-o71s.onrender.com`)

## Step 2: Configure Vercel Environment Variables

1. Go to your [Vercel Dashboard](https://vercel.com/dashboard)
2. Select your project (`booking-site-zeta`)
3. Click on **Settings** in the top navigation
4. Click on **Environment Variables** in the left sidebar
5. Add the following variables:

### Variables to Add

| Variable Name             | Value                       | Environment |
| ------------------------- | --------------------------- | ----------- |
| `VITE_API_URL`            | Your Railway backend URL    | Production  |
| `VITE_KEYCLOAK_URL`       | Your Render Keycloak URL    | Production  |
| `VITE_KEYCLOAK_REALM`     | `event-ticket-platform`     | Production  |
| `VITE_KEYCLOAK_CLIENT_ID` | `event-ticket-platform-app` | Production  |

### How to Add Each Variable

For each variable:

1. Click **Add New** button
2. Enter the **Key** (variable name from table above)
3. Enter the **Value** (your actual URL or the specified value)
4. Select **Production** environment
5. Click **Save**

> [!IMPORTANT]
> Make sure to use your actual deployment URLs, not the placeholder values!
>
> Example values:
>
> - `VITE_API_URL` = `https://booking-backend-production-abc123.up.railway.app`
> - `VITE_KEYCLOAK_URL` = `https://keycloak-xyz789.onrender.com`

## Step 3: Redeploy Your Application

After adding all environment variables:

1. Go to the **Deployments** tab in your Vercel project
2. Click on the three dots (**...**) next to the latest deployment
3. Select **Redeploy**
4. Check **Use existing Build Cache** (optional, for faster deployment)
5. Click **Redeploy**

Alternatively, you can trigger a new deployment by pushing to your GitHub repository:

```bash
git push origin production
```

## Step 4: Verify the Configuration

Once the deployment completes:

1. **Test the signup page**: https://booking-site-zeta.vercel.app/signup

   - Fill out the form and submit
   - Check browser console for any errors
   - Registration should connect to your Railway backend

2. **Test the login page**: https://booking-site-zeta.vercel.app/login

   - Click "Sign in with Keycloak" or use Google/GitHub
   - Should redirect to your Render Keycloak instance
   - Check the URL in the browser - it should show your Keycloak domain

3. **Check browser console**:
   - Open Developer Tools (F12)
   - Go to the Console tab
   - Look for any errors related to API calls
   - Expected: No CORS errors, no 404s for API endpoints

## Troubleshooting

### CORS Errors

If you see CORS errors in the console, make sure your backend has the correct `CORS_ALLOWED_ORIGINS` environment variable set to your Vercel URL:

- In Railway, add: `CORS_ALLOWED_ORIGINS=https://booking-site-zeta.vercel.app`

### 404 Errors on API Calls

- Verify `VITE_API_URL` is set correctly in Vercel
- Check that your Railway backend is running
- Test the backend health endpoint: `https://your-backend-url/actuator/health`

### Keycloak Redirect Issues

- Verify `VITE_KEYCLOAK_URL` is set correctly in Vercel
- Make sure the Keycloak URL does NOT have a trailing slash
- Check that your Keycloak service is running on Render
- Verify the redirect URI is configured in Keycloak admin console

## Quick Reference

After configuration, your frontend will use these URLs:

- **API calls**: `VITE_API_URL + /api/v1/...`
- **Authentication**: `VITE_KEYCLOAK_URL + /realms/event-ticket-platform`
- **Callback**: `window.location.origin + /callback` (automatically uses your Vercel URL)
