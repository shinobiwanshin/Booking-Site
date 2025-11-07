FROM quay.io/keycloak/keycloak:23.0.0

WORKDIR /opt/keycloak

# Expose port
EXPOSE 8080

# Use start-dev for Railway deployment (simpler, no build step required)
# For production with build step, use: build then start --optimized
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev"]
