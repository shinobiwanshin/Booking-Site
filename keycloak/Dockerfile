FROM quay.io/keycloak/keycloak:23.0.0

WORKDIR /opt/keycloak

# Expose port
EXPOSE 8080

# Run Keycloak in production mode (non-optimized to allow runtime config)
# All configs will come from environment variables at runtime
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start"]
