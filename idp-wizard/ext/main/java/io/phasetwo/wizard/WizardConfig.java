package io.phasetwo.wizard;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import java.util.Optional;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

@JsonPropertyOrder({
  "domain",
  "name",
  "displayName",
  "logoUrl",
  "apiMode",
  "enableGroupMapping",
  "enableLdap",
  "enableDashboard",
  "emailAsUsername",
  "trustEmail",
  "usernameMapperImport",
  "appName"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WizardConfig {

  public static String CONFIG_KEY(String property) {
    return String.format("_providerConfig.wizard.%s", property);
  }

  public static WizardConfig createFromAttributes(KeycloakSession session) {
    RealmModel realm = session.getContext().getRealm();
    URI uri = session.getContext().getAuthServerUrl();
    WizardConfig config = new WizardConfig();
    Optional.ofNullable(realm.getAttribute(CONFIG_KEY("apiMode")))
        .ifPresent(a -> config.apiMode(a));
    Optional.ofNullable(realm.getAttribute(CONFIG_KEY("enableGroupMapping")))
        .ifPresent(a -> config.enableGroupMapping(a.toLowerCase().equals("true")));
    Optional.ofNullable(realm.getAttribute(CONFIG_KEY("enableLdap")))
        .ifPresent(a -> config.enableLdap(a.toLowerCase().equals("true")));
    Optional.ofNullable(realm.getAttribute(CONFIG_KEY("enableDashboard")))
        .ifPresent(a -> config.enableDashboard(a.toLowerCase().equals("true")));
    Optional.ofNullable(realm.getAttribute(CONFIG_KEY("emailAsUsername")))
        .ifPresent(a -> config.emailAsUsername(a.toLowerCase().equals("true")));
    Optional.ofNullable(realm.getAttribute(CONFIG_KEY("trustEmail")))
        .ifPresent(a -> config.trustEmail(a.toLowerCase().equals("true")));
    Optional.ofNullable(realm.getAttribute(CONFIG_KEY("usernameMapperImport")))
        .ifPresent(a -> config.usernameMapperImport(a.toLowerCase().equals("true")));
    Optional.ofNullable(
            realm.getAttribute(
                String.format("_providerConfig.assets.logo.url"))) // from keycloak-orgs override
        .ifPresent(a -> config.logoUrl(a));
    Optional.ofNullable(realm.getName()).ifPresent(a -> config.name(a));
    Optional.ofNullable(realm.getDisplayName()).ifPresent(a -> config.displayName(a));
    Optional.ofNullable(realm.getAttribute(CONFIG_KEY("appName")))
        .ifPresent(a -> config.appName(a));
    if (uri != null) Optional.ofNullable(uri.getHost()).ifPresent(a -> config.domain(a));
    return config;
  }

  @JsonProperty("domain")
  private String domain;

  @JsonProperty("name")
  private String name;

  @JsonProperty("displayName")
  private String displayName;

  @JsonProperty("logoUrl")
  private String logoUrl;

  @JsonProperty("apiMode")
  private String apiMode = "onprem";

  @JsonProperty("enableGroupMapping")
  private boolean enableGroupMapping = true;

  @JsonProperty("enableLdap")
  private boolean enableLdap = true;

  @JsonProperty("enableDashboard")
  private boolean enableDashboard = true;

  @JsonProperty("emailAsUsername")
  private boolean emailAsUsername = false;

  @JsonProperty("trustEmail")
  private boolean trustEmail = false;

  @JsonProperty("usernameMapperImport")
  private boolean usernameMapperImport = true;

  @JsonProperty("domain")
  public String getDomain() {
    return domain;
  }

  @JsonProperty("domain")
  public void setDomain(String domain) {
    this.domain = domain;
  }

  public WizardConfig domain(String domain) {
    this.domain = domain;
    return this;
  }

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  public WizardConfig name(String name) {
    this.name = name;
    return this;
  }

  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }

  @JsonProperty("displayName")
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public WizardConfig displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  @JsonProperty("logoUrl")
  public String getLogoUrl() {
    return logoUrl;
  }

  @JsonProperty("logoUrl")
  public void setLogoUrl(String logoUrl) {
    this.logoUrl = logoUrl;
  }

  public WizardConfig logoUrl(String logoUrl) {
    this.logoUrl = logoUrl;
    return this;
  }

  @JsonProperty("apiMode")
  public String getApiMode() {
    return apiMode;
  }

  @JsonProperty("apiMode")
  public void setApiMode(String apiMode) {
    this.apiMode = apiMode;
  }

  public WizardConfig apiMode(String apiMode) {
    this.apiMode = apiMode;
    return this;
  }

  @JsonProperty("enableGroupMapping")
  public boolean getEnableGroupMapping() {
    return enableGroupMapping;
  }

  @JsonProperty("enableGroupMapping")
  public void setEnableGroupMapping(boolean enableGroupMapping) {
    this.enableGroupMapping = enableGroupMapping;
  }

  public WizardConfig enableGroupMapping(boolean enableGroupMapping) {
    this.enableGroupMapping = enableGroupMapping;
    return this;
  }

  @JsonProperty("enableLdap")
  public boolean getEnableLdap() {
    return enableLdap;
  }

  @JsonProperty("enableLdap")
  public void setEnableLdap(boolean enableLdap) {
    this.enableLdap = enableLdap;
  }

  public WizardConfig enableLdap(boolean enableLdap) {
    this.enableLdap = enableLdap;
    return this;
  }

  @JsonProperty("enableDashboard")
  public boolean getEnableDashboard() {
    return enableDashboard;
  }

  @JsonProperty("enableDashboard")
  public void setEnableDashboard(boolean enableDashboard) {
    this.enableDashboard = enableDashboard;
  }

  public WizardConfig enableDashboard(boolean enableDashboard) {
    this.enableDashboard = enableDashboard;
    return this;
  }

  @JsonProperty("emailAsUsername")
  public boolean getEmailAsUsername() {
    return emailAsUsername;
  }

  @JsonProperty("emailAsUsername")
  public void setEmailAsUsername(boolean emailAsUsername) {
    this.emailAsUsername = emailAsUsername;
  }

  public WizardConfig emailAsUsername(boolean emailAsUsername) {
    this.emailAsUsername = emailAsUsername;
    return this;
  }

  @JsonProperty("trustEmail")
  public boolean getTrustEmail() {
    return trustEmail;
  }

  @JsonProperty("trustEmail")
  public void setTrustEmail(boolean trustEmail) {
    this.trustEmail = trustEmail;
  }

  public WizardConfig trustEmail(boolean trustEmail) {
    this.trustEmail = trustEmail;
    return this;
  }

  @JsonProperty("usernameMapperImport")
  public boolean getUsernameMapperImport() {
    return usernameMapperImport;
  }

  @JsonProperty("usernameMapperImport")
  public void setUsernameMapperImport(boolean usernameMapperImport) {
    this.usernameMapperImport = usernameMapperImport;
  }

  public WizardConfig usernameMapperImport(boolean usernameMapperImport) {
    this.usernameMapperImport = usernameMapperImport;
    return this;
  }

  @JsonProperty("appName")
  private String appName = "";

  @JsonProperty("appName")
  public String getAppName() {
    return appName;
  }

  @JsonProperty("appName")
  public void setAppName(String appName) {
    this.appName = appName;
  }

  public WizardConfig appName(String appName) {
    this.appName = appName;
    return this;
  }
}
