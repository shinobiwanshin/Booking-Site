# 🚂 Railway Deployment Guide - Production Branch

This guide will help you deploy your Event Ticketing Platform to Railway using a separate production branch.

## 📋 Prerequisites

1. Railway account (sign up at https://railway.app)
2. Railway CLI installed
3. GitHub repository (already set up ✓)

## 🌿 Step 1: Create Production Branch

```bash
# Create and switch to production branch
git checkout -b production

# Push production branch to GitHub
git push -u origin production

# Switch back to main for development
git checkout main
```

## 🚀 Step 2: Install Railway CLI

```bash
npm install -g @railway/cli
```

## 🔐 Step 3: Login to Railway

```bash
railway login
```

This will open your browser for authentication.

## 📦 Step 4: Deploy Backend

```bash
# Navigate to backend folder
cd backend

# Initialize Railway project (creates railway.toml)
railway init

# Link to your Railway project (if already created in dashboard)
# OR create new project
railway init --name event-ticketing-backend

# Add PostgreSQL database
railway add --database postgresql

# This will automatically create DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD
```

## ⚙️ Step 5: Set Environment Variables

Set these in Railway Dashboard (Settings → Variables) or via CLI:

```bash
# Email Configuration
railway variables set MAIL_USERNAME=supersaiyan2k03@gmail.com
railway variables set MAIL_PASSWORD="tvxo sfox haqz ftph"
railway variables set MAIL_HOST=smtp.gmail.com
railway variables set MAIL_PORT=587

# Keycloak Configuration (use Railway's Keycloak service or external)
railway variables set KEYCLOAK_ADMIN_URL=https://your-keycloak-url.railway.app
railway variables set KEYCLOAK_ISSUER_URI=https://your-keycloak-url.railway.app/realms/event-ticket-platform
railway variables set KEYCLOAK_REALM=event-ticket-platform
railway variables set KEYCLOAK_ADMIN_USERNAME=admin
railway variables set KEYCLOAK_ADMIN_PASSWORD=your_secure_password
railway variables set KEYCLOAK_CLIENT_ID=event-ticket-client
railway variables set KEYCLOAK_CLIENT_SECRET=nfCJmmoiCWbHW38m16bujSBIW3ujoJzN

# Frontend URL (set after deploying frontend)
railway variables set FRONTEND_URL=https://your-frontend-url.vercel.app

# Database variables (automatically set by Railway PostgreSQL plugin)
# DATABASE_URL - set automatically
# DATABASE_USER - set automatically
# DATABASE_PASSWORD - set automatically
```

## 🌐 Step 6: Configure Railway to Deploy from Production Branch

### Option A: Via Railway Dashboard

1. Go to your project in Railway Dashboard
2. Click on your service → Settings
3. Under "Source" section, change branch from `main` to `production`
4. Click "Save"

### Option B: Via railway.toml (Already created)

The `railway.toml` file is configured to work with any branch.

## 🚀 Step 7: Deploy Backend

```bash
# Make sure you're on production branch
git checkout production

# Merge changes from main
git merge main

# Push to production
git push origin production

# Deploy to Railway (Railway auto-deploys on push, or use)
railway up
```

Railway will:

1. Detect it's a Maven/Spring Boot project
2. Run `mvn clean package`
3. Start the application with `java -jar target/tickets-0.0.1-SNAPSHOT.jar`
4. Assign a public URL like `https://event-ticketing-backend.up.railway.app`

## 🎨 Step 8: Deploy Frontend (Vercel Recommended)

```bash
# Navigate to frontend folder
cd ../frontend

# Install Vercel CLI
npm install -g vercel

# Login
vercel login

# Deploy to production
vercel --prod

# When prompted, link to existing project or create new
# Set production branch as 'production'
```

Set environment variables in Vercel Dashboard:

```
VITE_API_URL=https://your-backend.up.railway.app
VITE_KEYCLOAK_URL=https://your-keycloak.railway.app
VITE_KEYCLOAK_REALM=event-ticket-platform
VITE_KEYCLOAK_CLIENT_ID=event-ticket-platform-app
```

## 📋 Step 9: Deploy Keycloak (Optional - Railway)

If you want to deploy Keycloak on Railway:

```bash
# Create new Railway project for Keycloak
railway init --name event-ticketing-keycloak

# Deploy Keycloak (use Docker image)
# In Railway Dashboard:
# 1. New Service → Docker Image
# 2. Image: quay.io/keycloak/keycloak:23.0
# 3. Add PostgreSQL database
# 4. Set environment variables:
```

Keycloak Environment Variables:

```
KC_DB=postgres
KC_DB_URL=<railway postgres url>
KC_DB_USERNAME=<railway postgres user>
KC_DB_PASSWORD=<railway postgres password>
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=<your_secure_password>
KC_HOSTNAME=<your-keycloak-domain>
KC_PROXY=edge
KC_HTTP_ENABLED=true
```

Start command: `start --optimized`

## 🔄 Workflow: Development → Production

### For Development (main branch)

```bash
git checkout main
# Make changes
git add .
git commit -m "Feature: Add new feature"
git push origin main
```

### For Production Deployment

```bash
# Switch to production branch
git checkout production

# Merge changes from main
git merge main

# Push to production (Railway auto-deploys)
git push origin production

# Switch back to development
git checkout main
```

## 🔍 Step 10: Verify Deployment

```bash
# Check backend health
curl https://your-backend.up.railway.app/actuator/health

# Should return:
# {"status":"UP"}

# Check Railway logs
railway logs

# Check service status
railway status
```

## 📊 Railway Dashboard Features

- **Logs**: Real-time application logs
- **Metrics**: CPU, Memory, Network usage
- **Deployments**: History of all deployments
- **Variables**: Environment variable management
- **Settings**: Domain, scaling, etc.

## 🛠️ Troubleshooting

### Build Fails

```bash
# Check logs
railway logs

# Common issues:
# 1. Missing environment variables
# 2. Database connection issues
# 3. Build timeout (increase in settings)
```

### Database Connection Issues

```bash
# Verify DATABASE_URL is set
railway variables

# Test connection
railway run mvn test
```

### Port Issues

- Railway automatically sets `PORT` environment variable
- Our app reads `${PORT:8080}` so it works automatically

## 💰 Pricing

### Railway Free Tier

- $5 free credit per month
- ~500 hours of runtime
- Perfect for development/testing

### Estimated Monthly Cost (Production)

- Backend: ~$5-10
- PostgreSQL: ~$5
- Keycloak: ~$5-10
- **Total**: ~$15-25/month

### Vercel (Frontend)

- Free for personal projects
- Unlimited bandwidth for hobby

## 🔒 Security Checklist

- [ ] Changed default Keycloak admin password
- [ ] Using environment variables for secrets
- [ ] HTTPS enabled (Railway does this automatically)
- [ ] CORS configured for production domain
- [ ] Database backups enabled
- [ ] Rate limiting configured
- [ ] Email credentials secured

## 📝 Quick Commands Reference

```bash
# Deploy backend
cd backend && railway up

# View logs
railway logs

# Open in browser
railway open

# Run commands in Railway environment
railway run <command>

# List environment variables
railway variables

# Link to different project
railway link

# Check service status
railway status
```

## 🎯 Next Steps After Deployment

1. **Custom Domain**: Add your domain in Railway settings
2. **SSL Certificate**: Automatic via Railway
3. **Monitoring**: Set up alerts in Railway dashboard
4. **Backups**: Configure automated database backups
5. **CI/CD**: Set up GitHub Actions for automated testing before merge to production

## 📚 Useful Links

- Railway Docs: https://docs.railway.app
- Railway Dashboard: https://railway.app/dashboard
- Railway CLI Reference: https://docs.railway.app/develop/cli
- Spring Boot on Railway: https://docs.railway.app/guides/spring

---

**Production Branch Strategy Benefits:**

- ✅ Keep main branch for active development
- ✅ Test features thoroughly before production
- ✅ Easy rollbacks (just revert production branch)
- ✅ Clear separation of environments
- ✅ Can have staging branch too (main → staging → production)

Happy Deploying! 🚀
