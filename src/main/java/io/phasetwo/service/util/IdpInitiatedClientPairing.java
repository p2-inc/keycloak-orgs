package io.phasetwo.service.util;

import static io.phasetwo.service.Orgs.ORG_OWNER_CONFIG_KEY;

import com.google.common.base.Strings;
import java.util.Map;
import java.util.Optional;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.ClientModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.services.managers.ClientManager;
import org.keycloak.services.managers.RealmManager;

/**
 * Keeps an identity provider and its IdP-initiated SSO SAML client in sync: removing one removes
 * the other.
 *
 * <p>The client is paired with the identity provider named by its {@value #ATTR_IDP_ALIAS}
 * attribute. A client without that attribute is paired only by the naming convention: SAML
 * protocol, clientId {@value #CLIENT_ID_PREFIX}{alias}, and a non-empty {@value
 * #ATTR_SSO_URL_NAME}. When the client records the organization that created it ({@value
 * #ATTR_ORG_ID}), the pair is honored only while that organization owns the identity provider, so
 * one organization's deletion never removes another organization's object.
 *
 * <p>Each side's removal publishes the other side's removal event in the same session, so a session
 * attribute marks the object being removed and the second event is ignored.
 */
@JBossLog
public final class IdpInitiatedClientPairing {

  private IdpInitiatedClientPairing() {}

  public static final String CLIENT_ID_PREFIX = "idp-initiated-";
  public static final String ATTR_SSO_URL_NAME = "saml_idp_initiated_sso_url_name";
  public static final String ATTR_IDP_ALIAS = "phasetwo.idp-initiated.alias";
  public static final String ATTR_ORG_ID = "phasetwo.idp-initiated.org-id";

  private static final String SAML_PROTOCOL = "saml";
  private static final String GUARD_PREFIX = "phasetwo.idp-initiated.removing.";

  /** Removes the paired client after an identity provider is removed. */
  public static void identityProviderRemoved(
      KeycloakSession session, RealmModel realm, IdentityProviderModel idp) {
    if (session == null || realm == null || idp == null || Strings.isNullOrEmpty(idp.getAlias())) {
      return;
    }
    String alias = idp.getAlias();
    if (session.getAttribute(idpGuard(realm, alias)) != null) {
      return; // this removal was started by removing the paired client
    }

    ClientModel client = findPairedClient(session, realm, alias).orElse(null);
    if (client == null) {
      return;
    }
    if (!sameOrganization(client, idp)) {
      log.debugf(
          "Not removing client %s: its organization does not own identity provider %s",
          client.getClientId(), alias);
      return;
    }

    String guard = clientGuard(realm, client.getId());
    session.setAttribute(guard, Boolean.TRUE);
    try {
      log.infof(
          "Removing IdP-initiated client %s paired with removed identity provider %s in realm %s",
          client.getClientId(), alias, realm.getName());
      new ClientManager(new RealmManager(session)).removeClient(realm, client);
    } finally {
      session.removeAttribute(guard);
    }
  }

  /** Removes the paired identity provider after an IdP-initiated client is removed. */
  public static void clientRemoved(KeycloakSession session, ClientModel client) {
    if (session == null || client == null || client.getRealm() == null) {
      return;
    }
    RealmModel realm = client.getRealm();
    if (session.getAttribute(clientGuard(realm, client.getId())) != null) {
      return; // this removal was started by removing the paired identity provider
    }
    String alias = pairedAlias(client);
    if (alias == null) {
      return;
    }

    // session.identityProviders() is scoped to the context realm, which is not necessarily the
    // client's realm (for example when a realm is removed from the master realm's admin console).
    KeycloakContext context = session.getContext();
    RealmModel contextRealm = context.getRealm();
    boolean switchRealm = contextRealm == null || !contextRealm.getId().equals(realm.getId());
    String guard = idpGuard(realm, alias);
    try {
      if (switchRealm) {
        context.setRealm(realm);
      }
      IdentityProviderModel idp = session.identityProviders().getByAlias(alias);
      if (idp == null) {
        return; // already gone
      }
      if (!sameOrganization(client, idp)) {
        log.debugf(
            "Not removing identity provider %s: it is not owned by the organization of client %s",
            alias, client.getClientId());
        return;
      }
      session.setAttribute(guard, Boolean.TRUE);
      log.infof(
          "Removing identity provider %s paired with removed IdP-initiated client %s in realm %s",
          alias, client.getClientId(), realm.getName());
      session.identityProviders().remove(alias);
    } finally {
      session.removeAttribute(guard);
      if (switchRealm) {
        context.setRealm(contextRealm);
      }
    }
  }

  /**
   * @return the alias of the identity provider this client is paired with, or null if the client
   *     is not an IdP-initiated client
   */
  static String pairedAlias(ClientModel client) {
    String alias = client.getAttribute(ATTR_IDP_ALIAS);
    if (!Strings.isNullOrEmpty(alias)) {
      return alias;
    }
    String clientId = client.getClientId();
    if (isConventionClient(client)) {
      return clientId.substring(CLIENT_ID_PREFIX.length());
    }
    return null;
  }

  static Optional<ClientModel> findPairedClient(
      KeycloakSession session, RealmModel realm, String alias) {
    try {
      Optional<ClientModel> byAttribute =
          session
              .clients()
              .searchClientsByAttributes(realm, Map.of(ATTR_IDP_ALIAS, alias), 0, 10)
              .filter(c -> alias.equals(c.getAttribute(ATTR_IDP_ALIAS)))
              .findFirst();
      if (byAttribute.isPresent()) {
        return byAttribute;
      }
    } catch (ModelException e) {
      // client attribute search can be restricted with spi-client-jpa-searchable-attributes
      log.debugf("Cannot search clients by %s: %s", ATTR_IDP_ALIAS, e.getMessage());
    }

    ClientModel byName = realm.getClientByClientId(CLIENT_ID_PREFIX + alias);
    if (byName == null) {
      return Optional.empty();
    }
    String attributeAlias = byName.getAttribute(ATTR_IDP_ALIAS);
    if (!Strings.isNullOrEmpty(attributeAlias)) {
      // explicitly paired with something else (or found above)
      return alias.equals(attributeAlias) ? Optional.of(byName) : Optional.empty();
    }
    return isConventionClient(byName) ? Optional.of(byName) : Optional.empty();
  }

  private static boolean isConventionClient(ClientModel client) {
    String clientId = client.getClientId();
    return clientId != null
        && clientId.startsWith(CLIENT_ID_PREFIX)
        && clientId.length() > CLIENT_ID_PREFIX.length()
        && SAML_PROTOCOL.equals(client.getProtocol())
        && !Strings.isNullOrEmpty(client.getAttribute(ATTR_SSO_URL_NAME));
  }

  private static boolean sameOrganization(ClientModel client, IdentityProviderModel idp) {
    String orgId = client.getAttribute(ATTR_ORG_ID);
    if (Strings.isNullOrEmpty(orgId)) {
      return true;
    }
    return IdentityProviders.getAttributeMultivalued(idp.getConfig(), ORG_OWNER_CONFIG_KEY)
        .contains(orgId);
  }

  private static String idpGuard(RealmModel realm, String alias) {
    return GUARD_PREFIX + "idp." + realm.getId() + "." + alias;
  }

  private static String clientGuard(RealmModel realm, String clientId) {
    return GUARD_PREFIX + "client." + realm.getId() + "." + clientId;
  }
}
