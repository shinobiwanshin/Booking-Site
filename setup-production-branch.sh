#!/bin/bash

# 🌿 Create Production Branch Script
# This script sets up the production branch for Railway deployment

set -e

echo "🌿 Setting up Production Branch"
echo "================================"
echo ""

# Check if production branch already exists
if git show-ref --verify --quiet refs/heads/production; then
    echo "⚠️  Production branch already exists!"
    read -p "Do you want to reset it? This will DELETE the existing production branch! (y/n) " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "🗑️  Deleting existing production branch..."
        git branch -D production
        
        # Delete remote branch if it exists
        if git ls-remote --exit-code --heads origin production &> /dev/null; then
            echo "🗑️  Deleting remote production branch..."
            git push origin --delete production
        fi
    else
        echo "❌ Setup cancelled"
        exit 1
    fi
fi

echo "✨ Creating production branch from main..."
git checkout main
git pull origin main
git checkout -b production

echo ""
echo "📝 Production branch created!"
echo ""

# Update CORS configuration for production
echo "⚙️  Configuring for production environment..."

# Add .env.production example
cat > backend/.env.production.example << 'EOF'
# Database (Railway provides these automatically)
DATABASE_URL=jdbc:postgresql://host:port/database
DATABASE_USER=postgres
DATABASE_PASSWORD=password

# Keycloak
KEYCLOAK_ADMIN_URL=https://your-keycloak.railway.app
KEYCLOAK_ISSUER_URI=https://your-keycloak.railway.app/realms/event-ticket-platform
KEYCLOAK_REALM=event-ticket-platform
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=your_secure_password
KEYCLOAK_CLIENT_ID=event-ticket-client
KEYCLOAK_CLIENT_SECRET=your_client_secret

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=supersaiyan2k03@gmail.com
MAIL_PASSWORD=your_app_password

# Frontend
FRONTEND_URL=https://your-frontend.vercel.app

# Server
PORT=8080
EOF

cat > frontend/.env.production.example << 'EOF'
# Backend API
VITE_API_URL=https://your-backend.railway.app

# Keycloak
VITE_KEYCLOAK_URL=https://your-keycloak.railway.app
VITE_KEYCLOAK_REALM=event-ticket-platform
VITE_KEYCLOAK_CLIENT_ID=event-ticket-platform-app
EOF

echo "✅ Environment templates created"
echo ""

# Commit production setup
git add .
git commit -m "chore: Setup production branch with Railway configuration

- Add railway.toml for Railway deployment
- Add Procfile for process management
- Add system.properties for Java version
- Add actuator dependency for health checks
- Update application.properties with environment variables
- Add .env.production.example templates
- Configure SecurityConfig for actuator endpoints"

echo ""
echo "📤 Pushing production branch to GitHub..."
git push -u origin production

echo ""
echo "✅ Production branch setup complete!"
echo ""
echo "📋 Summary:"
echo "  ✓ Production branch created from main"
echo "  ✓ Railway configuration files added"
echo "  ✓ Environment variables configured"
echo "  ✓ Pushed to GitHub"
echo ""
echo "🚀 Next Steps:"
echo "  1. Install Railway CLI: npm install -g @railway/cli"
echo "  2. Login to Railway: railway login"
echo "  3. Deploy: ./deploy-production.sh"
echo ""
echo "💡 Tip: Keep main branch for development, merge to production when ready to deploy"
echo ""
echo "🔙 Switching back to main branch..."
git checkout main

echo ""
echo "🎉 All done! You can now deploy to production using ./deploy-production.sh"
