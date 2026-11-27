# Phase Two Keycloak image, optionally with the keycloak-orgs jar from this checkout.
# Put a built jar in provider/ (CI copies target/keycloak-orgs-*.jar there) and it replaces
# the bundled keycloak-orgs; with provider/ empty the image runs as published.
# The image is pre-built and its build metadata names the bundled jar's file, so the new jar
# is written over that file rather than added next to it.
ARG KC_IMAGE=quay.io/phasetwo/phasetwo-keycloak:26.8.2
FROM ${KC_IMAGE}
COPY provider/ /tmp/provider/
RUN set -e; new=$(ls /tmp/provider/*.jar 2>/dev/null | head -1 || true); \
    if [ -n "$new" ]; then \
      old=$(ls /opt/keycloak/providers/*keycloak-orgs*.jar | head -1); \
      echo "replacing $old with $new"; cp "$new" "$old"; \
    fi
