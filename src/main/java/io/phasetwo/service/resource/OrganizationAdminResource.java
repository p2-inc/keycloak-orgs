package io.phasetwo.service.resource;

import io.phasetwo.service.model.OrganizationProvider;
import jakarta.persistence.EntityManager;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.managers.AppAuthManager;

/** */
@JBossLog
public class OrganizationAdminResource extends AbstractAdminResource<OrganizationAdminAuth> {

  protected OrganizationProvider orgs;
  protected EntityManager em;

  protected OrganizationAdminResource(KeycloakSession session) {
    super(session);
    init();
  }

  protected OrganizationAdminResource(OrganizationAdminResource parent) {
    super(parent);
    init();
  }

  protected final Keycloak getKeycloakForUser() {
    String clientId = auth.getToken().getIssuedFor();
    String serverUrl = getServerUrl() + "auth";
    String token = AppAuthManager.extractAuthorizationHeaderToken(headers);
    log.debugf(
        "Creating Keycloak admin client from serverUrl=%s realm=%s client=%s token=%s",
        serverUrl, realm.getName(), clientId, token);
    return KeycloakBuilder.builder()
        .serverUrl(serverUrl)
        .realm(realm.getName())
        .clientId(clientId)
        .authorization(token)
        .build();
  }

  protected final Keycloak getKeycloakForAdmin() {
    String clientId = auth.getToken().getIssuedFor();
    String serverUrl = getServerUrl() + "auth";
    String token = AppAuthManager.extractAuthorizationHeaderToken(headers);
    log.debugf(
        "Creating Keycloak admin client from serverUrl=%s realm=%s client=%s token=%s",
        serverUrl, realm.getName(), clientId, token);
    return KeycloakBuilder.builder()
        .serverUrl(serverUrl)
        .realm(realm.getName())
        .clientId(clientId)
        .authorization(token)
        .build();
  }

  protected final String getServerUrl() {
    URI u = session.getContext().getUri().getRequestUri();
    try {
      return new URI(u.getScheme(), null, u.getHost(), u.getPort(), "/", null, null).toString();
    } catch (URISyntaxException e) {
      throw new IllegalStateException(e);
    }
  }

  protected final void init() {
    this.em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
    this.orgs = session.getProvider(OrganizationProvider.class);
  }
}
