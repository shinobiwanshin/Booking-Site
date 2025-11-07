#!/bin/bash

# 🚀 Deploy to Production Script
# This script helps you deploy to Railway production environment

set -e  # Exit on error

echo "🚂 Railway Production Deployment Script"
echo "========================================"
echo ""

# Check if Railway CLI is installed
if ! command -v railway &> /dev/null; then
    echo "❌ Railway CLI is not installed!"
    echo "📦 Install it with: npm install -g @railway/cli"
    exit 1
fi

echo "✅ Railway CLI found"
echo ""

# Get current branch
CURRENT_BRANCH=$(git branch --show-current)
echo "📍 Current branch: $CURRENT_BRANCH"

# Ask for confirmation if not on production branch
if [ "$CURRENT_BRANCH" != "production" ]; then
    echo "⚠️  You are not on the production branch!"
    echo ""
    read -p "Do you want to switch to production and merge from $CURRENT_BRANCH? (y/n) " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        # Ensure we have latest changes
        echo "📥 Fetching latest changes..."
        git fetch origin
        
        # Switch to production branch
        echo "🌿 Switching to production branch..."
        git checkout production
        
        # Pull latest production
        echo "📥 Pulling latest production..."
        git pull origin production
        
        # Merge from previous branch
        echo "🔀 Merging from $CURRENT_BRANCH..."
        git merge $CURRENT_BRANCH -m "Merge $CURRENT_BRANCH into production for deployment"
        
        echo "✅ Merged successfully"
    else
        echo "❌ Deployment cancelled"
        exit 1
    fi
fi

echo ""
echo "🔍 Checking for uncommitted changes..."
if [[ -n $(git status -s) ]]; then
    echo "⚠️  You have uncommitted changes:"
    git status -s
    echo ""
    read -p "Do you want to commit these changes? (y/n) " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        read -p "Enter commit message: " COMMIT_MSG
        git add .
        git commit -m "$COMMIT_MSG"
        echo "✅ Changes committed"
    else
        echo "⚠️  Continuing with uncommitted changes..."
    fi
fi

echo ""
echo "📤 Pushing to production branch..."
git push origin production

echo ""
echo "🏗️  Building and deploying backend to Railway..."
cd backend

# Check if railway is linked
if [ ! -f "railway.json" ] && [ ! -f ".railway.json" ]; then
    echo "⚠️  Railway project not linked!"
    echo "🔗 Linking to Railway project..."
    railway link
fi

# Deploy to Railway
echo "🚀 Deploying to Railway..."
railway up

echo ""
echo "✅ Backend deployed successfully!"
echo ""

# Show deployment info
echo "📊 Deployment Information:"
railway status

echo ""
echo "📝 View logs with: railway logs"
echo "🌐 Open dashboard with: railway open"
echo ""

# Ask if user wants to deploy frontend
read -p "Do you want to deploy frontend to Vercel? (y/n) " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    cd ../frontend
    
    if ! command -v vercel &> /dev/null; then
        echo "❌ Vercel CLI is not installed!"
        echo "📦 Install it with: npm install -g vercel"
        exit 1
    fi
    
    echo "🚀 Deploying frontend to Vercel..."
    vercel --prod
    
    echo "✅ Frontend deployed successfully!"
fi

echo ""
echo "🎉 Deployment Complete!"
echo "========================"
echo ""
echo "📋 Next Steps:"
echo "1. Check backend health: curl https://your-backend-url.railway.app/actuator/health"
echo "2. View Railway logs: railway logs"
echo "3. Monitor in dashboard: railway open"
echo ""
echo "🔙 Switch back to main branch: git checkout main"
