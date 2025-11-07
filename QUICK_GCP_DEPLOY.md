# 🚀 Quick GCP Deployment Guide

This is a simplified, step-by-step guide to deploy your Event Ticket Platform to Google Cloud.

## ✅ Prerequisites Checklist

- [x] Google Cloud account
- [x] `gcloud` CLI installed (version 546.0.0)
- [x] Authenticated (`gcloud auth login`)
- [x] Project created: `event-ticket-management-477218`
- [ ] **Billing enabled** ← YOU ARE HERE

## 🔴 Step 1: Enable Billing (Required)

Visit: https://console.cloud.google.com/billing?project=event-ticket-management-477218

1. Click "Link a Billing Account" or "Create Billing Account"
2. Add payment method
3. Enable billing for the project

**New users get $300 free credits for 90 days!**

---

## 🔧 Step 2: Enable Required APIs

Once billing is enabled, run:

```bash
gcloud services enable \
  run.googleapis.com \
  sqladmin.googleapis.com \
  secretmanager.googleapis.com \
  cloudbuild.googleapis.com \
  artifactregistry.googleapis.com \
  compute.googleapis.com \
  vpcaccess.googleapis.com \
  --project=event-ticket-management-477218
```

This will take 2-3 minutes.

---

## 🗄️ Step 3: Create Cloud SQL Database

### 3.1 Create PostgreSQL Instance

```bash
gcloud sql instances create event-ticket-db \
  --database-version=POSTGRES_16 \
  --tier=db-f1-micro \
  --region=us-central1 \
  --root-password=YourSecurePassword123! \
  --project=event-ticket-management-477218
```

⏱️ This takes 5-10 minutes.

### 3.2 Create Application Database

```bash
gcloud sql databases create ticketing \
  --instance=event-ticket-db \
  --project=event-ticket-management-477218
```

### 3.3 Create Database User

```bash
gcloud sql users create ticketing_user \
  --instance=event-ticket-db \
  --password=SecureAppPassword123! \
  --project=event-ticket-management-477218
```

### 3.4 Get Connection Name

```bash
gcloud sql instances describe event-ticket-db \
  --project=event-ticket-management-477218 \
  --format="value(connectionName)"
```

Save this! Format: `event-ticket-management-477218:us-central1:event-ticket-db`

---

## 🔐 Step 4: Store Secrets in Secret Manager

### 4.1 Database Password

```bash
echo -n "SecureAppPassword123!" | gcloud secrets create db-password \
  --data-file=- \
  --replication-policy="automatic" \
  --project=event-ticket-management-477218
```

### 4.2 Email Password

```bash
echo -n "tvxo sfox haqz ftph" | gcloud secrets create mail-password \
  --data-file=- \
  --replication-policy="automatic" \
  --project=event-ticket-management-477218
```

### 4.3 Keycloak Admin Password

```bash
echo -n "YourKeycloakAdminPass123!" | gcloud secrets create keycloak-admin-password \
  --data-file=- \
  --replication-policy="automatic" \
  --project=event-ticket-management-477218
```

### 4.4 Keycloak Client Secret

```bash
echo -n "nfCJmmoiCWbHW38m16bujSBIW3ujoJzN" | gcloud secrets create keycloak-client-secret \
  --data-file=- \
  --replication-policy="automatic" \
  --project=event-ticket-management-477218
```

---

## 🐳 Step 5: Build and Deploy Keycloak

### 5.1 Build Keycloak Image

```bash
cd /Users/amitabhanath/Documents/Booking-Site/keycloak

gcloud builds submit \
  --tag gcr.io/event-ticket-management-477218/keycloak:latest \
  --project=event-ticket-management-477218
```

### 5.2 Deploy Keycloak to Cloud Run

```bash
gcloud run deploy keycloak \
  --image gcr.io/event-ticket-management-477218/keycloak:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars "KC_DB=postgres,KC_HOSTNAME_STRICT=false,KC_PROXY=edge,KC_HTTP_ENABLED=true,KEYCLOAK_ADMIN=admin" \
  --set-secrets "KEYCLOAK_ADMIN_PASSWORD=keycloak-admin-password:latest" \
  --add-cloudsql-instances event-ticket-management-477218:us-central1:event-ticket-db \
  --memory 512Mi \
  --cpu 1 \
  --min-instances 0 \
  --max-instances 4 \
  --project=event-ticket-management-477218
```

### 5.3 Get Keycloak URL

```bash
gcloud run services describe keycloak \
  --platform managed \
  --region us-central1 \
  --project=event-ticket-management-477218 \
  --format="value(status.url)"
```

Save this URL! You'll need it for the backend configuration.

---

## 🚀 Step 6: Build and Deploy Backend

### 6.1 Build Backend Image

```bash
cd /Users/amitabhanath/Documents/Booking-Site/backend

gcloud builds submit \
  --tag gcr.io/event-ticket-management-477218/backend:latest \
  --project=event-ticket-management-477218
```

⏱️ This takes 3-5 minutes.

### 6.2 Deploy Backend to Cloud Run

Replace `<KEYCLOAK_URL>` with the URL from Step 5.3:

```bash
gcloud run deploy backend \
  --image gcr.io/event-ticket-management-477218/backend:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars "\
SPRING_DATASOURCE_URL=jdbc:postgresql:///ticketing?cloudSqlInstance=event-ticket-management-477218:us-central1:event-ticket-db&socketFactory=com.google.cloud.sql.postgres.SocketFactory,\
SPRING_DATASOURCE_USERNAME=ticketing_user,\
SPRING_JPA_HIBERNATE_DDL_AUTO=update,\
SPRING_MAIL_HOST=smtp.gmail.com,\
SPRING_MAIL_PORT=587,\
SPRING_MAIL_USERNAME=supersaiyan2k03@gmail.com,\
KEYCLOAK_ADMIN_URL=<KEYCLOAK_URL>,\
KEYCLOAK_ISSUER_URI=<KEYCLOAK_URL>/realms/event-ticket-platform,\
KEYCLOAK_REALM=event-ticket-platform,\
KEYCLOAK_ADMIN_USERNAME=admin,\
KEYCLOAK_CLIENT_ID=event-ticket-client,\
FRONTEND_URL=https://your-frontend-url.com" \
  --set-secrets "\
SPRING_DATASOURCE_PASSWORD=db-password:latest,\
SPRING_MAIL_PASSWORD=mail-password:latest,\
KEYCLOAK_ADMIN_PASSWORD=keycloak-admin-password:latest,\
KEYCLOAK_CLIENT_SECRET=keycloak-client-secret:latest" \
  --add-cloudsql-instances event-ticket-management-477218:us-central1:event-ticket-db \
  --memory 1Gi \
  --cpu 1 \
  --min-instances 0 \
  --max-instances 10 \
  --project=event-ticket-management-477218
```

### 6.3 Get Backend URL

```bash
gcloud run services describe backend \
  --platform managed \
  --region us-central1 \
  --project=event-ticket-management-477218 \
  --format="value(status.url)"
```

---

## 🎨 Step 7: Deploy Frontend (Cloud Storage + CDN)

### 7.1 Create Storage Bucket

```bash
gsutil mb -p event-ticket-management-477218 -l us-central1 gs://event-ticket-frontend-477218
```

### 7.2 Build Frontend

First, update the frontend environment variables in `/frontend/.env.production`:

```bash
cd /Users/amitabhanath/Documents/Booking-Site/frontend

cat > .env.production << 'EOF'
VITE_API_URL=<BACKEND_URL>
VITE_KEYCLOAK_URL=<KEYCLOAK_URL>
VITE_KEYCLOAK_REALM=event-ticket-platform
VITE_KEYCLOAK_CLIENT_ID=event-ticket-client
EOF
```

Replace `<BACKEND_URL>` and `<KEYCLOAK_URL>` with the URLs from previous steps.

### 7.3 Build and Upload

```bash
npm install
npm run build

gsutil -m rsync -r -d dist gs://event-ticket-frontend-477218
```

### 7.4 Make Public

```bash
gsutil iam ch allUsers:objectViewer gs://event-ticket-frontend-477218

gsutil web set -m index.html -e index.html gs://event-ticket-frontend-477218
```

### 7.5 Set CORS

```bash
cat > cors.json << 'EOF'
[
  {
    "origin": ["*"],
    "method": ["GET", "HEAD"],
    "responseHeader": ["Content-Type"],
    "maxAgeSeconds": 3600
  }
]
EOF

gsutil cors set cors.json gs://event-ticket-frontend-477218
```

### 7.6 Frontend URL

Your frontend will be available at:

```
https://storage.googleapis.com/event-ticket-frontend-477218/index.html
```

Or set up Cloud CDN for better performance (optional).

---

## 🔧 Step 8: Configure Keycloak Realm

1. **Access Keycloak Admin Console:**

   - URL: `<KEYCLOAK_URL>/admin`
   - Username: `admin`
   - Password: `YourKeycloakAdminPass123!`

2. **Create Realm:**

   - Click dropdown (top-left) → "Create Realm"
   - Realm name: `event-ticket-platform`
   - Click "Create"

3. **Create Client:**

   - Clients → "Create client"
   - Client ID: `event-ticket-client`
   - Client Protocol: `openid-connect`
   - Click "Next"
   - Client authentication: `ON`
   - Authorization: `OFF`
   - Authentication flow: Enable all
   - Click "Save"

4. **Configure Client:**

   - Valid redirect URIs: `<FRONTEND_URL>/*` and `<BACKEND_URL>/*`
   - Web origins: `<FRONTEND_URL>` and `<BACKEND_URL>`
   - Click "Save"

5. **Get Client Secret:**

   - Credentials tab → Copy "Client Secret"
   - Update Secret Manager if needed

6. **Create Roles:**
   - Realm roles → "Create role"
   - Create: `ROLE_ATTENDEE`, `ROLE_ORGANIZER`, `ROLE_STAFF`

---

## ✅ Step 9: Test Deployment

### Test Backend Health

```bash
curl https://<BACKEND_URL>/actuator/health
# Should return: {"status":"UP"}
```

### Test Keycloak

```bash
curl https://<KEYCLOAK_URL>/realms/event-ticket-platform
# Should return realm configuration JSON
```

### Test Frontend

Open in browser:

```
https://storage.googleapis.com/event-ticket-frontend-477218/index.html
```

---

## 📊 Monitor Your Deployment

### View Logs

```bash
# Backend logs
gcloud run services logs read backend --region us-central1 --limit 50

# Keycloak logs
gcloud run services logs read keycloak --region us-central1 --limit 50
```

### View Metrics

Visit Cloud Run Console:

```
https://console.cloud.google.com/run?project=event-ticket-management-477218
```

---

## 💰 Cost Estimate

**Free Tier (first 90 days with $300 credit):**

- Cloud Run: Free (within limits)
- Cloud SQL (db-f1-micro): ~$7/month
- Cloud Storage: ~$0.02/month
- Secret Manager: Free (first 6 secrets)
- **Estimated Total**: ~$7-10/month (covered by free credits)

---

## 🔄 Update Your Application

### Update Backend

```bash
cd backend
gcloud builds submit --tag gcr.io/event-ticket-management-477218/backend:latest
gcloud run deploy backend --image gcr.io/event-ticket-management-477218/backend:latest --region us-central1
```

### Update Frontend

```bash
cd frontend
npm run build
gsutil -m rsync -r -d dist gs://event-ticket-frontend-477218
```

---

## 🆘 Troubleshooting

### Check Service Status

```bash
gcloud run services list --project=event-ticket-management-477218
```

### Check Database Connection

```bash
gcloud sql instances list --project=event-ticket-management-477218
```

### View Recent Errors

```bash
gcloud run services logs read backend --region us-central1 --limit 20
```

---

## 📚 Useful Commands

```bash
# Set default project
gcloud config set project event-ticket-management-477218

# Set default region
gcloud config set run/region us-central1

# List all Cloud Run services
gcloud run services list

# Delete a service
gcloud run services delete SERVICE_NAME --region us-central1

# View all secrets
gcloud secrets list
```

---

## 🎯 Next Steps

1. [ ] Set up custom domain (https://cloud.google.com/run/docs/mapping-custom-domains)
2. [ ] Configure Cloud CDN for frontend
3. [ ] Set up monitoring alerts
4. [ ] Configure automated backups
5. [ ] Set up CI/CD with Cloud Build (already have cloudbuild.yaml!)
6. [ ] Enable Cloud Armor for DDoS protection

---

**Need Help?**

- GCP Documentation: https://cloud.google.com/docs
- Cloud Run Docs: https://cloud.google.com/run/docs
- Support: https://cloud.google.com/support

Happy Deploying! 🚀
