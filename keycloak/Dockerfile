FROM quay.io/keycloak/keycloak:23.0.0

WORKDIR /opt/keycloak

# Expose port (Render uses 10000)
EXPOSE 10000

# Use start-dev for Render deployment
# Will listen on port specified by KC_HTTP_PORT env var
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev"]
