package io.phasetwo.service.importexport;

import static io.phasetwo.service.Orgs.ORG_OWNER_CONFIG_KEY;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.phasetwo.service.importexport.representation.InvitationRepresentation;
import io.phasetwo.service.importexport.representation.OrganizationAttributes;
import io.phasetwo.service.importexport.representation.OrganizationRepresentation;
import io.phasetwo.service.importexport.representation.OrganizationRoleRepresentation;
import io.phasetwo.service.importexport.representation.UserRolesRepresentation;
import io.phasetwo.service.model.InvitationModel;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationRoleModel;
import io.phasetwo.service.resource.OrganizationResourceProviderFactory;
import io.phasetwo.service.util.IdentityProviders;
import org.keycloak.models.IdentityProviderModel;

public final class KeycloakOrgsExportConverter {
  public static OrganizationRepresentation convertOrganizationModelToOrganizationRepresentation(
      OrganizationModel organizationModel, boolean exportMembersAndInvitations) {
    var organization =
        KeycloakOrgsExportConverter.convertOrganizationModelToOrganizationAttributes(
            organizationModel);
    var roles =
        organizationModel
            .getRolesStream()
            .map(KeycloakOrgsExportConverter::convertOrganizationRoleRepresentation)
            .toList();
    var idpOptional =
        organizationModel
            .getIdentityProvidersStream()
            .filter(
                identityProviderModel -> idpInOrg(identityProviderModel, organizationModel.getId()))
            .map(IdentityProviderModel::getAlias)
            .findFirst();

    var organizationRepresentation = new OrganizationRepresentation();
    organizationRepresentation.setOrganization(organization);
    organizationRepresentation.setRoles(roles);
    idpOptional.ifPresent(organizationRepresentation::setIdpLink);

    if (exportMembersAndInvitations) {
      var members =
          organizationModel
              .getMembersStream()
              .filter(
                  userModel ->
                      !OrganizationResourceProviderFactory.getDefaultAdminUsername(
                              organizationModel)
                          .contains(userModel.getUsername()))
              .map(
                  userModel -> {
                    var userRoles =
                        organizationModel
                            .getRolesByUserStream(userModel)
                            .map(OrganizationRoleModel::getName)
                            .toList();

                    return new UserRolesRepresentation(null, userModel.getUsername(), userRoles);
                  })
              .toList();
      organizationRepresentation.setMembers(members);

      var invitations =
          organizationModel
              .getInvitationsStream()
              .map(KeycloakOrgsExportConverter::convertInvitationModelToInvitationRepresentation)
              .toList();
      organizationRepresentation.setInvitations(invitations);
    }

    return organizationRepresentation;
  }

  private static InvitationRepresentation convertInvitationModelToInvitationRepresentation(
      InvitationModel invitationModel) {
    var i = new InvitationRepresentation();
    i.setEmail(invitationModel.getEmail());
    i.setInviterUsername(invitationModel.getInviter().getUsername());
    i.setRedirectUri(invitationModel.getUrl());
    i.setRoles(Lists.newArrayList(invitationModel.getRoles()));
    i.setAttributes(Maps.newHashMap(invitationModel.getAttributes()));
    return i;
  }

  private static OrganizationRoleRepresentation convertOrganizationRoleRepresentation(
      OrganizationRoleModel m) {
    var role = new OrganizationRoleRepresentation();
    role.setName(m.getName());
    role.setDescription(m.getDescription());
    return role;
  }

  private static OrganizationAttributes convertOrganizationModelToOrganizationAttributes(
      OrganizationModel e) {
    var o = new OrganizationAttributes();

    o.setName(e.getName());
    o.setDisplayName(e.getDisplayName());
    o.setDomains(e.getDomains());
    o.setUrl(e.getUrl());
    o.setAttributes(e.getAttributes());
    return o;
  }

  private static boolean idpInOrg(IdentityProviderModel provider, String orgId) {
    var orgs =
        IdentityProviders.getAttributeMultivalued(provider.getConfig(), ORG_OWNER_CONFIG_KEY);
    return orgs.contains(orgId);
  }
}
