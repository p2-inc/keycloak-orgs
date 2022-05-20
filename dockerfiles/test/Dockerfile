################################################################################
ARG KEYCLOAK_VERSION=18.0.0
ARG IMAGE=quay.io/keycloak/keycloak:$KEYCLOAK_VERSION
ARG KEYCLOAK_INSTALL_BASE=/opt/keycloak
ARG PLUGIN_JAR_DIR=$KEYCLOAK_INSTALL_BASE/providers

################################################################################
FROM $IMAGE as plugin-keycloak-orgs
################################################################################
ARG KEYCLOAK_VERSION
ARG IMAGE
ARG KEYCLOAK_INSTALL_BASE
ARG PLUGIN_JAR_DIR

USER root

COPY . /project

RUN --mount=type=cache,target=/var/cache/dnf \
    microdnf install -y git which

RUN --mount=type=cache,target=/root/.m2 \
    set -x && \
    cd /project &&  \
    sed -i '/<dependency> <!-- if you.re getting a missing/,/<\/dependency/d' pom.xml && \
    ./mvnw -version && \
    ./mvnw clean package -Dmaven.test.skip -Pdistribution && \
    ./mvnw install dependency:copy-dependencies -Dmaven.test.skip

################################################################################
FROM $IMAGE
################################################################################
ARG KEYCLOAK_VERSION
ARG IMAGE
ARG KEYCLOAK_INSTALL_BASE
ARG PLUGIN_JAR_DIR

COPY --from=plugin-keycloak-orgs /project/target/keycloak-orgs*.jar $PLUGIN_JAR_DIR/keycloak-orgs.jar

RUN cd $KEYCLOAK_INSTALL_BASE && ./bin/kc.sh build