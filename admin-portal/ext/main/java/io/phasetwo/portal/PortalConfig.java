package io.phasetwo.portal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import java.util.Optional;
import javax.annotation.Generated;
import org.keycloak.authentication.requiredactions.DeleteAccount;
import org.keycloak.common.Profile;
import org.keycloak.events.EventStoreProvider;
import org.keycloak.models.AccountRoles;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.Auth;
import org.keycloak.services.managers.AuthenticationManager;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "name",
  "displayName",
  "logoUrl",
  "faviconUrl",
  "profileEnabled",
  "registrationEmailAsUsername",
  "passwordUpdateAllowed",
  "twoFactorUpdateAllowed",
  "totpConfigured",
  "passwordlessUpdateAllowed",
  "deviceActivityEnabled",
  "linkedAccountsEnabled",
  "eventsEnabled",
  "editUsernameAllowed",
  "internationalizationEnabled",
  "resourcesEnabled",
  "viewGroupsEnabled",
  "deleteAccountAllowed",
  "updateEmailFeatureEnabled",
  "updateEmailActionEnabled",
  "organizationsEnabled",
  "orgDetailsEnabled",
  "orgMembersEnabled",
  "orgInvitationsEnabled",
  "orgDomainsEnabled",
  "orgSsoEnabled",
  "orgEventsEnabled"
})
@Generated("jsonschema2pojo")
public class PortalConfig {

  public static String CONFIG_KEY(String property) {
    return String.format("_providerConfig.portal.%s", property);
  }

  public static boolean CONFIG_ENABLED(RealmModel realm, String property, boolean defaultValue) {
    String v = realm.getAttribute(CONFIG_KEY(property));
    if (v == null) return defaultValue;
    return ("true".equals(v));
  }

  public static PortalConfig createFromAttributes(KeycloakSession session) {
    RealmModel realm = session.getContext().getRealm();
    Auth auth = null;
    AppAuthManager authManager = new AppAuthManager();
    AuthenticationManager.AuthResult authResult =
        authManager.authenticateIdentityCookie(session, realm);
    if (authResult != null) {
      auth =
          new Auth(
              realm,
              authResult.getToken(),
              authResult.getUser(),
              session.getContext().getClient(),
              authResult.getSession(),
              true);
    }
    UserModel user = null;
    boolean isTotpConfigured = false;
    boolean deleteAccountAllowed = false;
    boolean isViewGroupsEnabled = false;
    if (auth != null) {
      user = auth.getUser();
      isTotpConfigured = user.credentialManager().isConfiguredFor(realm.getOTPPolicy().getType());
      RoleModel deleteAccountRole =
          realm
              .getClientByClientId(Constants.ACCOUNT_MANAGEMENT_CLIENT_ID)
              .getRole(AccountRoles.DELETE_ACCOUNT);
      deleteAccountAllowed =
          deleteAccountRole != null
              && user.hasRole(deleteAccountRole)
              && realm.getRequiredActionProviderByAlias(DeleteAccount.PROVIDER_ID).isEnabled();
      RoleModel viewGrouRole =
          realm
              .getClientByClientId(Constants.ACCOUNT_MANAGEMENT_CLIENT_ID)
              .getRole(AccountRoles.VIEW_GROUPS);
      isViewGroupsEnabled = viewGrouRole != null && user.hasRole(viewGrouRole);
    }
    URI uri = session.getContext().getAuthServerUrl();

    PortalConfig config = new PortalConfig();
    // name
    Optional.ofNullable(realm.getName()).ifPresent(a -> config.name(a));
    // displayName
    Optional.ofNullable(realm.getDisplayName()).ifPresent(a -> config.displayName(a));
    // logoUrl
    Optional.ofNullable(realm.getAttribute(String.format("_providerConfig.assets.logo.url")))
        .ifPresent(a -> config.logoUrl(a));
    // faviconUrl
    Optional.ofNullable(realm.getAttribute(String.format("_providerConfig.assets.favicon.url")))
        .ifPresent(a -> config.faviconUrl(a));
    // profileEnabled
    config.profileEnabled(CONFIG_ENABLED(realm, "profile.enabled", true));
    // registrationEmailAsUsername
    config.registrationEmailAsUsername(realm.isRegistrationEmailAsUsername());
    // passwordUpdateAllowed
    config.passwordUpdateAllowed(CONFIG_ENABLED(realm, "profile.password.enabled", true));
    // twoFactorUpdateAllowed
    config.twoFactorUpdateAllowed(CONFIG_ENABLED(realm, "profile.twofactor.enabled", true));
    // totpConfigured
    config.totpConfigured(isTotpConfigured);
    // passwordlessUpdateAllowed
    config.passwordlessUpdateAllowed(CONFIG_ENABLED(realm, "profile.passwordless.enabled", true));
    // deviceActivityEnabled
    config.deviceActivityEnabled(CONFIG_ENABLED(realm, "profile.activity.enabled", true));
    // linkedAccountsEnabled
    config.linkedAccountsEnabled(realm.isIdentityFederationEnabled());
    // eventsEnabled
    EventStoreProvider eventStore = session.getProvider(EventStoreProvider.class);
    config.eventsEnabled(eventStore != null && realm.isEventsEnabled());
    // editUsernameAllowed
    config.editUsernameAllowed(realm.isEditUsernameAllowed());
    // internationalizationEnabled
    config.internationalizationEnabled(realm.isInternationalizationEnabled());
    // resourcesEnabled
    config.resourcesEnabled(realm.isUserManagedAccessAllowed());
    // viewGroupsEnabled
    config.viewGroupsEnabled(isViewGroupsEnabled);
    // deleteAccountAllowed
    config.deleteAccountAllowed(deleteAccountAllowed);
    // updateEmailFeatureEnabled
    config.updateEmailFeatureEnabled(Profile.isFeatureEnabled(Profile.Feature.UPDATE_EMAIL));
    // updateEmailActionEnabled
    RequiredActionProviderModel updateEmailActionProvider =
        realm.getRequiredActionProviderByAlias(UserModel.RequiredAction.UPDATE_EMAIL.name());
    config.updateEmailActionEnabled(
        updateEmailActionProvider != null && updateEmailActionProvider.isEnabled());
    // organizationsEnabled
    config.organizationsEnabled(CONFIG_ENABLED(realm, "org.enabled", true));
    // orgDetailsEnabled
    config.orgDetailsEnabled(CONFIG_ENABLED(realm, "org.details.enabled", true));
    // orgMembersEnabled
    config.orgMembersEnabled(CONFIG_ENABLED(realm, "org.members.enabled", true));
    // orgInvitationsEnabled
    config.orgInvitationsEnabled(CONFIG_ENABLED(realm, "org.invitations.enabled", true));
    // orgDomainsEnabled
    config.orgDomainsEnabled(CONFIG_ENABLED(realm, "org.domains.enabled", true));
    // orgSsoEnabled
    config.orgSsoEnabled(CONFIG_ENABLED(realm, "org.sso.enabled", true));
    // orgEventsEnabled
    config.orgEventsEnabled(CONFIG_ENABLED(realm, "org.events.enabled", true));

    return config;
  }

  @JsonProperty("name")
  private String name;

  @JsonProperty("displayName")
  private String displayName;

  @JsonProperty("logoUrl")
  private String logoUrl;

  @JsonProperty("faviconUrl")
  private String faviconUrl;

  @JsonProperty("profileEnabled")
  private Boolean profileEnabled;

  @JsonProperty("registrationEmailAsUsername")
  private Boolean registrationEmailAsUsername;

  @JsonProperty("passwordUpdateAllowed")
  private Boolean passwordUpdateAllowed;

  @JsonProperty("twoFactorUpdateAllowed")
  private Boolean twoFactorUpdateAllowed;

  @JsonProperty("totpConfigured")
  private Boolean totpConfigured;

  @JsonProperty("passwordlessUpdateAllowed")
  private Boolean passwordlessUpdateAllowed;

  @JsonProperty("deviceActivityEnabled")
  private Boolean deviceActivityEnabled;

  @JsonProperty("linkedAccountsEnabled")
  private Boolean linkedAccountsEnabled;

  @JsonProperty("eventsEnabled")
  private Boolean eventsEnabled;

  @JsonProperty("editUsernameAllowed")
  private Boolean editUsernameAllowed;

  @JsonProperty("internationalizationEnabled")
  private Boolean internationalizationEnabled;

  @JsonProperty("resourcesEnabled")
  private Boolean resourcesEnabled;

  @JsonProperty("viewGroupsEnabled")
  private Boolean viewGroupsEnabled;

  @JsonProperty("deleteAccountAllowed")
  private Boolean deleteAccountAllowed;

  @JsonProperty("updateEmailFeatureEnabled")
  private Boolean updateEmailFeatureEnabled;

  @JsonProperty("updateEmailActionEnabled")
  private Boolean updateEmailActionEnabled;

  @JsonProperty("organizationsEnabled")
  private Boolean organizationsEnabled;

  @JsonProperty("orgDetailsEnabled")
  private Boolean orgDetailsEnabled;

  @JsonProperty("orgMembersEnabled")
  private Boolean orgMembersEnabled;

  @JsonProperty("orgInvitationsEnabled")
  private Boolean orgInvitationsEnabled;

  @JsonProperty("orgDomainsEnabled")
  private Boolean orgDomainsEnabled;

  @JsonProperty("orgSsoEnabled")
  private Boolean orgSsoEnabled;

  @JsonProperty("orgEventsEnabled")
  private Boolean orgEventsEnabled;

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  public PortalConfig name(String name) {
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

  public PortalConfig displayName(String displayName) {
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

  public PortalConfig logoUrl(String logoUrl) {
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

  public PortalConfig faviconUrl(String faviconUrl) {
    this.faviconUrl = faviconUrl;
    return this;
  }

  @JsonProperty("profileEnabled")
  public Boolean getProfileEnabled() {
    return profileEnabled;
  }

  @JsonProperty("profileEnabled")
  public void setProfileEnabled(Boolean profileEnabled) {
    this.profileEnabled = profileEnabled;
  }

  public PortalConfig profileEnabled(Boolean profileEnabled) {
    this.profileEnabled = profileEnabled;
    return this;
  }

  @JsonProperty("registrationEmailAsUsername")
  public Boolean getRegistrationEmailAsUsername() {
    return registrationEmailAsUsername;
  }

  @JsonProperty("registrationEmailAsUsername")
  public void setRegistrationEmailAsUsername(Boolean registrationEmailAsUsername) {
    this.registrationEmailAsUsername = registrationEmailAsUsername;
  }

  public PortalConfig registrationEmailAsUsername(Boolean registrationEmailAsUsername) {
    this.registrationEmailAsUsername = registrationEmailAsUsername;
    return this;
  }

  @JsonProperty("passwordUpdateAllowed")
  public Boolean getPasswordUpdateAllowed() {
    return passwordUpdateAllowed;
  }

  @JsonProperty("passwordUpdateAllowed")
  public void setPasswordUpdateAllowed(Boolean passwordUpdateAllowed) {
    this.passwordUpdateAllowed = passwordUpdateAllowed;
  }

  public PortalConfig passwordUpdateAllowed(Boolean passwordUpdateAllowed) {
    this.passwordUpdateAllowed = passwordUpdateAllowed;
    return this;
  }

  @JsonProperty("twoFactorUpdateAllowed")
  public Boolean getTwoFactorUpdateAllowed() {
    return twoFactorUpdateAllowed;
  }

  @JsonProperty("twoFactorUpdateAllowed")
  public void setTwoFactorUpdateAllowed(Boolean twoFactorUpdateAllowed) {
    this.twoFactorUpdateAllowed = twoFactorUpdateAllowed;
  }

  public PortalConfig twoFactorUpdateAllowed(Boolean twoFactorUpdateAllowed) {
    this.twoFactorUpdateAllowed = twoFactorUpdateAllowed;
    return this;
  }

  @JsonProperty("totpConfigured")
  public Boolean getTotpConfigured() {
    return totpConfigured;
  }

  @JsonProperty("totpConfigured")
  public void setTotpConfigured(Boolean totpConfigured) {
    this.totpConfigured = totpConfigured;
  }

  public PortalConfig totpConfigured(Boolean totpConfigured) {
    this.totpConfigured = totpConfigured;
    return this;
  }

  @JsonProperty("passwordlessUpdateAllowed")
  public Boolean getPasswordlessUpdateAllowed() {
    return passwordlessUpdateAllowed;
  }

  @JsonProperty("passwordlessUpdateAllowed")
  public void setPasswordlessUpdateAllowed(Boolean passwordlessUpdateAllowed) {
    this.passwordlessUpdateAllowed = passwordlessUpdateAllowed;
  }

  public PortalConfig passwordlessUpdateAllowed(Boolean passwordlessUpdateAllowed) {
    this.passwordlessUpdateAllowed = passwordlessUpdateAllowed;
    return this;
  }

  @JsonProperty("deviceActivityEnabled")
  public Boolean getDeviceActivityEnabled() {
    return deviceActivityEnabled;
  }

  @JsonProperty("deviceActivityEnabled")
  public void setDeviceActivityEnabled(Boolean deviceActivityEnabled) {
    this.deviceActivityEnabled = deviceActivityEnabled;
  }

  public PortalConfig deviceActivityEnabled(Boolean deviceActivityEnabled) {
    this.deviceActivityEnabled = deviceActivityEnabled;
    return this;
  }

  @JsonProperty("linkedAccountsEnabled")
  public Boolean getLinkedAccountsEnabled() {
    return linkedAccountsEnabled;
  }

  @JsonProperty("linkedAccountsEnabled")
  public void setLinkedAccountsEnabled(Boolean linkedAccountsEnabled) {
    this.linkedAccountsEnabled = linkedAccountsEnabled;
  }

  public PortalConfig linkedAccountsEnabled(Boolean linkedAccountsEnabled) {
    this.linkedAccountsEnabled = linkedAccountsEnabled;
    return this;
  }

  @JsonProperty("eventsEnabled")
  public Boolean getEventsEnabled() {
    return eventsEnabled;
  }

  @JsonProperty("eventsEnabled")
  public void setEventsEnabled(Boolean eventsEnabled) {
    this.eventsEnabled = eventsEnabled;
  }

  public PortalConfig eventsEnabled(Boolean eventsEnabled) {
    this.eventsEnabled = eventsEnabled;
    return this;
  }

  @JsonProperty("editUsernameAllowed")
  public Boolean getEditUsernameAllowed() {
    return editUsernameAllowed;
  }

  @JsonProperty("editUsernameAllowed")
  public void setEditUsernameAllowed(Boolean editUsernameAllowed) {
    this.editUsernameAllowed = editUsernameAllowed;
  }

  public PortalConfig editUsernameAllowed(Boolean editUsernameAllowed) {
    this.editUsernameAllowed = editUsernameAllowed;
    return this;
  }

  @JsonProperty("internationalizationEnabled")
  public Boolean getInternationalizationEnabled() {
    return internationalizationEnabled;
  }

  @JsonProperty("internationalizationEnabled")
  public void setInternationalizationEnabled(Boolean internationalizationEnabled) {
    this.internationalizationEnabled = internationalizationEnabled;
  }

  public PortalConfig internationalizationEnabled(Boolean internationalizationEnabled) {
    this.internationalizationEnabled = internationalizationEnabled;
    return this;
  }

  @JsonProperty("resourcesEnabled")
  public Boolean getResourcesEnabled() {
    return resourcesEnabled;
  }

  @JsonProperty("resourcesEnabled")
  public void setResourcesEnabled(Boolean resourcesEnabled) {
    this.resourcesEnabled = resourcesEnabled;
  }

  public PortalConfig resourcesEnabled(Boolean resourcesEnabled) {
    this.resourcesEnabled = resourcesEnabled;
    return this;
  }

  @JsonProperty("viewGroupsEnabled")
  public Boolean getViewGroupsEnabled() {
    return viewGroupsEnabled;
  }

  @JsonProperty("viewGroupsEnabled")
  public void setViewGroupsEnabled(Boolean viewGroupsEnabled) {
    this.viewGroupsEnabled = viewGroupsEnabled;
  }

  public PortalConfig viewGroupsEnabled(Boolean viewGroupsEnabled) {
    this.viewGroupsEnabled = viewGroupsEnabled;
    return this;
  }

  @JsonProperty("deleteAccountAllowed")
  public Boolean getDeleteAccountAllowed() {
    return deleteAccountAllowed;
  }

  @JsonProperty("deleteAccountAllowed")
  public void setDeleteAccountAllowed(Boolean deleteAccountAllowed) {
    this.deleteAccountAllowed = deleteAccountAllowed;
  }

  public PortalConfig deleteAccountAllowed(Boolean deleteAccountAllowed) {
    this.deleteAccountAllowed = deleteAccountAllowed;
    return this;
  }

  @JsonProperty("updateEmailFeatureEnabled")
  public Boolean getUpdateEmailFeatureEnabled() {
    return updateEmailFeatureEnabled;
  }

  @JsonProperty("updateEmailFeatureEnabled")
  public void setUpdateEmailFeatureEnabled(Boolean updateEmailFeatureEnabled) {
    this.updateEmailFeatureEnabled = updateEmailFeatureEnabled;
  }

  public PortalConfig updateEmailFeatureEnabled(Boolean updateEmailFeatureEnabled) {
    this.updateEmailFeatureEnabled = updateEmailFeatureEnabled;
    return this;
  }

  @JsonProperty("updateEmailActionEnabled")
  public Boolean getUpdateEmailActionEnabled() {
    return updateEmailActionEnabled;
  }

  @JsonProperty("updateEmailActionEnabled")
  public void setUpdateEmailActionEnabled(Boolean updateEmailActionEnabled) {
    this.updateEmailActionEnabled = updateEmailActionEnabled;
  }

  public PortalConfig updateEmailActionEnabled(Boolean updateEmailActionEnabled) {
    this.updateEmailActionEnabled = updateEmailActionEnabled;
    return this;
  }

  @JsonProperty("organizationsEnabled")
  public Boolean getOrganizationsEnabled() {
    return organizationsEnabled;
  }

  @JsonProperty("organizationsEnabled")
  public void setOrganizationsEnabled(Boolean organizationsEnabled) {
    this.organizationsEnabled = organizationsEnabled;
  }

  public PortalConfig organizationsEnabled(Boolean organizationsEnabled) {
    this.organizationsEnabled = organizationsEnabled;
    return this;
  }

  @JsonProperty("orgDetailsEnabled")
  public Boolean getOrgDetailsEnabled() {
    return orgDetailsEnabled;
  }

  @JsonProperty("orgDetailsEnabled")
  public void setOrgDetailsEnabled(Boolean orgDetailsEnabled) {
    this.orgDetailsEnabled = orgDetailsEnabled;
  }

  public PortalConfig orgDetailsEnabled(Boolean orgDetailsEnabled) {
    this.orgDetailsEnabled = orgDetailsEnabled;
    return this;
  }

  @JsonProperty("orgMembersEnabled")
  public Boolean getOrgMembersEnabled() {
    return orgMembersEnabled;
  }

  @JsonProperty("orgMembersEnabled")
  public void setOrgMembersEnabled(Boolean orgMembersEnabled) {
    this.orgMembersEnabled = orgMembersEnabled;
  }

  public PortalConfig orgMembersEnabled(Boolean orgMembersEnabled) {
    this.orgMembersEnabled = orgMembersEnabled;
    return this;
  }

  @JsonProperty("orgInvitationsEnabled")
  public Boolean getOrgInvitationsEnabled() {
    return orgInvitationsEnabled;
  }

  @JsonProperty("orgInvitationsEnabled")
  public void setOrgInvitationsEnabled(Boolean orgInvitationsEnabled) {
    this.orgInvitationsEnabled = orgInvitationsEnabled;
  }

  public PortalConfig orgInvitationsEnabled(Boolean orgInvitationsEnabled) {
    this.orgInvitationsEnabled = orgInvitationsEnabled;
    return this;
  }

  @JsonProperty("orgDomainsEnabled")
  public Boolean getOrgDomainsEnabled() {
    return orgDomainsEnabled;
  }

  @JsonProperty("orgDomainsEnabled")
  public void setOrgDomainsEnabled(Boolean orgDomainsEnabled) {
    this.orgDomainsEnabled = orgDomainsEnabled;
  }

  public PortalConfig orgDomainsEnabled(Boolean orgDomainsEnabled) {
    this.orgDomainsEnabled = orgDomainsEnabled;
    return this;
  }

  @JsonProperty("orgSsoEnabled")
  public Boolean getOrgSsoEnabled() {
    return orgSsoEnabled;
  }

  @JsonProperty("orgSsoEnabled")
  public void setOrgSsoEnabled(Boolean orgSsoEnabled) {
    this.orgSsoEnabled = orgSsoEnabled;
  }

  public PortalConfig orgSsoEnabled(Boolean orgSsoEnabled) {
    this.orgSsoEnabled = orgSsoEnabled;
    return this;
  }

  @JsonProperty("orgEventsEnabled")
  public Boolean getOrgEventsEnabled() {
    return orgEventsEnabled;
  }

  @JsonProperty("orgEventsEnabled")
  public void setOrgEventsEnabled(Boolean orgEventsEnabled) {
    this.orgEventsEnabled = orgEventsEnabled;
  }

  public PortalConfig orgEventsEnabled(Boolean orgEventsEnabled) {
    this.orgEventsEnabled = orgEventsEnabled;
    return this;
  }
}
