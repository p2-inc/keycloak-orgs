package io.phasetwo.service.util;

import com.google.common.base.Strings;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.keycloak.representations.idm.ClientRepresentation;

/**
 * Builds the SAML client that backs IdP-initiated SSO for an organization's identity provider.
 *
 * <p>Organization admins with the org-scoped {@code manage-identity-providers} role may create this
 * client, so the request is accepted only in the exact shape the IdP wizard sends, and the stored
 * client is built server-side from the allowed fields. Nothing else from the request reaches the
 * client model (no OIDC flows, service accounts, redirect URIs, roles, or protocol mappers).
 */
public final class IdpInitiatedClients {

  private IdpInitiatedClients() {}

  public static final String SAML_PROTOCOL = "saml";
  public static final String CLIENT_ID_PREFIX = "idp-initiated-";

  public static final String ATTR_SSO_URL_NAME = "saml_idp_initiated_sso_url_name";
  public static final String ATTR_SSO_RELAY_STATE = "saml_idp_initiated_sso_relay_state";
  public static final String ATTR_FORCE_POST_BINDING = "saml.force.post.binding";
  public static final String ATTR_ACS_REDIRECT = "saml_assertion_consumer_url_redirect";
  public static final String ATTR_ACS_POST = "saml_assertion_consumer_url_post";

  /** Alias of the identity provider this client is paired with. Sent by the caller. */
  public static final String ATTR_IDP_ALIAS = "phasetwo.idp-initiated.alias";

  /** Organization that created the client. Always set server-side, never accepted from input. */
  public static final String ATTR_ORG_ID = "phasetwo.idp-initiated.org-id";

  static final Set<String> ALLOWED_FIELDS =
      Set.of("protocol", "clientId", "name", "description", "attributes");

  static final Set<String> ALLOWED_ATTRIBUTES =
      Set.of(
          ATTR_SSO_URL_NAME,
          ATTR_SSO_RELAY_STATE,
          ATTR_FORCE_POST_BINDING,
          ATTR_ACS_REDIRECT,
          ATTR_ACS_POST,
          ATTR_IDP_ALIAS);

  /** Thrown when the request does not match the IdP-initiated client shape. */
  public static class InvalidClientException extends RuntimeException {
    public InvalidClientException(String message) {
      super(message);
    }
  }

  /**
   * Validates the request body and builds the client representation to create.
   *
   * @param body the raw JSON request body
   * @param orgId the organization the client is created for, recorded on the client
   * @return a client representation containing only the allowed fields
   * @throws InvalidClientException if the body is not an IdP-initiated SAML client
   */
  public static ClientRepresentation toClientRepresentation(
      Map<String, Object> body, String orgId) {
    if (body == null) {
      throw new InvalidClientException("Request body is required");
    }

    Set<String> unknownFields = new TreeSet<>(body.keySet());
    unknownFields.removeAll(ALLOWED_FIELDS);
    if (!unknownFields.isEmpty()) {
      throw new InvalidClientException("Unsupported client fields: " + unknownFields);
    }

    String protocol = optionalString(body, "protocol");
    if (!SAML_PROTOCOL.equals(protocol)) {
      throw new InvalidClientException("protocol must be saml");
    }

    String clientId = optionalString(body, "clientId");
    if (Strings.isNullOrEmpty(clientId)
        || !clientId.startsWith(CLIENT_ID_PREFIX)
        || clientId.length() == CLIENT_ID_PREFIX.length()) {
      throw new InvalidClientException("clientId must start with " + CLIENT_ID_PREFIX);
    }

    Map<String, String> attributes = attributes(body.get("attributes"));

    String alias = attributes.get(ATTR_IDP_ALIAS);
    if (Strings.isNullOrEmpty(alias) || !clientId.equals(CLIENT_ID_PREFIX + alias)) {
      throw new InvalidClientException(
          String.format(
              "%s is required and clientId must be %s{alias}", ATTR_IDP_ALIAS, CLIENT_ID_PREFIX));
    }
    if (!clientId.equals(attributes.get(ATTR_SSO_URL_NAME))) {
      throw new InvalidClientException(ATTR_SSO_URL_NAME + " must equal clientId");
    }
    String forcePost = attributes.get(ATTR_FORCE_POST_BINDING);
    if (forcePost != null && !"true".equals(forcePost) && !"false".equals(forcePost)) {
      throw new InvalidClientException(ATTR_FORCE_POST_BINDING + " must be true or false");
    }
    for (String key : List.of(ATTR_ACS_REDIRECT, ATTR_ACS_POST)) {
      String url = attributes.get(key);
      if (!Strings.isNullOrEmpty(url) && !isHttpUrl(url)) {
        throw new InvalidClientException(key + " must be an absolute http(s) URL");
      }
    }

    attributes.put(ATTR_ORG_ID, orgId);

    ClientRepresentation client = new ClientRepresentation();
    client.setProtocol(SAML_PROTOCOL);
    client.setClientId(clientId);
    client.setName(optionalString(body, "name"));
    client.setDescription(optionalString(body, "description"));
    client.setAttributes(attributes);
    // Pin the settings that would broaden the client rather than relying on defaults.
    client.setEnabled(true);
    client.setPublicClient(false);
    client.setBearerOnly(false);
    client.setStandardFlowEnabled(true);
    client.setImplicitFlowEnabled(false);
    client.setDirectAccessGrantsEnabled(false);
    client.setServiceAccountsEnabled(false);
    client.setAuthorizationServicesEnabled(false);
    client.setRedirectUris(List.of());
    client.setWebOrigins(List.of());
    return client;
  }

  private static String optionalString(Map<String, Object> body, String field) {
    Object value = body.get(field);
    if (value == null) {
      return null;
    }
    if (!(value instanceof String)) {
      throw new InvalidClientException(field + " must be a string");
    }
    return (String) value;
  }

  private static Map<String, String> attributes(Object value) {
    if (!(value instanceof Map<?, ?> raw)) {
      throw new InvalidClientException("attributes are required");
    }
    Map<String, String> attributes = new HashMap<>();
    Set<String> unknown = new TreeSet<>();
    for (Map.Entry<?, ?> entry : raw.entrySet()) {
      String key = String.valueOf(entry.getKey());
      if (!ALLOWED_ATTRIBUTES.contains(key)) {
        unknown.add(key);
        continue;
      }
      if (entry.getValue() != null && !(entry.getValue() instanceof String)) {
        throw new InvalidClientException("attribute " + key + " must be a string");
      }
      if (entry.getValue() != null) {
        attributes.put(key, (String) entry.getValue());
      }
    }
    if (!unknown.isEmpty()) {
      throw new InvalidClientException("Unsupported client attributes: " + unknown);
    }
    return attributes;
  }

  private static boolean isHttpUrl(String value) {
    try {
      URI uri = URI.create(value);
      String scheme = uri.getScheme();
      return uri.isAbsolute()
          && uri.getHost() != null
          && ("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme));
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
