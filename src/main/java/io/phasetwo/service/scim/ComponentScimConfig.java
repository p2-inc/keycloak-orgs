package io.phasetwo.service.scim;

import static org.keycloak.provider.ProviderConfigProperty.*;

import fi.metatavu.keycloak.scim.server.config.ScimConfig.AuthenticationMode;
import fi.metatavu.keycloak.scim.server.organization.OrganizationScimConfig;
import java.util.List;
import org.keycloak.component.ComponentModel;
import org.keycloak.provider.ProviderConfigProperty;

public final class ComponentScimConfig implements OrganizationScimConfig {

  public static final String ENABLED_PROPERTY = "ENABLED";
  public static final String ORGANIZATION_ID = "ORGANIZATION_ID";

  private static final List<ProviderConfigProperty> PROPERTIES =
      List.of(
          new ProviderConfigProperty(
              ORGANIZATION_ID,
              "Organization ID",
              "Organization ID of the org this config represents. Both the component ID and organization ID will be the same",
              STRING_TYPE,
              null),
          new ProviderConfigProperty(
              SCIM_AUTHENTICATION_MODE,
              "Authentication Mode",
              "Authentication mode for SCIM API. Possible values are KEYCLOAK and EXTERNAL. If the value is not set the server will respond unauthorzed for all requests.",
              LIST_TYPE,
              "KEYCLOAK",
              "KEYCLOAK",
              "EXTERNAL"),
          new ProviderConfigProperty(
              SCIM_EXTERNAL_ISSUER,
              "External Issuer",
              "Issuer for the external authentication. This is used to validate the JWT token.",
              STRING_TYPE,
              null),
          new ProviderConfigProperty(
              SCIM_EXTERNAL_AUDIENCE,
              "External Audience",
              "Audience for the external authentication. This is used to validate the JWT token.",
              STRING_TYPE,
              null),
          new ProviderConfigProperty(
              SCIM_EXTERNAL_JWKS_URI,
              "External JWKS URI",
              "JWKS URI for the external authentication. This is used to validate the JWT token.",
              STRING_TYPE,
              null),
          new ProviderConfigProperty(
              SCIM_EXTERNAL_SHARED_SECRET,
              "External Shared Secret",
              "Shared secret value used for request authentication/validation.",
              STRING_TYPE,
              null),
          new ProviderConfigProperty(
              SCIM_LINK_IDP,
              "Link to Organization IdP",
              "Enables support for linking organization identity provider with user.",
              BOOLEAN_TYPE,
              "false"),
          new ProviderConfigProperty(
              SCIM_EMAIL_AS_USERNAME,
              "Use Email as Username",
              "Forces server to use email as username instead of actual username. When this setting is enabled username will be unaffected by any update operations.",
              BOOLEAN_TYPE,
              "false"),
          new ProviderConfigProperty(
              SCIM_BASIC_AUTH_USERNAME,
              "Basic Auth Username",
              "Username for HTTP Basic authentication.",
              STRING_TYPE,
              null),
          new ProviderConfigProperty(
              SCIM_BASIC_AUTH_PASSWORD,
              "Basic Auth Password",
              "Password hash in PHC String Format for HTTP Basic authentication.",
              STRING_TYPE,
              null));

  public static List<ProviderConfigProperty> getConfigProperties() {
    return PROPERTIES;
  }

  private final ComponentModel model;

  public ComponentScimConfig(ComponentModel model) {
    this.model = model;
  }

  public String getId() {
    return model.getId();
  }

  public void setId(String id) {
    model.setId(id);
  }

  public String getOrganizationId() {
    return model.get(ORGANIZATION_ID);
  }

  public void setOrganizationId(String organizationId) {
    model.put(ORGANIZATION_ID, organizationId);
  }

  @Override
  public boolean isEnabled() {
    return model.get(ENABLED_PROPERTY, true);
  }

  @Override
  public AuthenticationMode getAuthenticationMode() {
    String value = model.get(SCIM_AUTHENTICATION_MODE);
    if (value == null || value.isEmpty()) {
      return null;
    }
    return AuthenticationMode.valueOf(value);
  }

  @Override
  public String getExternalIssuer() {
    return model.get(SCIM_EXTERNAL_ISSUER);
  }

  public void setExternalIssuer(String externalIssuer) {
    model.put(SCIM_EXTERNAL_ISSUER, externalIssuer);
  }

  @Override
  public String getExternalJwksUri() {
    return model.get(SCIM_EXTERNAL_JWKS_URI);
  }

  public void setExternalJwksUri(String externalJwksUri) {
    model.put(SCIM_EXTERNAL_JWKS_URI, externalJwksUri);
  }

  @Override
  public String getExternalAudience() {
    return model.get(SCIM_EXTERNAL_AUDIENCE);
  }

  public void setExternalAudience(String externalAudience) {
    model.put(SCIM_EXTERNAL_AUDIENCE, externalAudience);
  }

  @Override
  public String getSharedSecret() {
    return model.get(SCIM_EXTERNAL_SHARED_SECRET);
  }

  public void setSharedSecret(String sharedSecret) {
    model.put(SCIM_EXTERNAL_SHARED_SECRET, sharedSecret);
  }

  @Override
  public boolean getLinkIdp() {
    return model.get(SCIM_LINK_IDP, false);
  }

  public void setLinkIdp(boolean linkIdp) {
    model.put(SCIM_LINK_IDP, linkIdp);
  }

  @Override
  public boolean getEmailAsUsername() {
    return model.get(SCIM_EMAIL_AS_USERNAME, false);
  }

  public void setEmailAsUsername(boolean emailAsUsername) {
    model.put(SCIM_EMAIL_AS_USERNAME, emailAsUsername);
  }

  @Override
  public String getBasicAuthUsername() {
    return model.get(SCIM_BASIC_AUTH_USERNAME);
  }

  public void setBasicAuthUsername(String basicAuthUsername) {
    model.put(SCIM_BASIC_AUTH_USERNAME, basicAuthUsername);
  }

  @Override
  public String getBasicAuthPassword() {
    return model.get(SCIM_BASIC_AUTH_PASSWORD);
  }

  public void setBasicAuthPassword(String basicAuthPassword) {
    model.put(SCIM_BASIC_AUTH_PASSWORD, basicAuthPassword);
  }

  public ComponentModel getModel() {
    return model;
  }
}
