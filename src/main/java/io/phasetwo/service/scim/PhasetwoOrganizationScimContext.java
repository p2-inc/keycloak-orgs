package io.phasetwo.service.scim;

import static fi.metatavu.keycloak.scim.server.users.UsersController.getEmailDomain;

import fi.metatavu.keycloak.scim.server.organization.OrganizationScimConfig;
import fi.metatavu.keycloak.scim.server.organization.OrganizationScimContext;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.resource.Converters;
import java.net.URI;
import java.util.stream.Stream;
import org.jboss.logging.Logger;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/** SCIM context for Phase Two organizations. https://github.com/p2-inc/keycloak-orgs */
public class PhasetwoOrganizationScimContext extends OrganizationScimContext {

  private static final Logger logger = Logger.getLogger(PhasetwoOrganizationScimContext.class);

  protected final OrganizationModel organization;

  public PhasetwoOrganizationScimContext(
      URI baseUri,
      KeycloakSession session,
      RealmModel realm,
      OrganizationScimConfig config,
      OrganizationModel organization) {
    super(baseUri, session, realm, organization.getId(), config);
    this.organization = organization;
  }

  @Override
  public Stream<UserModel> getMembersStream(Integer first, Integer max) {
    return organization.searchForMembersStream(null, null, null);
  }

  @Override
  public UserModel findUser(String userId) {
    UserModel organizationUser = getSession().users().getUserById(getRealm(), userId);
    if (organizationUser == null) return null;
    if (!organization.hasMembership(organizationUser)) return null;
    return organizationUser;
  }

  @Override
  public boolean addMember(UserModel user) {
    try {
      organization.grantMembership(user);
    } catch (Exception ignore) {
      return false;
    }
    return true;
  }

  @Override
  public boolean isMember(UserModel user) {
    return organization.hasMembership(user);
  }

  @Override
  public boolean removeMember(UserModel user) {
    try {
      organization.revokeMembership(user);
    } catch (Exception ignore) {
      return false;
    }
    return true;
  }

  @Override
  public boolean linkUserIdp(
      UserModel user, String scimUserEmail, String scimUserName, String scimExternalId) {
    if (scimUserEmail == null) {
      logger.warn("User email is not set. Cannot link user to identity provider");
      return false;
    }

    if (scimExternalId == null) {
      logger.warn("User externalId is not set. Cannot link user to identity provider");
      return false;
    }

    String emailDomain = getEmailDomain(scimUserEmail);
    if (emailDomain == null) {
      logger.warn("User email domain is not set. Cannot link user to identity provider");
      return false;
    }

    IdentityProviderModel identityProvider = null;
    if (organization.getDomains() != null && organization.getDomains().contains(emailDomain)) {
      identityProvider =
          organization
              .getIdentityProvidersStream()
              .filter(IdentityProviderModel::isEnabled)
              .findFirst()
              .orElse(null);
    }

    if (identityProvider == null) {
      logger.warn(
          "No identity provider found for email domain: "
              + emailDomain
              + ". Cannot link user to identity provider");
      return false;
    }

    if (getSession().users().getFederatedIdentity(getRealm(), user, identityProvider.getAlias())
        == null) {
      logger.info("Linking user to identity provider: " + identityProvider.getAlias());
      FederatedIdentityModel identityModel =
          new FederatedIdentityModel(identityProvider.getAlias(), scimExternalId, scimUserName);
      getSession().users().addFederatedIdentity(getRealm(), user, identityModel);
      return true;
    }

    return false;
  }

  @Override
  public Object toRepresentation() {
    return Converters.convertOrganizationModelToOrganization(organization);
  }
}
