package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.Converters.*;
import static io.phasetwo.service.resource.OrganizationResourceType.*;
import static org.keycloak.models.utils.ModelToRepresentation.*;

import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationRoleModel;
import io.phasetwo.service.representation.OrganizationRole;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.UserRepresentation;

@JBossLog
public class RoleResource extends OrganizationAdminResource {

  private final OrganizationModel organization;
  private final OrganizationRoleModel role;
  private final String name;

  public RoleResource(RealmModel realm, OrganizationModel organization, String name) {
    super(realm);
    this.organization = organization;
    this.role = organization.getRoleByName(name);
    this.name = name;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public OrganizationRole getRole() {
    return convertOrganizationRole(role);
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateRole(OrganizationRole representation) {
    canManage();

    if (!Objects.equals(role.getDescription(), representation.getDescription())) {
      role.setDescription(representation.getDescription());

      OrganizationRole or = convertOrganizationRole(role);
      adminEvent
          .resource(ORGANIZATION_ROLE.name())
          .operation(OperationType.UPDATE)
          .resourcePath(session.getContext().getUri(), or.getName())
          .representation(or)
          .success();
    }

    return Response.noContent().build();
  }

  @DELETE
  public Response deleteRole() {
    canManage();

    if (Arrays.asList(OrganizationAdminAuth.DEFAULT_ORG_ROLES).contains(name)) {
      throw new BadRequestException(
          String.format("Default organization role '%s' cannot be deleted.", name));
    }

    organization.removeRole(name);

    adminEvent
        .resource(ORGANIZATION_ROLE.name())
        .operation(OperationType.DELETE)
        .resourcePath(session.getContext().getUri(), name)
        .success();

    return Response.noContent().build();
  }

  @GET
  @Path("users")
  @Produces(MediaType.APPLICATION_JSON)
  public Stream<UserRepresentation> users() {
    return role.getUserMappingsStream().map(m -> toRepresentation(session, realm, m));
  }

  @GET
  @Path("users/{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response userHasRole(@PathParam("userId") String userId) {
    UserModel user = session.users().getUserById(realm, userId);
    if (user != null && role.hasDirectRole(user)) {
      return Response.noContent().build();
    } else {
      throw new NotFoundException(String.format("User %s doesn't have role %s", userId, name));
    }
  }

  @PUT
  @Path("users/{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response grantUserRole(@PathParam("userId") String userId) {
    canManage();

    UserModel user = session.users().getUserById(realm, userId);
    if (user != null) {
      if (!organization.hasMembership(user)) {
        throw new BadRequestException(
            String.format(
                "User '%s' must be a member of '%s' to be granted role.",
                userId, organization.getName()));
      }
      if (!role.hasDirectRole(user)) {
        role.grantRole(user);

        adminEvent
            .resource(ORGANIZATION_ROLE_MAPPING.name())
            .operation(OperationType.CREATE)
            .resourcePath(session.getContext().getUri())
            .representation(userId)
            .success();
      }

      return Response.created(session.getContext().getUri().getAbsolutePathBuilder().build())
          .build();
    } else {
      throw new NotFoundException(String.format("User %s doesn't exist", userId));
    }
  }

  @DELETE
  @Path("users/{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response revokeUserRole(@PathParam("userId") String userId) {
    canManage();

    UserModel user = session.users().getUserById(realm, userId);
    if (user != null && role.hasDirectRole(user)) {
      role.revokeRole(user);
      adminEvent
          .resource(ORGANIZATION_ROLE_MAPPING.name())
          .operation(OperationType.DELETE)
          .resourcePath(session.getContext().getUri())
          .representation(userId)
          .success();
      return Response.noContent().build();
    } else {
      throw new NotFoundException(String.format("User %s doesn't have role %s", userId, name));
    }
  }

  private void canManage() {
    if (!auth.hasManageOrgs() && !auth.hasOrgManageRoles(organization)) {
      throw notAuthorized(OrganizationAdminAuth.ORG_ROLE_MANAGE_ROLES, organization);
    }
  }
}
