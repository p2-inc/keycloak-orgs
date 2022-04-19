package io.phasetwo.wizard;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Optional;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

@JsonPropertyOrder({"apiMode", "enableGroupMapping", "enableLdap", "enableDashboard"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WizardConfig {

  public static String CONFIG_KEY(String property) {
    return String.format("_providerConfig.wizard.%s", property);
  }

  public static WizardConfig createFromAttributes(KeycloakSession session) {
    RealmModel realm = session.getContext().getRealm();
    WizardConfig config = new WizardConfig();
    Optional.ofNullable(realm.getAttribute(CONFIG_KEY("apiMode")))
        .ifPresent(a -> config.apiMode(a));
    Optional.ofNullable(realm.getAttribute(CONFIG_KEY("enableGroupMapping")))
        .ifPresent(a -> config.enableGroupMapping(a.toLowerCase().equals("true")));
    Optional.ofNullable(realm.getAttribute(CONFIG_KEY("enableLdap")))
        .ifPresent(a -> config.enableLdap(a.toLowerCase().equals("true")));
    Optional.ofNullable(realm.getAttribute(CONFIG_KEY("enableDashboard")))
        .ifPresent(a -> config.enableDashboard(a.toLowerCase().equals("true")));
    return config;
  }

  @JsonProperty("apiMode")
  private String apiMode = "onprem";

  @JsonProperty("enableGroupMapping")
  private boolean enableGroupMapping = true;

  @JsonProperty("enableLdap")
  private boolean enableLdap = true;

  @JsonProperty("enableDashboard")
  private boolean enableDashboard = true;

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
}
