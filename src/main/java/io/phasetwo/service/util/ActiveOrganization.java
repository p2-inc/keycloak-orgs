package io.phasetwo.service.util;

import static io.phasetwo.service.Orgs.ACTIVE_ORGANIZATION;

import com.google.common.collect.Lists;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class ActiveOrganization {

  private static final Logger log = Logger.getLogger(ActiveOrganization.class);

  private final RealmModel realm;

  private final UserModel user;

  private final OrganizationProvider organizationProvider;

  private OrganizationModel organization;

  public ActiveOrganization(KeycloakSession session, RealmModel realm, UserModel user) {
    this.realm = realm;
    this.user = user;
    this.organizationProvider = session.getProvider(OrganizationProvider.class);
  }

  public boolean hasOrganization() {
    return organizationProvider.getUserOrganizationsStream(realm, user).findFirst().isPresent();
  }

  public boolean isValid() {

    String activeOrganizationId;

    // If the user has no active organization, we take the first one by default, if there is any.
    if (!user.getAttributes().containsKey(ACTIVE_ORGANIZATION)) {
      return getDefaultActiveOrganization();
    } else {
      activeOrganizationId = user.getFirstAttribute(ACTIVE_ORGANIZATION);
    }

    // security measure
    // verify that the user belong to the organization (in case he modified through account POST
    // api)
    // no issue if read-only user attributes is enabled with "org.ro.*"
    // --spi-user-profile-declarative-user-profile-read-only-attributes=org.ro.*
    if (organizationProvider
        .getUserOrganizationsStream(realm, user)
        .noneMatch(org -> org.getId().equals(activeOrganizationId))) {
      log.warnf("%s doesn't belong to this organization", user.getUsername());

      // verify that the organization still exists
      organization = organizationProvider.getOrganizationById(realm, activeOrganizationId);
      if (organization == null) {
        log.warnf("organization doesn't exists anymore.");
        user.removeAttribute(ACTIVE_ORGANIZATION);
      }

      return false;
    }

    organization = organizationProvider.getOrganizationById(realm, activeOrganizationId);

    if (organization == null) {
      log.errorf("%s not found", activeOrganizationId);
      return false;
    }

    return true;
  }

  public boolean getDefaultActiveOrganization() {

    Stream<OrganizationModel> userOrganizations =
        organizationProvider.getUserOrganizationsStream(realm, user);
    Optional<OrganizationModel> firstOrganization = userOrganizations.findFirst();

    if (firstOrganization.isEmpty()) {
      return false;
    }

    organization = firstOrganization.get();

    return true;
  }

  public OrganizationModel getActiveOrganization() {
    return organization;
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
}
