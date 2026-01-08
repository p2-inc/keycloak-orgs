ARG PHASETWO_IMAGE=quay.io/phasetwo/phasetwo-keycloak:latest

FROM ${PHASETWO_IMAGE} AS builder

COPY target/phasetwo-idp-wizard*.jar /tmp/phasetwo-idp-wizard.jar

RUN set -e; \
  target=""; \
  for f in /opt/keycloak/providers/io.phasetwo-phasetwo-idp-wizard-*.jar; do \
    if [ -f "$f" ]; then target="$f"; break; fi; \
  done; \
  if [ -z "$target" ]; then \
    echo "No existing idp-wizard jar found in base image." >&2; \
    exit 1; \
  fi; \
  cp /tmp/phasetwo-idp-wizard.jar "$target"; \
  rm -f /tmp/phasetwo-idp-wizard.jar

RUN /opt/keycloak/bin/kc.sh build

FROM ${PHASETWO_IMAGE}

ENV KC_THEME_ADMIN=phasetwo.v2

COPY --from=builder /opt/keycloak/lib/quarkus/ /opt/keycloak/lib/quarkus/
COPY --from=builder /opt/keycloak/providers/ /opt/keycloak/providers/

WORKDIR /opt/keycloak
