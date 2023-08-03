package io.phasetwo.service.resource;

import com.google.common.collect.ImmutableList;
import io.phasetwo.service.model.InvitationModel;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationRoleModel;
import io.phasetwo.service.model.jpa.entity.InvitationEntity;
import io.phasetwo.service.model.jpa.entity.OrganizationEntity;
import io.phasetwo.service.model.jpa.entity.OrganizationMemberEntity;
import io.phasetwo.service.model.jpa.entity.TeamEntity;
import jakarta.ws.rs.NotAuthorizedException;
import java.util.List;
import java.util.Map;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.resources.admin.AdminAuth;

/** */
@JBossLog
public class OrganizationAdminAuth extends AdminAuth {

  public static final String ROLE_CREATE_ORGANIZATION = "create-organization";
  public static final String ROLE_VIEW_ORGANIZATION = "view-organizations";
  public static final String ROLE_MANAGE_ORGANIZATION = "manage-organizations";

  public OrganizationAdminAuth(
      RealmModel realm, AccessToken token, UserModel user, ClientModel client) {
    super(realm, token, user, client);
    log.debugf("Realm passed to ctr is %s", realm.getName());
  }

  // create-organization
  void requireCreateOrg() {
    if (!hasAppRole(getClient(), ROLE_CREATE_ORGANIZATION))
      throw new NotAuthorizedException(ROLE_CREATE_ORGANIZATION);
  }

  boolean hasCreateOrg() {
    return hasAppRole(getClient(), ROLE_CREATE_ORGANIZATION);
  }

  // view-organizations
  void requireViewOrgs() {
    if (!hasAppRole(getClient(), ROLE_VIEW_ORGANIZATION))
      throw new NotAuthorizedException(ROLE_VIEW_ORGANIZATION);
  }

  boolean hasViewOrgs() {
    return hasAppRole(getClient(), ROLE_VIEW_ORGANIZATION);
  }

  // manage-organizations
  void requireManageOrgs() {
    if (!hasAppRole(getClient(), ROLE_MANAGE_ORGANIZATION))
      throw new NotAuthorizedException(ROLE_MANAGE_ORGANIZATION);
  }

  boolean hasManageOrgs() {
    return hasAppRole(getClient(), ROLE_MANAGE_ORGANIZATION);
  }

  // org in realm
  @Deprecated
  void requireOrgInRealm(OrganizationEntity orgEntity) {
    if (!orgEntity.getRealmId().equals(getRealm().getId())) {
      throw new NotAuthorizedException(
          String.format(
              "Organization %s not in realm %s", orgEntity.getId(), getRealm().getName()));
    }
  }

  // org in realm
  void requireOrgInRealm(OrganizationModel org) {
    if (!isOrgInRealm(org)) {
      throw new NotAuthorizedException(
          String.format("Organization %s not in realm %s", org.getId(), getRealm().getName()));
    }
  }

  boolean isOrgInRealm(OrganizationModel org) {
    return (org.getRealm().getId().equals(getRealm().getId()));
  }

  // user in realm
  @Deprecated
  void requireUserInRealm(UserEntity userEntity) {
    if (!userEntity.getRealmId().equals(getRealm().getId())) {
      throw new NotAuthorizedException(
          String.format("User %s not in realm %s", userEntity.getId(), getRealm().getName()));
    }
  }

  // invitation in org in realm
  @Deprecated
  void requireInvitationInOrgInRealm(InvitationEntity inviteEntity, OrganizationEntity orgEntity) {
    if (!inviteEntity.getOrganization().equals(orgEntity)) {
      throw new NotAuthorizedException(
          String.format("Invitation %s not in org %s", inviteEntity.getId(), orgEntity.getId()));
    }
    requireOrgInRealm(orgEntity);
  }

  // invitation in org in realm
  void requireInvitationInOrgInRealm(InvitationModel invite, OrganizationModel org) {
    if (!isInvitationInOrgInRealm(invite, org)) {
      throw new NotAuthorizedException(
          String.format("Invitation %s not in org %s", invite.getId(), org.getId()));
    }
  }

  boolean isInvitationInOrgInRealm(InvitationModel invite, OrganizationModel org) {
    return (invite.getOrganization().equals(org) && isOrgInRealm(org));
  }

  // team in org in realm
  @Deprecated
  void requireTeamInOrgInRealm(TeamEntity teamEntity, OrganizationEntity orgEntity) {
    if (!teamEntity.getOrganization().equals(orgEntity)) {
      throw new NotAuthorizedException(
          String.format("Team %s not in org %s", teamEntity.getId(), orgEntity.getId()));
    }
    requireOrgInRealm(orgEntity);
  }

  // user in org in realm
  @Deprecated
  void requireUserInOrgInRealm(
      UserEntity userEntity, OrganizationMemberEntity memberEntity, OrganizationEntity orgEntity) {
    if (userEntity == null
        || memberEntity == null
        || orgEntity != null
        || !userEntity.getId().equals(memberEntity.getUserId())
        || !memberEntity.getOrganization().equals(orgEntity)) {
      throw new NotAuthorizedException(
          String.format("User %s not in org %s", userEntity.getId(), orgEntity.getId()));
    }
    requireOrgInRealm(orgEntity);
  }

  //
  // Organization Users
  //

  public static final String ORG_ROLE_VIEW_ORGANIZATION = "view-organization";
  public static final String ORG_ROLE_MANAGE_ORGANIZATION = "manage-organization";
  public static final String ORG_ROLE_VIEW_MEMBERS = "view-members";
  public static final String ORG_ROLE_MANAGE_MEMBERS = "manage-members";
  public static final String ORG_ROLE_VIEW_ROLES = "view-roles";
  public static final String ORG_ROLE_MANAGE_ROLES = "manage-roles";
  public static final String ORG_ROLE_VIEW_INVITATIONS = "view-invitations";
  public static final String ORG_ROLE_MANAGE_INVITATIONS = "manage-invitations";
  public static final String ORG_ROLE_VIEW_IDENTITY_PROVIDERS = "view-identity-providers";
  public static final String ORG_ROLE_MANAGE_IDENTITY_PROVIDERS = "manage-identity-providers";

  public static final String[] DEFAULT_ORG_ROLES = {
    ORG_ROLE_VIEW_ORGANIZATION,
    ORG_ROLE_MANAGE_ORGANIZATION,
    ORG_ROLE_VIEW_MEMBERS,
    ORG_ROLE_MANAGE_MEMBERS,
    ORG_ROLE_VIEW_ROLES,
    ORG_ROLE_MANAGE_ROLES,
    ORG_ROLE_VIEW_INVITATIONS,
    ORG_ROLE_MANAGE_INVITATIONS,
    ORG_ROLE_VIEW_IDENTITY_PROVIDERS,
    ORG_ROLE_MANAGE_IDENTITY_PROVIDERS
  };

  /**
   * @param org The selected organization
   * @return true if the logged-in user has the view-organization permission *IN* the specified org
   *     or they are a member of the organization.
   */
  boolean hasOrgViewOrg(OrganizationModel org) {
    return hasOrgRole(org, ORG_ROLE_VIEW_ORGANIZATION) || org.hasMembership(getUser());
  }

  /**
   * @param org The selected organization
   * @return true if the logged-in user has the manage-organization permission *IN* the specified
   *     org
   */
  boolean hasOrgManageOrg(OrganizationModel org) {
    return hasOrgRole(org, ORG_ROLE_MANAGE_ORGANIZATION);
  }

  /**
   * @param org The selected organization
   * @return true if the logged-in user has the view-members permission *IN* the specified org
   */
  boolean hasOrgViewMembers(OrganizationModel org) {
    return hasOrgRole(org, ORG_ROLE_VIEW_MEMBERS);
  }

  /**
   * @param org The selected organization
   * @return true if the logged-in user has the manage-members permission *IN* the specified org
   */
  boolean hasOrgManageMembers(OrganizationModel org) {
    return hasOrgRole(org, ORG_ROLE_MANAGE_MEMBERS);
  }

  /**
   * @param org The selected organization
   * @return true if the logged-in user has the view-invitations permission *IN* the specified org
   */
  boolean hasOrgViewInvitations(OrganizationModel org) {
    return hasOrgRole(org, ORG_ROLE_VIEW_INVITATIONS);
  }

  /**
   * @param org The selected organization
   * @return true if the logged-in user has the manage-invitations permission *IN* the specified org
   */
  boolean hasOrgManageInvitations(OrganizationModel org) {
    return hasOrgRole(org, ORG_ROLE_MANAGE_INVITATIONS);
  }

  /**
   * @param org The selected organization
   * @return true if the logged-in user has the view-roles permission *IN* the specified org
   */
  boolean hasOrgViewRoles(OrganizationModel org) {
    return hasOrgRole(org, ORG_ROLE_VIEW_ROLES);
  }

  /**
   * @param org The selected organization
   * @return true if the logged-in user has the manage-roles permission *IN* the specified org
   */
  boolean hasOrgManageRoles(OrganizationModel org) {
    return hasOrgRole(org, ORG_ROLE_MANAGE_ROLES);
  }

  /**
   * @param org The selected organization
   * @return true if the logged-in user has the view-identity-providers permission *IN* the
   *     specified org
   */
  boolean hasOrgViewIdentityProviders(OrganizationModel org) {
    return hasOrgRole(org, ORG_ROLE_VIEW_IDENTITY_PROVIDERS);
  }

  /**
   * @param org The selected organization
   * @return true if the logged-in user has the manage-identity-providers permission *IN* the
   *     specified org
   */
  boolean hasOrgManageIdentityProviders(OrganizationModel org) {
    return hasOrgRole(org, ORG_ROLE_MANAGE_IDENTITY_PROVIDERS);
  }

  /**
   * @param org The selected organization
   * @return true if the logged-in user has all default org roles *IN* the specified org
   */
  boolean hasOrgAll(OrganizationModel org) {
    for (String role : DEFAULT_ORG_ROLES) {
      if (!hasOrgRole(org, role)) return false;
    }
    return true;
  }

  /**
   * @param org The selected organization
   * @return true if the logged-in user has all default org view roles *IN* the specified org
   */
  boolean hasOrgViewAll(OrganizationModel org) {
    for (String role : DEFAULT_ORG_ROLES) {
      if (role.startsWith("view")) {
        if (!hasOrgRole(org, role)) return false;
      }
    }
    return true;
  }

  /**
   * @param org The selected organization
   * @return true if the logged-in user has all default org manage roles *IN* the specified org
   */
  boolean hasOrgManageAll(OrganizationModel org) {
    for (String role : DEFAULT_ORG_ROLES) {
      if (role.startsWith("manage")) {
        if (!hasOrgRole(org, role)) return false;
      }
    }
    return true;
  }

  static String ORGANIZATIONS_CLAIM = "organizations";

  private List<String> getOrganizationRoles(OrganizationModel org) {
    Object o = getToken().getOtherClaims().get(ORGANIZATIONS_CLAIM);
    if (o == null || !(o instanceof Map)) return ImmutableList.of();
    Map<String, Object> orgs = (Map<String, Object>) o;
    Object os = orgs.get(org.getId());
    if (os == null || !(os instanceof Map)) return ImmutableList.of();
    Map<String, Object> osrg = (Map<String, Object>) os;
    Object rs = osrg.get("roles");
    if (rs == null || !(rs instanceof List)) return ImmutableList.of();
    return (List<String>) rs;
  }

  private boolean hasOrgRoleInToken(OrganizationModel org, String roleName) {
    return (getOrganizationRoles(org).contains(roleName));
  }

  private boolean hasOrgRole(OrganizationModel org, String roleName) {
    /*
    if (!hasOrgRoleInToken(org, roleName)) {
      log.debugf("%s not in token %s", roleName, getToken().getOtherClaims());
      return false;
    }
    */
    OrganizationRoleModel role = org.getRoleByName(roleName);
    boolean has = (role != null && role.hasRole(getUser()));
    log.debugf("%s has role %s? %b", getUser().getId(), roleName, has);
    return has;
  }

  private void requireOrgRole(OrganizationModel org, String roleName) {
    if (!hasOrgRole(org, roleName)) {
      throw new NotAuthorizedException(
          String.format(
              "User %s doesn't have role %s in org %s",
              getUser().getId(), roleName, org.getName()));
    }
  }
}
