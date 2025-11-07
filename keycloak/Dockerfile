FROM quay.io/keycloak/keycloak:23.0.0 as builder

# Enable health and metrics support
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

# Configure a database vendor
ENV KC_DB=postgres

WORKDIR /opt/keycloak

# Build Keycloak with production settings
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:23.0.0
COPY --from=builder /opt/keycloak/ /opt/keycloak/

# Set runtime environment to match build configuration
ENV KC_DB=postgres
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

WORKDIR /opt/keycloak

# Expose port (Render uses 10000)
EXPOSE 10000

# Use start-dev to avoid hostname restrictions while keeping optimized build benefits
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev", "--http-port=10000"]
