#!/bin/bash

# Unset any environment variables that might conflict with Keycloak's hostname settings
unset KC_HOSTNAME
unset KC_HOSTNAME_URL
unset KC_HOSTNAME_ADMIN
unset KC_HOSTNAME_ADMIN_URL
unset HOSTNAME
unset RENDER_EXTERNAL_HOSTNAME
unset RENDER_EXTERNAL_URL

# Export admin credentials so Keycloak can create the admin user
export KEYCLOAK_ADMIN=${KEYCLOAK_ADMIN:-admin}
export KEYCLOAK_ADMIN_PASSWORD=${KEYCLOAK_ADMIN_PASSWORD:-SecureAdmin2024!}

# Start Keycloak in dev mode
exec /opt/keycloak/bin/kc.sh start-dev --http-port=10000
