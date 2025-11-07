FROM quay.io/keycloak/keycloak:23.0.0 AS builder

# Enable health and metrics endpoints
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

# Database configuration
ENV KC_DB=postgres

# Build optimized Keycloak for faster startup
RUN /opt/keycloak/bin/kc.sh build

# ==================== Production Image ====================
FROM quay.io/keycloak/keycloak:23.0.0

# Copy optimized Keycloak from builder
COPY --from=builder /opt/keycloak/ /opt/keycloak/

# Production environment variables
ENV KC_HOSTNAME_STRICT=false
ENV KC_PROXY=edge
ENV KC_HTTP_ENABLED=true
ENV KC_HTTP_PORT=8080

# For Google Cloud Run
ENV KC_HOSTNAME_STRICT_HTTPS=false

WORKDIR /opt/keycloak

# Expose port
EXPOSE 8080

# Run Keycloak in production mode
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
