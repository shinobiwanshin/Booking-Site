# 🚀 Google Cloud Platform Deployment Guide

Complete guide to deploy the Event Ticket Booking Platform on Google Cloud Platform (GCP).

---

## 📋 Table of Contents

- [Architecture Overview](#architecture-overview)
- [Prerequisites](#prerequisites)
- [Cost Estimate](#cost-estimate)
- [Quick Start](#quick-start)
- [Detailed Setup](#detailed-setup)
  - [1. GCP Project Setup](#1-gcp-project-setup)
  - [2. Deploy PostgreSQL (Cloud SQL)](#2-deploy-postgresql-cloud-sql)
  - [3. Deploy Keycloak (Cloud Run)](#3-deploy-keycloak-cloud-run)
  - [4. Deploy Backend (Cloud Run)](#4-deploy-backend-cloud-run)
  - [5. Deploy Frontend (Cloud Storage + CDN)](#5-deploy-frontend-cloud-storage--cdn)
- [Domain & SSL Configuration](#domain--ssl-configuration)
- [CI/CD with Cloud Build](#cicd-with-cloud-build)
- [Monitoring & Logging](#monitoring--logging)
- [Troubleshooting](#troubleshooting)

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    Google Cloud Platform                         │
│                                                                  │
│  ┌────────────────┐         ┌──────────────────┐               │
│  │ Cloud Storage  │         │    Cloud CDN     │               │
│  │  (Frontend)    │◄────────│  (Global Cache)  │               │
│  └────────────────┘         └──────────────────┘               │
│          │                                                       │
│          ▼                                                       │
│  ┌──────────────────────────────────────────────┐              │
│  │      Cloud Load Balancer (HTTPS/SSL)         │              │
│  └──────────────────────────────────────────────┘              │
│          │                           │                          │
│          ▼                           ▼                          │
│  ┌──────────────────┐       ┌──────────────────┐              │
│  │   Cloud Run      │       │   Cloud Run      │              │
│  │  (Backend API)   │◄─────►│   (Keycloak)     │              │
│  │  Auto-scaling    │       │   Auth Server    │              │
│  └──────────────────┘       └──────────────────┘              │
│          │                           │                          │
│          ▼                           ▼                          │
│  ┌──────────────────┐       ┌──────────────────┐              │
│  │   Cloud SQL      │       │ Secret Manager   │              │
│  │  (PostgreSQL)    │       │  (Credentials)   │              │
│  │  Managed DB      │       │   Encrypted      │              │
│  └──────────────────┘       └──────────────────┘              │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

**Services Used:**

- ☁️ **Cloud Run**: Serverless containers (Backend + Keycloak)
- 🗄️ **Cloud SQL**: Managed PostgreSQL database
- 📦 **Cloud Storage**: Static frontend hosting
- 🌐 **Cloud CDN**: Global content delivery
- 🔐 **Secret Manager**: Secure credentials storage
- 🔨 **Cloud Build**: CI/CD automation
- 📊 **Cloud Logging/Monitoring**: Observability

---

## ✅ Prerequisites

### 1. Google Cloud Account

- Sign up at https://cloud.google.com
- Enable billing (new accounts get $300 free credit)
- Have a credit card ready for verification

### 2. Install Required Tools

```bash
# Install gcloud CLI (macOS)
brew install --cask google-cloud-sdk

# Or download from: https://cloud.google.com/sdk/docs/install

# Install Docker
brew install docker
# Or from: https://www.docker.com/products/docker-desktop

# Verify installations
gcloud --version
docker --version
```

### 3. Authenticate with Google Cloud

```bash
# Login to Google Cloud
gcloud auth login

# Configure Docker to use gcloud credentials
gcloud auth configure-docker
```

---

## 💰 Cost Estimate

### Monthly Cost Breakdown (Production)

| Service                | Configuration          | Estimated Cost    |
| ---------------------- | ---------------------- | ----------------- |
| Cloud Run (Backend)    | 1M requests, 512MB RAM | $5-15             |
| Cloud Run (Keycloak)   | 1GB RAM, always-on     | $15-25            |
| Cloud SQL (PostgreSQL) | db-f1-micro, 10GB      | $10-15            |
| Cloud Storage          | 50GB, 100GB egress     | $3-5              |
| Cloud CDN              | 100GB bandwidth        | $8-12             |
| Secret Manager         | 10 secrets             | $0.30             |
| Cloud Build            | 120 min/day            | Free tier         |
| **Total Estimated**    |                        | **~$40-75/month** |

### Free Tier Inclusions

- 🆓 Cloud Run: 2M requests/month
- 🆓 Cloud Storage: 5GB storage
- 🆓 Cloud Build: 120 build-minutes/day
- 🆓 Cloud SQL: First backup free

### Cost Optimization Tips

1. Use `--min-instances=0` for dev environments
2. Enable Cloud CDN caching (reduces egress costs)
3. Use db-f1-micro for development/testing
4. Set up budget alerts in GCP Console

---

## 🚀 Quick Start

Copy and run these commands to deploy everything:

```bash
# Set your variables
export PROJECT_ID="event-tickets-$(date +%s)"
export REGION="us-central1"
export DB_PASSWORD="$(openssl rand -base64 32)"
export KEYCLOAK_ADMIN_PASSWORD="$(openssl rand -base64 32)"

# Create and configure project
gcloud projects create $PROJECT_ID --name="Event Ticket Platform"
gcloud config set project $PROJECT_ID
gcloud config set run/region $REGION

# Enable required APIs
gcloud services enable \
  cloudbuild.googleapis.com \
  run.googleapis.com \
  sqladmin.googleapis.com \
  storage.googleapis.com \
  secretmanager.googleapis.com \
  compute.googleapis.com

# Create Cloud SQL instance
gcloud sql instances create tickets-db \
  --database-version=POSTGRES_16 \
  --tier=db-f1-micro \
  --region=$REGION \
  --root-password="$DB_PASSWORD"

# Follow detailed steps below for complete deployment...
```

---

## 📚 Detailed Setup

### 1. GCP Project Setup

#### Create New Project

```bash
# Choose a unique project ID
export PROJECT_ID="event-tickets-prod-2025"
export REGION="us-central1"  # Or us-east1, europe-west1, etc.

# Create project
gcloud projects create $PROJECT_ID \
  --name="Event Ticket Platform" \
  --set-as-default

# Set default project
gcloud config set project $PROJECT_ID

# Link billing account (get ID from console or list)
gcloud billing accounts list
export BILLING_ACCOUNT_ID="YOUR-BILLING-ACCOUNT-ID"
gcloud billing projects link $PROJECT_ID \
  --billing-account=$BILLING_ACCOUNT_ID
```

#### Enable APIs

```bash
# Enable all required Google Cloud APIs
gcloud services enable \
  cloudbuild.googleapis.com \
  run.googleapis.com \
  sqladmin.googleapis.com \
  storage-api.googleapis.com \
  storage.googleapis.com \
  secretmanager.googleapis.com \
  compute.googleapis.com \
  cloudresourcemanager.googleapis.com \
  artifactregistry.googleapis.com \
  servicenetworking.googleapis.com

# Set default region
gcloud config set run/region $REGION
gcloud config set compute/region $REGION
```

---

### 2. Deploy PostgreSQL (Cloud SQL)

#### Create Cloud SQL Instance

```bash
# Generate secure password
export DB_PASSWORD="$(openssl rand -base64 32)"
echo "Database Password: $DB_PASSWORD" > gcp-credentials.txt

# Create PostgreSQL instance
# For production, use: db-n1-standard-1 or higher
# For dev/testing: db-f1-micro (free tier eligible)
gcloud sql instances create tickets-db \
  --database-version=POSTGRES_16 \
  --tier=db-f1-micro \
  --region=$REGION \
  --root-password="$DB_PASSWORD" \
  --backup \
  --backup-start-time=03:00 \
  --storage-type=SSD \
  --storage-size=10GB \
  --storage-auto-increase \
  --database-flags=max_connections=100

# Get connection name (save this!)
export SQL_CONNECTION_NAME=$(gcloud sql instances describe tickets-db \
  --format="value(connectionName)")
echo "SQL Connection Name: $SQL_CONNECTION_NAME" >> gcp-credentials.txt
```

#### Create Databases

```bash
# Create application database
gcloud sql databases create tickets_db \
  --instance=tickets-db

# Create Keycloak database
gcloud sql databases create keycloak_db \
  --instance=tickets-db
```

#### Create Database User

```bash
# Generate user password
export DB_USER_PASSWORD="$(openssl rand -base64 32)"
echo "DB User Password: $DB_USER_PASSWORD" >> gcp-credentials.txt

# Create database user
gcloud sql users create ticketuser \
  --instance=tickets-db \
  --password="$DB_USER_PASSWORD"
```

#### Store Secrets

```bash
# Store database passwords in Secret Manager
echo -n "$DB_USER_PASSWORD" | gcloud secrets create db-password \
  --data-file=- \
  --replication-policy="automatic"

# Grant Cloud Run access to secret
PROJECT_NUMBER=$(gcloud projects describe $PROJECT_ID --format="value(projectNumber)")
gcloud secrets add-iam-policy-binding db-password \
  --member="serviceAccount:$PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"
```

---

### 3. Deploy Keycloak (Cloud Run)

#### Create Keycloak Admin Password

```bash
# Generate admin password
export KEYCLOAK_ADMIN_PASSWORD="$(openssl rand -base64 32)"
echo "Keycloak Admin Password: $KEYCLOAK_ADMIN_PASSWORD" >> gcp-credentials.txt

# Store in Secret Manager
echo -n "$KEYCLOAK_ADMIN_PASSWORD" | gcloud secrets create keycloak-admin-password \
  --data-file=- \
  --replication-policy="automatic"

# Grant access
gcloud secrets add-iam-policy-binding keycloak-admin-password \
  --member="serviceAccount:$PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"
```

#### Create Keycloak Dockerfile

Create `keycloak/Dockerfile`:

```dockerfile
FROM quay.io/keycloak/keycloak:23.0.0 AS builder

# Enable health and metrics
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true
ENV KC_DB=postgres

# Build optimized image
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:23.0.0
COPY --from=builder /opt/keycloak/ /opt/keycloak/

# Production settings
ENV KC_HOSTNAME_STRICT=false
ENV KC_PROXY=edge
ENV KC_HTTP_ENABLED=true

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
```

#### Build and Deploy Keycloak

```bash
# Create keycloak directory
mkdir -p keycloak
cd keycloak

# Create Dockerfile (paste content above)
cat > Dockerfile << 'EOF'
FROM quay.io/keycloak/keycloak:23.0.0 AS builder
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true
ENV KC_DB=postgres
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:23.0.0
COPY --from=builder /opt/keycloak/ /opt/keycloak/
ENV KC_HOSTNAME_STRICT=false
ENV KC_PROXY=edge
ENV KC_HTTP_ENABLED=true
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
EOF

# Build and submit to Cloud Build
gcloud builds submit --tag gcr.io/$PROJECT_ID/keycloak:latest .

# Deploy to Cloud Run
gcloud run deploy keycloak \
  --image=gcr.io/$PROJECT_ID/keycloak:latest \
  --platform=managed \
  --region=$REGION \
  --allow-unauthenticated \
  --add-cloudsql-instances=$SQL_CONNECTION_NAME \
  --set-env-vars="KC_DB=postgres,KC_DB_URL_HOST=/cloudsql/$SQL_CONNECTION_NAME,KC_DB_URL_DATABASE=keycloak_db,KC_DB_USERNAME=ticketuser,KC_HOSTNAME_STRICT=false,KC_PROXY=edge,KEYCLOAK_ADMIN=admin" \
  --update-secrets="KC_DB_PASSWORD=db-password:latest,KEYCLOAK_ADMIN_PASSWORD=keycloak-admin-password:latest" \
  --memory=1Gi \
  --cpu=1 \
  --min-instances=1 \
  --max-instances=5 \
  --port=8080 \
  --timeout=300 \
  --command="start" \
  --args="--optimized"

# Get Keycloak URL
export KEYCLOAK_URL=$(gcloud run services describe keycloak \
  --region=$REGION \
  --format="value(status.url)")
echo "Keycloak URL: $KEYCLOAK_URL" >> ../gcp-credentials.txt

cd ..
```

#### Configure Keycloak Realm

```bash
# Open Keycloak admin console
echo "Open Keycloak at: $KEYCLOAK_URL"
echo "Username: admin"
echo "Password: (check gcp-credentials.txt)"

# Manual steps in Keycloak UI:
# 1. Create realm: event-ticket-platform
# 2. Create client: event-ticket-client
#    - Client Protocol: openid-connect
#    - Access Type: confidential
#    - Enable: Standard Flow, Direct Access Grants
#    - Valid Redirect URIs: https://your-domain.com/*
#    - Web Origins: https://your-domain.com
# 3. Create roles: ROLE_ATTENDEE, ROLE_ORGANIZER, ROLE_STAFF
# 4. Save client secret
```

#### Store Keycloak Client Secret

```bash
# After creating client in Keycloak, store the secret
echo -n "YOUR_KEYCLOAK_CLIENT_SECRET" | gcloud secrets create keycloak-client-secret \
  --data-file=- \
  --replication-policy="automatic"

gcloud secrets add-iam-policy-binding keycloak-client-secret \
  --member="serviceAccount:$PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"
```

---

### 4. Deploy Backend (Cloud Run)

#### Add Cloud SQL Dependency to Backend

Update `backend/pom.xml` to include Cloud SQL connector:

```xml
<!-- Add inside <dependencies> section -->
<dependency>
    <groupId>com.google.cloud.sql</groupId>
    <artifactId>postgres-socket-factory</artifactId>
    <version>1.15.2</version>
</dependency>
```

#### Create Backend Dockerfile

Create `backend/Dockerfile`:

```dockerfile
# Multi-stage build for optimal image size
FROM maven:3.9-eclipse-temurin-23 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Production image
FROM eclipse-temurin:23-jre-alpine

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Run as non-root user
RUN addgroup -g 1001 -S appuser && adduser -u 1001 -S appuser -G appuser
USER appuser

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
```

#### Create Email Secret

```bash
# For Gmail, create an App Password: https://myaccount.google.com/apppasswords
export EMAIL_PASSWORD="your-gmail-app-password"
echo -n "$EMAIL_PASSWORD" | gcloud secrets create email-password \
  --data-file=- \
  --replication-policy="automatic"

gcloud secrets add-iam-policy-binding email-password \
  --member="serviceAccount:$PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"
```

#### Build and Deploy Backend

```bash
cd backend

# Build and submit to Cloud Build
gcloud builds submit --tag gcr.io/$PROJECT_ID/backend:latest .

# Deploy to Cloud Run
gcloud run deploy backend \
  --image=gcr.io/$PROJECT_ID/backend:latest \
  --platform=managed \
  --region=$REGION \
  --allow-unauthenticated \
  --add-cloudsql-instances=$SQL_CONNECTION_NAME \
  --set-env-vars="SPRING_DATASOURCE_URL=jdbc:postgresql:///tickets_db?cloudSqlInstance=$SQL_CONNECTION_NAME&socketFactory=com.google.cloud.sql.postgres.SocketFactory&user=ticketuser,KEYCLOAK_ISSUER_URI=$KEYCLOAK_URL/realms/event-ticket-platform,KEYCLOAK_ADMIN_URL=$KEYCLOAK_URL,KEYCLOAK_REALM=event-ticket-platform,KEYCLOAK_CLIENT_ID=event-ticket-client,KEYCLOAK_ADMIN_USERNAME=admin,SPRING_MAIL_HOST=smtp.gmail.com,SPRING_MAIL_PORT=587,SPRING_MAIL_USERNAME=your-email@gmail.com,SPRING_PROFILES_ACTIVE=prod" \
  --update-secrets="SPRING_DATASOURCE_PASSWORD=db-password:latest,KEYCLOAK_CLIENT_SECRET=keycloak-client-secret:latest,KEYCLOAK_ADMIN_PASSWORD=keycloak-admin-password:latest,SPRING_MAIL_PASSWORD=email-password:latest" \
  --memory=2Gi \
  --cpu=2 \
  --min-instances=0 \
  --max-instances=100 \
  --timeout=300 \
  --port=8080

# Get backend URL
export BACKEND_URL=$(gcloud run services describe backend \
  --region=$REGION \
  --format="value(status.url)")
echo "Backend URL: $BACKEND_URL" >> ../gcp-credentials.txt

cd ..
```

#### Update Backend with Frontend URL

After deploying frontend (next step), update backend:

```bash
# Update with your actual frontend URL
gcloud run services update backend \
  --region=$REGION \
  --set-env-vars="FRONTEND_URL=https://your-domain.com"
```

---

### 5. Deploy Frontend (Cloud Storage + CDN)

#### Create Cloud Storage Bucket

```bash
# Choose globally unique bucket name
export BUCKET_NAME="event-tickets-frontend-prod"

# Create bucket
gsutil mb -l $REGION gs://$BUCKET_NAME

# Make bucket public for web hosting
gsutil iam ch allUsers:objectViewer gs://$BUCKET_NAME

# Configure for website hosting
gsutil web set -m index.html -e index.html gs://$BUCKET_NAME

# Enable CORS
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
gsutil cors set cors.json gs://$BUCKET_NAME
```

#### Build and Deploy Frontend

```bash
cd frontend

# Create production environment file
cat > .env.production << EOF
VITE_API_URL=$BACKEND_URL
VITE_KEYCLOAK_URL=$KEYCLOAK_URL
VITE_KEYCLOAK_REALM=event-ticket-platform
VITE_KEYCLOAK_CLIENT_ID=event-ticket-client
EOF

# Install dependencies and build
npm install
npm run build

# Deploy to Cloud Storage
gsutil -m rsync -r -d dist gs://$BUCKET_NAME

# Set cache headers
gsutil -m setmeta -h "Cache-Control:public, max-age=31536000, immutable" \
  "gs://$BUCKET_NAME/assets/**"
gsutil -m setmeta -h "Cache-Control:public, max-age=3600" \
  "gs://$BUCKET_NAME/*.html"
gsutil -m setmeta -h "Cache-Control:public, max-age=3600" \
  "gs://$BUCKET_NAME/*.js"

cd ..
```

#### Set up Load Balancer with Cloud CDN

```bash
# Reserve static IP
gcloud compute addresses create event-tickets-ip \
  --global

# Get the IP address
export STATIC_IP=$(gcloud compute addresses describe event-tickets-ip \
  --global \
  --format="value(address)")
echo "Static IP: $STATIC_IP" >> gcp-credentials.txt
echo "Point your domain's A record to: $STATIC_IP"

# Create backend bucket
gcloud compute backend-buckets create event-tickets-backend \
  --gcs-bucket-name=$BUCKET_NAME \
  --enable-cdn

# Create URL map
gcloud compute url-maps create event-tickets-lb \
  --default-backend-bucket=event-tickets-backend

# Create HTTP proxy (for redirect)
gcloud compute target-http-proxies create event-tickets-http-proxy \
  --url-map=event-tickets-lb

# Create forwarding rule for HTTP (will redirect to HTTPS)
gcloud compute forwarding-rules create event-tickets-http \
  --address=event-tickets-ip \
  --global \
  --target-http-proxy=event-tickets-http-proxy \
  --ports=80
```

---

## 🌍 Domain & SSL Configuration

### Option 1: Using Google-Managed SSL Certificate

```bash
# Replace with your actual domain
export DOMAIN="your-domain.com"

# Create managed SSL certificate
gcloud compute ssl-certificates create event-tickets-cert \
  --domains=$DOMAIN,www.$DOMAIN \
  --global

# Create HTTPS proxy
gcloud compute target-https-proxies create event-tickets-https-proxy \
  --url-map=event-tickets-lb \
  --ssl-certificates=event-tickets-cert

# Create HTTPS forwarding rule
gcloud compute forwarding-rules create event-tickets-https \
  --address=event-tickets-ip \
  --global \
  --target-https-proxy=event-tickets-https-proxy \
  --ports=443

# Check certificate status (takes 15-60 minutes)
gcloud compute ssl-certificates describe event-tickets-cert \
  --global \
  --format="get(managed.status)"
```

### DNS Configuration

Add these records to your domain registrar:

```
Type: A
Name: @
Value: [YOUR_STATIC_IP]
TTL: 300

Type: A
Name: www
Value: [YOUR_STATIC_IP]
TTL: 300
```

### Update Keycloak Valid Redirect URIs

After domain is configured:

1. Open Keycloak admin console
2. Go to Clients → event-ticket-client
3. Update Valid Redirect URIs: `https://your-domain.com/*`
4. Update Web Origins: `https://your-domain.com`
5. Save

---

## 🔄 CI/CD with Cloud Build

### Create Cloud Build Configuration

Create `cloudbuild.yaml` in project root:

```yaml
steps:
  # Build Backend
  - name: "gcr.io/cloud-builders/docker"
    args:
      - "build"
      - "-t"
      - "gcr.io/$PROJECT_ID/backend:$COMMIT_SHA"
      - "-t"
      - "gcr.io/$PROJECT_ID/backend:latest"
      - "./backend"
    id: "build-backend"

  # Push Backend Image
  - name: "gcr.io/cloud-builders/docker"
    args: ["push", "gcr.io/$PROJECT_ID/backend:$COMMIT_SHA"]
    id: "push-backend"

  # Deploy Backend to Cloud Run
  - name: "gcr.io/google.com/cloudsdktool/cloud-sdk"
    entrypoint: gcloud
    args:
      - "run"
      - "deploy"
      - "backend"
      - "--image=gcr.io/$PROJECT_ID/backend:$COMMIT_SHA"
      - "--region=us-central1"
      - "--platform=managed"
    id: "deploy-backend"

  # Build Frontend
  - name: "node:18"
    entrypoint: "bash"
    args:
      - "-c"
      - |
        cd frontend
        npm ci
        npm run build
    id: "build-frontend"

  # Deploy Frontend to Cloud Storage
  - name: "gcr.io/cloud-builders/gsutil"
    args:
      - "-m"
      - "rsync"
      - "-r"
      - "-d"
      - "frontend/dist"
      - "gs://event-tickets-frontend-prod"
    id: "deploy-frontend"

  # Set Cache Headers
  - name: "gcr.io/cloud-builders/gsutil"
    args:
      - "-m"
      - "setmeta"
      - "-h"
      - "Cache-Control:public, max-age=31536000, immutable"
      - "gs://event-tickets-frontend-prod/assets/**"
    id: "cache-assets"

images:
  - "gcr.io/$PROJECT_ID/backend:$COMMIT_SHA"
  - "gcr.io/$PROJECT_ID/backend:latest"

options:
  machineType: "E2_HIGHCPU_8"
  logging: CLOUD_LOGGING_ONLY

timeout: "1200s"
```

### Create Build Trigger

```bash
# Connect to GitHub repository
gcloud builds triggers create github \
  --repo-name=Booking-Site \
  --repo-owner=shinobiwanshin \
  --branch-pattern="^main$" \
  --build-config=cloudbuild.yaml \
  --description="Deploy on push to main"

# View triggers
gcloud builds triggers list
```

### Manual Build

```bash
# Trigger build manually
gcloud builds submit --config=cloudbuild.yaml .

# View build logs
gcloud builds list
gcloud builds log [BUILD_ID]
```

---

## 📊 Monitoring & Logging

### View Logs

```bash
# Backend logs
gcloud run services logs read backend \
  --region=$REGION \
  --limit=100

# Keycloak logs
gcloud run services logs read keycloak \
  --region=$REGION \
  --limit=100

# Live tail
gcloud run services logs tail backend --region=$REGION

# Filter errors only
gcloud run services logs read backend \
  --region=$REGION \
  --filter="severity=ERROR"
```

### Set Up Monitoring

```bash
# Create uptime check
gcloud monitoring uptime create event-tickets-uptime \
  --display-name="Event Tickets Health Check" \
  --resource-type=gce_instance \
  --host=$DOMAIN \
  --path=/actuator/health \
  --port=443

# Create alert policy for error rate
gcloud alpha monitoring policies create \
  --notification-channels=CHANNEL_ID \
  --display-name="High Error Rate Alert" \
  --condition-display-name="Error rate > 5%" \
  --condition-threshold-value=0.05 \
  --condition-threshold-duration=300s
```

### View Metrics in Console

```bash
# Open monitoring dashboard
echo "https://console.cloud.google.com/monitoring/dashboards?project=$PROJECT_ID"

# Open Cloud Run metrics
echo "https://console.cloud.google.com/run?project=$PROJECT_ID"

# Open Cloud SQL metrics
echo "https://console.cloud.google.com/sql/instances?project=$PROJECT_ID"
```

---

## 🔧 Troubleshooting

### Common Issues and Solutions

#### 1. Cloud SQL Connection Failed

**Error**: `java.sql.SQLException: Connection refused`

**Solution**:

```bash
# Verify Cloud SQL instance is running
gcloud sql instances list

# Check Cloud Run has Cloud SQL connection
gcloud run services describe backend --region=$REGION \
  --format="value(spec.template.spec.containers[0].env)"

# Verify service account has Cloud SQL Client role
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:$PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
  --role="roles/cloudsql.client"
```

#### 2. Keycloak Authentication Fails

**Error**: `401 Unauthorized` or `Account is not fully set up`

**Solution**:

```bash
# Check Keycloak is accessible
curl -I $KEYCLOAK_URL

# Verify user has firstName and lastName
# Follow the troubleshooting guide in main README.md

# Check client configuration in Keycloak UI
echo "Keycloak Admin: $KEYCLOAK_URL"
```

#### 3. Frontend Shows CORS Errors

**Error**: `Access-Control-Allow-Origin` error

**Solution**:

```bash
# Update backend CORS configuration
gcloud run services update backend \
  --region=$REGION \
  --set-env-vars="FRONTEND_URL=https://your-domain.com"

# Redeploy backend
gcloud run services update backend \
  --region=$REGION \
  --image=gcr.io/$PROJECT_ID/backend:latest
```

#### 4. SSL Certificate Not Provisioning

**Error**: Certificate status is `PROVISIONING` for > 24 hours

**Solution**:

```bash
# Verify DNS is properly configured
dig your-domain.com

# Check certificate status
gcloud compute ssl-certificates describe event-tickets-cert \
  --global

# If stuck, delete and recreate
gcloud compute ssl-certificates delete event-tickets-cert --global
gcloud compute ssl-certificates create event-tickets-cert \
  --domains=your-domain.com,www.your-domain.com \
  --global
```

#### 5. High Cloud Run Costs

**Issue**: Unexpected charges from Cloud Run

**Solution**:

```bash
# Reduce min instances for development
gcloud run services update backend \
  --region=$REGION \
  --min-instances=0

# Set request timeout
gcloud run services update backend \
  --region=$REGION \
  --timeout=60

# Set concurrency limits
gcloud run services update backend \
  --region=$REGION \
  --concurrency=80

# Set up budget alerts in GCP Console
```

### Get Help

```bash
# View service details
gcloud run services describe backend --region=$REGION

# Check service account permissions
gcloud projects get-iam-policy $PROJECT_ID \
  --flatten="bindings[].members" \
  --filter="bindings.members:$PROJECT_NUMBER-compute@developer.gserviceaccount.com"

# View all Cloud Run revisions
gcloud run revisions list --region=$REGION

# Rollback to previous revision if needed
gcloud run services update-traffic backend \
  --region=$REGION \
  --to-revisions=backend-00002-abc=100
```

---

## 🎯 Next Steps

After deployment:

1. ✅ **Test all features**

   - Login/Registration
   - Create events
   - Purchase tickets
   - QR code validation

2. 📊 **Set up monitoring**

   - Configure uptime checks
   - Set up error alerts
   - Create custom dashboards

3. 🔐 **Security hardening**

   - Enable VPC Service Controls
   - Set up Cloud Armor (DDoS protection)
   - Configure IAM roles properly

4. 💰 **Set budget alerts**

   ```bash
   # Open billing console
   echo "https://console.cloud.google.com/billing/$BILLING_ACCOUNT_ID/budgets?project=$PROJECT_ID"
   ```

5. 📝 **Document credentials**
   - Store gcp-credentials.txt securely
   - Share necessary info with team
   - Set up secret rotation schedule

---

## 📚 Additional Resources

- [Google Cloud Run Documentation](https://cloud.google.com/run/docs)
- [Cloud SQL for PostgreSQL](https://cloud.google.com/sql/docs/postgres)
- [Cloud CDN Documentation](https://cloud.google.com/cdn/docs)
- [Secret Manager Best Practices](https://cloud.google.com/secret-manager/docs/best-practices)
- [Cloud Build Documentation](https://cloud.google.com/build/docs)

---

## 💬 Support

If you encounter issues:

1. Check the [Troubleshooting](#troubleshooting) section
2. Review Cloud Logging: `gcloud run services logs read backend`
3. Open an issue on GitHub
4. Contact: supersaiyan2k03@gmail.com

---

**🎉 Congratulations! Your application is now running on Google Cloud Platform!**
