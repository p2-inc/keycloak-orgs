package io.phasetwo.service.util;

import static io.phasetwo.service.Orgs.ACTIVE_ORGANIZATION;

import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.model.OrganizationRoleModel;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class ActiveOrganization {

  private static final Logger log = Logger.getLogger(ActiveOrganization.class);
  private final UserModel user;
  private final List<OrganizationModel> userOrganizations;
  @Getter() private final OrganizationModel organization;

  public static ActiveOrganization fromContext(
      KeycloakSession session, RealmModel realm, UserModel user) {
    return new ActiveOrganization(session, realm, user);
  }

  private ActiveOrganization(KeycloakSession session, RealmModel realm, UserModel user) {
    this.user = user;
    OrganizationProvider organizationProvider = session.getProvider(OrganizationProvider.class);
    // Each getUserOrganizationsStream call queries the membership table, so load it once.
    this.userOrganizations = organizationProvider.getUserOrganizationsStream(realm, user).toList();
    this.organization =
        userHasActiveOrganizationAttribute()
            ? initializeActiveOrganization()
            : initializeDefaultActiveOrganization();
    clearOutdatedActiveOrganizationAttribute();
  }

  private boolean userHasActiveOrganizationAttribute() {
    return user.getAttributes().containsKey(ACTIVE_ORGANIZATION);
  }

  /**
   * Resolves the organization named by the active organization attribute, but only among the
   * user's current memberships. If the attribute points at an organization the user no longer
   * belongs to, or one that has been deleted, fall back to the default organization so the token
   * being issued never carries a stale organization or an empty claim.
   */
  private OrganizationModel initializeActiveOrganization() {
    String activeOrganizationId = getActiveOrganizationIdFromAttribute();
    return userOrganizations.stream()
        .filter(org -> org.getId().equals(activeOrganizationId))
        .findFirst()
        .orElseGet(this::initializeDefaultActiveOrganization);
  }

  private OrganizationModel initializeDefaultActiveOrganization() {
    return userOrganizations.stream().findFirst().orElse(null);
  }

  private void clearOutdatedActiveOrganizationAttribute() {
    // Nothing to clear. Without this, a user with no active organization attribute fails the
    // membership check below (null id) and logs a spurious warning on every token.
    if (!userHasActiveOrganizationAttribute()) {
      return;
    }
    if (!userHasOrganization()) {
      user.setAttribute(ACTIVE_ORGANIZATION, new ArrayList<>());
    } else if (userOrganizations.stream()
        .noneMatch(org -> org.getId().equals(getActiveOrganizationIdFromAttribute()))) {
      log.warnf("%s doesn't belong to this organization", user.getUsername());
      user.setAttribute(ACTIVE_ORGANIZATION, new ArrayList<>());
    }
  }

  public boolean userHasOrganization() {
    return !userOrganizations.isEmpty();
  }

  private String getActiveOrganizationIdFromAttribute() {
    return user.getFirstAttribute(ACTIVE_ORGANIZATION);
  }

  public List<String> getUserActiveOrganizationRoles() {
    return organization.getRolesByUserStream(user).map(OrganizationRoleModel::getName).toList();
  }

  public boolean isCurrentActiveOrganization(String organizationId) {
    return organization.getId().equals(organizationId);
  }

  public boolean isValid() {
    return organization != null;
  }
}
