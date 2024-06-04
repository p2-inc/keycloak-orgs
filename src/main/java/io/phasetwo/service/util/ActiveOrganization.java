package io.phasetwo.service.util;

import static io.phasetwo.service.Orgs.ACTIVE_ORGANIZATION;

import com.google.common.collect.Lists;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.Getter;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class ActiveOrganization {

  private static final Logger log = Logger.getLogger(ActiveOrganization.class);
  private final RealmModel realm;
  private final UserModel user;
  private final OrganizationProvider organizationProvider;
  @Getter() private final OrganizationModel organization;

  public static ActiveOrganization fromContext(
      KeycloakSession session, RealmModel realm, UserModel user) {
    return new ActiveOrganization(session, realm, user);
  }

  private ActiveOrganization(KeycloakSession session, RealmModel realm, UserModel user) {
    this.realm = realm;
    this.user = user;
    this.organizationProvider = session.getProvider(OrganizationProvider.class);
    this.organization =
        userHasActiveOrganizationAttribute()
            ? initializeActiveOrganization()
            : initializeDefaultActiveOrganization();
    clearOutdatedActiveOrganizationAttribute();
  }

  private boolean userHasActiveOrganizationAttribute() {
    return user.getAttributes().containsKey(ACTIVE_ORGANIZATION);
  }

  private OrganizationModel initializeActiveOrganization() {
    return organizationProvider.getOrganizationById(realm, getActiveOrganizationIdFromAttribute());
  }

  private OrganizationModel initializeDefaultActiveOrganization() {
    Stream<OrganizationModel> userOrganizations =
        organizationProvider.getUserOrganizationsStream(realm, user);
    return userOrganizations.findFirst().orElse(null);
  }

  private void clearOutdatedActiveOrganizationAttribute() {
    if (!userHasOrganization() && userHasActiveOrganizationAttribute()) {
      user.setAttribute(ACTIVE_ORGANIZATION, new ArrayList<>());
    } else if (organizationProvider
        .getUserOrganizationsStream(realm, user)
        .noneMatch(org -> org.getId().equals(getActiveOrganizationIdFromAttribute()))) {
      log.warnf("%s doesn't belong to this organization", user.getUsername());
      user.setAttribute(ACTIVE_ORGANIZATION, new ArrayList<>());
    }
  }

  public boolean userHasOrganization() {
    return organizationProvider.getUserOrganizationsStream(realm, user).findFirst().isPresent();
  }

  private String getActiveOrganizationIdFromAttribute() {
    return user.getFirstAttribute(ACTIVE_ORGANIZATION);
  }

  public List<String> getUserActiveOrganizationRoles() {
    List<String> userOrganizationRoles = Lists.newArrayList();
    organization
        .getRolesStream()
        .forEach(
            role -> {
              if (role.hasRole(user)) {
                userOrganizationRoles.add(role.getName());
              }
            });
    return userOrganizationRoles;
  }

  public boolean isCurrentActiveOrganization(String organizationId) {
    return organization.getId().equals(organizationId);
  }

  public boolean isValid() {
    return organization != null;
  }

  public List<String> getRealmLevelTiers() {
    List<String> realmRoles = Lists.newArrayList();
    organization
        .getRealmTierMappingsStream()
        .forEach(tier -> realmRoles.add(tier.getRoleName()));
    return realmRoles;
  }
}
