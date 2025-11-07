FROM quay.io/keycloak/keycloak:26.0.0

# Set environment variables
ENV KC_DB=postgres
ENV KC_HTTP_ENABLED=true
ENV KC_HOSTNAME_STRICT=false
ENV KC_PROXY=edge

# Build optimized Keycloak
RUN /opt/keycloak/bin/kc.sh build

# Set the entry point
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start", "--optimized"]
