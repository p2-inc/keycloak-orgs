package io.phasetwo.service.importexport;

import static io.phasetwo.service.Orgs.ORG_OWNER_CONFIG_KEY;

import io.phasetwo.service.importexport.representation.OrganizationAttributes;
import io.phasetwo.service.importexport.representation.OrganizationRepresentation;
import io.phasetwo.service.importexport.representation.OrganizationRoleRepresentation;
import io.phasetwo.service.model.InvitationModel;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.resource.OrganizationAdminAuth;
import io.phasetwo.service.util.IdentityProviders;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;

@JBossLog
public final class KeycloakOrgsImportConverter {

  public static void setOrganizationAttributes(
      OrganizationAttributes organizationAttributes, OrganizationModel org) {
    org.setDisplayName(organizationAttributes.getDisplayName());
    org.setUrl(organizationAttributes.getUrl());
    if (organizationAttributes.getAttributes() != null) {
      organizationAttributes.getAttributes().forEach(org::setAttribute);
    }
    if (organizationAttributes.getDomains() != null) {
      org.setDomains(organizationAttributes.getDomains());
    }
  }

  public static void addInvitations(
      KeycloakSession session,
      RealmModel newRealm,
      OrganizationRepresentation organizationRepresentation,
      OrganizationModel org,
      boolean skipMissingMember) {
    organizationRepresentation
        .getInvitations()
        .forEach(
            invitation -> {
              var user =
                  KeycloakModelUtils.findUserByNameOrEmail(
                      session, newRealm, invitation.getEmail());
              if (user != null && org.hasMembership(user)) {
                throw new ModelException(
                    "User with email %s s already a member of organization: %s "
                        .formatted(invitation.getEmail(), org.getName()));
              }

              var inviter =
                  session.users().getUserByUsername(newRealm, invitation.getInviterUsername());
              if (inviter != null && org.hasMembership(inviter)) {
                InvitationModel i = org.addInvitation(invitation.getEmail(), inviter);
                i.setUrl(invitation.getRedirectUri());
                if (invitation.getRoles() != null) {
                  i.setRoles(invitation.getRoles());
                }
                if (invitation.getAttributes() != null && !invitation.getAttributes().isEmpty()) {
                  invitation.getAttributes().forEach(i::setAttribute);
                }
              } else {
                if (skipMissingMember) {
                  log.info(
                      "Skipped invitation import for email: %s. Inviter with username %s not found."
                          .formatted(invitation.getEmail(), invitation.getInviterUsername()));
                } else {
                  throw new ModelException(
                      "No inviter user with username %s in organization: %s"
                          .formatted(invitation.getInviterUsername(), org.getName()));
                }
              }
            });
  }

  public static void addMembers(
      KeycloakSession session,
      RealmModel newRealm,
      OrganizationRepresentation organizationRepresentation,
      OrganizationModel org,
      boolean skipMissingMember) {
    organizationRepresentation
        .getMembers()
        .forEach(
            member -> {
              var userModel = session.users().getUserByUsername(newRealm, member.getUsername());
              if (Objects.nonNull(userModel)) {
                org.grantMembership(userModel);
                member.getRoles().stream()
                    .map(org::getRoleByName)
                    .forEach(organizationRoleModel -> organizationRoleModel.grantRole(userModel));
              } else {
                if (skipMissingMember) {
                  log.info(
                      "Skipped import for member with username: "
                          + member.getUsername()
                          + ". No user found.");
                } else {
                  throw new ModelException("No user with username: " + member.getUsername());
                }
              }
            });
  }

  public static void createOrganizationIdp(
          KeycloakSession session, String idpLink, OrganizationModel org, boolean skipMissingIdp) {
    if (Objects.nonNull(idpLink)) {
      IdentityProviderModel idp = session.identityProviders().getByAlias(idpLink);
      if (Objects.nonNull(idp)) {
        IdentityProviders.setAttributeMultivalued(
            idp.getConfig(), ORG_OWNER_CONFIG_KEY, Set.of(org.getId()));
        session.identityProviders().update(idp);
      } else {
        if (skipMissingIdp) {
          log.info(
              "Skipped import for idp with alias: "
                  + idpLink
                  + ". No identity provider config found.");
        } else {
          throw new ModelException("No identity provider config with alias: " + idpLink);
        }
      }
    }
  }

  public static void createOrganizationRoles(
      List<OrganizationRoleRepresentation> roles, OrganizationModel org) {
    roles.stream()
        .filter(
            organizationRole ->
                Arrays.stream(OrganizationAdminAuth.DEFAULT_ORG_ROLES)
                    .noneMatch(role -> role.equals(organizationRole.getName())))
        .forEach(
            organizationRole -> {
              var role = org.addRole(organizationRole.getName());
              role.setDescription(organizationRole.getDescription());
            });
  }
}
