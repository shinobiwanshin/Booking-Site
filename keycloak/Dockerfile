FROM quay.io/keycloak/keycloak:23.0.0 AS builder

# Enable health and metrics endpoints
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

# Configure database type for build
ENV KC_DB=postgres

# Build optimized Keycloak for faster startup
RUN /opt/keycloak/bin/kc.sh build

# ==================== Production Image ====================
FROM quay.io/keycloak/keycloak:23.0.0

# Copy optimized Keycloak from builder
COPY --from=builder /opt/keycloak/ /opt/keycloak/

WORKDIR /opt/keycloak

# Expose port
EXPOSE 8080

# Run Keycloak in production mode with optimized build
# Database and other configs will come from environment variables
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start", "--optimized"]
