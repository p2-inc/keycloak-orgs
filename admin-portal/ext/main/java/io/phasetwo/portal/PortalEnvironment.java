package io.phasetwo.portal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Generated;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "name",
  "displayName",
  "logoUrl",
  "faviconUrl",
  "appiconUrl",
  "realm",
  "locale",
  "authServerUrl",
  "baseUrl",
  "resourceUrl",
  "refererUrl",
  "isRunningAsTheme",
  "supportedLocales",
  "features"
})
@Generated("jsonschema2pojo")
public class PortalEnvironment {

  @JsonProperty("name")
  private String name;

  @JsonProperty("displayName")
  private String displayName;

  @JsonProperty("logoUrl")
  private String logoUrl;

  @JsonProperty("faviconUrl")
  private String faviconUrl;

  @JsonProperty("appiconUrl")
  private String appiconUrl;

  @JsonProperty("realm")
  private String realm;

  @JsonProperty("locale")
  private String locale;

  @JsonProperty("authServerUrl")
  private String authServerUrl;

  @JsonProperty("baseUrl")
  private String baseUrl;

  @JsonProperty("resourceUrl")
  private String resourceUrl;

  @JsonProperty("refererUrl")
  private String refererUrl;

  @JsonProperty("isRunningAsTheme")
  private Boolean isRunningAsTheme;

  @JsonProperty("supportedLocales")
  private Map<String, String> supportedLocales;

  @JsonProperty("features")
  private PortalFeatures features;

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  public PortalEnvironment name(String name) {
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

  public PortalEnvironment displayName(String displayName) {
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

  public PortalEnvironment logoUrl(String logoUrl) {
    this.logoUrl = logoUrl;
    return this;
  }

  @JsonProperty("faviconUrl")
  public String getFaviconUrl() {
    return faviconUrl;
  }

  @JsonProperty("faviconUrl")
  public void setFaviconUrl(String faviconUrl) {
    this.faviconUrl = faviconUrl;
  }

  public PortalEnvironment faviconUrl(String faviconUrl) {
    this.faviconUrl = faviconUrl;
    return this;
  }

  @JsonProperty("appiconUrl")
  public String getAppiconUrl() {
    return appiconUrl;
  }

  @JsonProperty("appiconUrl")
  public void setAppiconUrl(String appiconUrl) {
    this.appiconUrl = appiconUrl;
  }

  public PortalEnvironment appiconUrl(String appiconUrl) {
    this.appiconUrl = appiconUrl;
    return this;
  }

  @JsonProperty("realm")
  public String getRealm() {
    return realm;
  }

  @JsonProperty("realm")
  public void setRealm(String realm) {
    this.realm = realm;
  }

  public PortalEnvironment realm(String realm) {
    this.realm = realm;
    return this;
  }

  @JsonProperty("locale")
  public String getLocale() {
    return locale;
  }

  @JsonProperty("locale")
  public void setLocale(String locale) {
    this.locale = locale;
  }

  public PortalEnvironment locale(String locale) {
    this.locale = locale;
    return this;
  }

  @JsonProperty("authServerUrl")
  public String getAuthServerUrl() {
    return authServerUrl;
  }

  @JsonProperty("authServerUrl")
  public void setAuthServerUrl(String authServerUrl) {
    this.authServerUrl = authServerUrl;
  }

  public PortalEnvironment authServerUrl(String authServerUrl) {
    this.authServerUrl = authServerUrl;
    return this;
  }

  @JsonProperty("baseUrl")
  public String getBaseUrl() {
    return baseUrl;
  }

  @JsonProperty("baseUrl")
  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public PortalEnvironment baseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  @JsonProperty("resourceUrl")
  public String getResourceUrl() {
    return resourceUrl;
  }

  @JsonProperty("resourceUrl")
  public void setResourceUrl(String resourceUrl) {
    this.resourceUrl = resourceUrl;
  }

  public PortalEnvironment resourceUrl(String resourceUrl) {
    this.resourceUrl = resourceUrl;
    return this;
  }

  @JsonProperty("refererUrl")
  public String getRefererUrl() {
    return refererUrl;
  }

  @JsonProperty("refererUrl")
  public void setRefererUrl(String refererUrl) {
    this.refererUrl = refererUrl;
  }

  public PortalEnvironment refererUrl(String refererUrl) {
    this.refererUrl = refererUrl;
    return this;
  }

  @JsonProperty("isRunningAsTheme")
  public Boolean getIsRunningAsTheme() {
    return isRunningAsTheme;
  }

  @JsonProperty("isRunningAsTheme")
  public void setIsRunningAsTheme(Boolean isRunningAsTheme) {
    this.isRunningAsTheme = isRunningAsTheme;
  }

  public PortalEnvironment isRunningAsTheme(Boolean isRunningAsTheme) {
    this.isRunningAsTheme = isRunningAsTheme;
    return this;
  }

  @JsonProperty("supportedLocales")
  public Map<String, String> getSupportedLocales() {
    return supportedLocales;
  }

  @JsonProperty("supportedLocales")
  public void setSupportedLocales(Map<String, String> supportedLocales) {
    this.supportedLocales = supportedLocales;
  }

  public PortalEnvironment supportedLocales(Map<String, String> supportedLocales) {
    this.supportedLocales = supportedLocales;
    return this;
  }

  @JsonProperty("features")
  public PortalFeatures getFeatures() {
    return features;
  }

  @JsonProperty("features")
  public void setFeatures(PortalFeatures features) {
    this.features = features;
  }

  public PortalEnvironment features(PortalFeatures features) {
    this.features = features;
    return this;
  }
}
