package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.Converters.*;
import static io.phasetwo.service.resource.OrganizationResourceType.*;
import static org.keycloak.models.utils.ModelToRepresentation.*;

import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationRoleModel;
import io.phasetwo.service.representation.OrganizationRole;
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.validation.constraints.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.resources.admin.AdminRoot;

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

  @DELETE
  public Response deleteRole() {
    canManage();

    if (Arrays.asList(OrganizationAdminAuth.DEFAULT_ORG_ROLES).contains(name)) {
      throw new BadRequestException(
          String.format("Default organization role %s cannot be deleted.", name));
    }

    organization.removeRole(name);
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
    if (user != null && role.hasRole(user)) {
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
                "User %s must be a member of %s to be granted role.",
                userId, organization.getName()));
      }
      if (!role.hasRole(user)) {
        role.grantRole(user);
      }

      // /auth/realms/:realm/orgs/:orgId/roles/:name/users/:userId"
      URI location =
          AdminRoot.realmsUrl(session.getContext().getUri())
              .path(realm.getName())
              .path("orgs")
              .path(organization.getId())
              .path("roles")
              .path(role.getName())
              .path("users")
              .path(userId)
              .build();
      return Response.created(location).build();
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
    if (user != null && role.hasRole(user)) {
      role.revokeRole(user);
      return Response.noContent().build();
    } else {
      throw new NotFoundException(String.format("User %s doesn't have role %s", userId, name));
    }
  }

  private void canManage() {
    if (!auth.hasManageOrgs() && !auth.hasOrgManageRoles(organization)) {
      throw new NotAuthorizedException(
          String.format(
              "User %s doesn't have permission to manage roles in org %s",
              auth.getUser().getId(), organization.getName()));
    }
  }
}
