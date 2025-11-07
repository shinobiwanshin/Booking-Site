#!/bin/bash

# Google Cloud Platform Quick Deployment Script
# This script automates the deployment of Event Ticket Platform to GCP

set -e  # Exit on error

echo "🚀 Event Ticket Platform - Google Cloud Deployment"
echo "=================================================="
echo ""

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    echo "❌ Error: gcloud CLI is not installed"
    echo "Install from: https://cloud.google.com/sdk/docs/install"
    exit 1
fi

# Check if user is authenticated
if ! gcloud auth list --filter=status:ACTIVE --format="value(account)" &> /dev/null; then
    echo "❌ Error: Not authenticated with gcloud"
    echo "Run: gcloud auth login"
    exit 1
fi

# Prompt for project configuration
echo "📝 Project Configuration"
echo "------------------------"
read -p "Enter your GCP Project ID (leave blank for auto-generate): " PROJECT_ID

if [ -z "$PROJECT_ID" ]; then
    PROJECT_ID="event-tickets-$(date +%s)"
    echo "✅ Generated Project ID: $PROJECT_ID"
fi

read -p "Enter your preferred region (default: us-central1): " REGION
REGION=${REGION:-us-central1}

read -p "Enter your domain name (e.g., example.com) or leave blank: " DOMAIN

read -p "Enter your email for SMTP (e.g., your-email@gmail.com): " SMTP_EMAIL

echo ""
echo "📋 Configuration Summary:"
echo "  Project ID: $PROJECT_ID"
echo "  Region: $REGION"
echo "  Domain: ${DOMAIN:-Not configured}"
echo "  SMTP Email: $SMTP_EMAIL"
echo ""

read -p "Continue with deployment? (y/N): " CONTINUE
if [ "$CONTINUE" != "y" ] && [ "$CONTINUE" != "Y" ]; then
    echo "❌ Deployment cancelled"
    exit 0
fi

echo ""
echo "🔧 Step 1: Creating GCP Project..."
gcloud projects create $PROJECT_ID --name="Event Ticket Platform" || echo "Project may already exist"
gcloud config set project $PROJECT_ID

echo ""
echo "💳 Please link a billing account in the Google Cloud Console:"
echo "https://console.cloud.google.com/billing/linkedaccount?project=$PROJECT_ID"
read -p "Press Enter after linking billing account..."

echo ""
echo "🔌 Step 2: Enabling Required APIs..."
gcloud services enable \
  cloudbuild.googleapis.com \
  run.googleapis.com \
  sqladmin.googleapis.com \
  storage.googleapis.com \
  secretmanager.googleapis.com \
  compute.googleapis.com \
  artifactregistry.googleapis.com

echo ""
echo "🗄️ Step 3: Creating Cloud SQL Instance..."
DB_PASSWORD=$(openssl rand -base64 32)
gcloud sql instances create tickets-db \
  --database-version=POSTGRES_16 \
  --tier=db-f1-micro \
  --region=$REGION \
  --root-password="$DB_PASSWORD" \
  --backup \
  --storage-type=SSD \
  --storage-size=10GB

echo ""
echo "📊 Step 4: Creating Databases..."
gcloud sql databases create tickets_db --instance=tickets-db
gcloud sql databases create keycloak_db --instance=tickets-db

echo ""
echo "👤 Step 5: Creating Database User..."
DB_USER_PASSWORD=$(openssl rand -base64 32)
gcloud sql users create ticketuser \
  --instance=tickets-db \
  --password="$DB_USER_PASSWORD"

echo ""
echo "🔐 Step 6: Storing Secrets..."
echo -n "$DB_USER_PASSWORD" | gcloud secrets create db-password --data-file=-

KEYCLOAK_ADMIN_PASSWORD=$(openssl rand -base64 32)
echo -n "$KEYCLOAK_ADMIN_PASSWORD" | gcloud secrets create keycloak-admin-password --data-file=-

read -p "Enter your Gmail App Password (for SMTP): " EMAIL_PASSWORD
echo -n "$EMAIL_PASSWORD" | gcloud secrets create email-password --data-file=-

echo ""
echo "🔑 Step 7: Granting Secret Access..."
PROJECT_NUMBER=$(gcloud projects describe $PROJECT_ID --format="value(projectNumber)")
gcloud secrets add-iam-policy-binding db-password \
  --member="serviceAccount:$PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"

gcloud secrets add-iam-policy-binding keycloak-admin-password \
  --member="serviceAccount:$PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"

gcloud secrets add-iam-policy-binding email-password \
  --member="serviceAccount:$PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"

echo ""
echo "🐳 Step 8: Building and Deploying Keycloak..."
SQL_CONNECTION_NAME=$(gcloud sql instances describe tickets-db --format="value(connectionName)")

cd keycloak
gcloud builds submit --tag gcr.io/$PROJECT_ID/keycloak:latest .

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

KEYCLOAK_URL=$(gcloud run services describe keycloak --region=$REGION --format="value(status.url)")
echo "✅ Keycloak deployed at: $KEYCLOAK_URL"

cd ..

echo ""
echo "⚙️ Step 9: Building and Deploying Backend..."
cd backend
gcloud builds submit --tag gcr.io/$PROJECT_ID/backend:latest .

gcloud run deploy backend \
  --image=gcr.io/$PROJECT_ID/backend:latest \
  --platform=managed \
  --region=$REGION \
  --allow-unauthenticated \
  --add-cloudsql-instances=$SQL_CONNECTION_NAME \
  --set-env-vars="SPRING_DATASOURCE_URL=jdbc:postgresql:///tickets_db?cloudSqlInstance=$SQL_CONNECTION_NAME&socketFactory=com.google.cloud.sql.postgres.SocketFactory&user=ticketuser,KEYCLOAK_ISSUER_URI=$KEYCLOAK_URL/realms/event-ticket-platform,KEYCLOAK_ADMIN_URL=$KEYCLOAK_URL,KEYCLOAK_REALM=event-ticket-platform,KEYCLOAK_CLIENT_ID=event-ticket-client,KEYCLOAK_ADMIN_USERNAME=admin,SPRING_MAIL_HOST=smtp.gmail.com,SPRING_MAIL_PORT=587,SPRING_MAIL_USERNAME=$SMTP_EMAIL" \
  --update-secrets="SPRING_DATASOURCE_PASSWORD=db-password:latest,KEYCLOAK_CLIENT_SECRET=keycloak-client-secret:latest,KEYCLOAK_ADMIN_PASSWORD=keycloak-admin-password:latest,SPRING_MAIL_PASSWORD=email-password:latest" \
  --memory=2Gi \
  --cpu=2 \
  --min-instances=0 \
  --max-instances=100 \
  --timeout=300 \
  --port=8080

BACKEND_URL=$(gcloud run services describe backend --region=$REGION --format="value(status.url)")
echo "✅ Backend deployed at: $BACKEND_URL"

cd ..

echo ""
echo "🌐 Step 10: Deploying Frontend..."
BUCKET_NAME="event-tickets-frontend-$PROJECT_ID"
gsutil mb -l $REGION gs://$BUCKET_NAME
gsutil iam ch allUsers:objectViewer gs://$BUCKET_NAME
gsutil web set -m index.html -e index.html gs://$BUCKET_NAME

cd frontend
cat > .env.production << EOF
VITE_API_URL=$BACKEND_URL
VITE_KEYCLOAK_URL=$KEYCLOAK_URL
VITE_KEYCLOAK_REALM=event-ticket-platform
VITE_KEYCLOAK_CLIENT_ID=event-ticket-client
EOF

npm install
npm run build
gsutil -m rsync -r -d dist gs://$BUCKET_NAME

cd ..

echo ""
echo "✅ Deployment Complete!"
echo "======================="
echo ""
echo "📝 Save these credentials:"
echo ""
echo "Keycloak Admin Console:"
echo "  URL: $KEYCLOAK_URL"
echo "  Username: admin"
echo "  Password: $KEYCLOAK_ADMIN_PASSWORD"
echo ""
echo "Backend API:"
echo "  URL: $BACKEND_URL"
echo ""
echo "Frontend:"
echo "  Bucket: gs://$BUCKET_NAME"
echo "  URL: https://storage.googleapis.com/$BUCKET_NAME/index.html"
echo ""
echo "Database:"
echo "  Instance: tickets-db"
echo "  Connection: $SQL_CONNECTION_NAME"
echo "  Password: (stored in Secret Manager: db-password)"
echo ""
echo "📋 Next Steps:"
echo "1. Configure Keycloak realm 'event-ticket-platform'"
echo "2. Create Keycloak client 'event-ticket-client' and save the secret"
echo "3. Store client secret: echo -n 'SECRET' | gcloud secrets create keycloak-client-secret --data-file=-"
echo "4. Set up domain and SSL (see GOOGLE_CLOUD_DEPLOYMENT.md)"
echo "5. Update FRONTEND_URL in backend: gcloud run services update backend --set-env-vars=FRONTEND_URL=https://yourdomain.com"
echo ""
echo "📖 For detailed instructions, see GOOGLE_CLOUD_DEPLOYMENT.md"

# Save credentials to file
cat > gcp-deployment-info.txt << EOF
Event Ticket Platform - GCP Deployment Information
===================================================

Project ID: $PROJECT_ID
Region: $REGION
Deployed: $(date)

Keycloak:
  URL: $KEYCLOAK_URL
  Admin Username: admin
  Admin Password: $KEYCLOAK_ADMIN_PASSWORD

Backend:
  URL: $BACKEND_URL

Frontend:
  Bucket: $BUCKET_NAME
  Temporary URL: https://storage.googleapis.com/$BUCKET_NAME/index.html

Database:
  Instance: tickets-db
  Connection Name: $SQL_CONNECTION_NAME
  Database: tickets_db
  User: ticketuser

Secrets (stored in Secret Manager):
  - db-password
  - keycloak-admin-password
  - email-password
  - keycloak-client-secret (create after Keycloak setup)

Next Steps:
1. Configure Keycloak at $KEYCLOAK_URL
2. Create client 'event-ticket-client'
3. Store client secret in Secret Manager
4. Set up custom domain and SSL
5. Update CORS and redirect URIs

EOF

echo ""
echo "💾 Credentials saved to: gcp-deployment-info.txt"
echo "⚠️  Keep this file secure and do not commit to Git!"
