package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.Converters.*;
import static io.phasetwo.service.resource.OrganizationResourceType.*;

import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationRoleModel;
import io.phasetwo.service.representation.OrganizationRole;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.stream.Stream;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.events.admin.OperationType;

@JBossLog
public class RolesResource extends OrganizationAdminResource {

  private final OrganizationModel organization;

  public RolesResource(OrganizationAdminResource parent, OrganizationModel organization) {
    super(parent);
    this.organization = organization;
  }

  @Path("{alias}")
  public RoleResource roles(@PathParam("alias") String name) {
    if (organization.getRoleByName(name) == null) {
      throw new NotFoundException();
    }
    return new RoleResource(this, organization, name);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Stream<OrganizationRole> getRoles() {
    return organization.getRolesStream().map(r -> convertOrganizationRole(r));
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createRole(OrganizationRole representation) {
    if (!auth.hasManageOrgs() && !auth.hasOrgManageRoles(organization)) {
      throw new NotAuthorizedException(
          String.format(
              "User %s doesn't have permission to manage roles in org %s",
              auth.getUser().getId(), organization.getName()));
    }
    OrganizationRoleModel r = organization.getRoleByName(representation.getName());
    if (r != null) {
      log.debug("duplicate role");
      throw new ClientErrorException(Response.Status.CONFLICT);
    }
    r = organization.addRole(representation.getName());
    r.setDescription(representation.getDescription());

    OrganizationRole or = convertOrganizationRole(r);
    adminEvent
        .resource(ORGANIZATION_ROLE.name())
        .operation(OperationType.CREATE)
        .resourcePath(session.getContext().getUri(), or.getName())
        .representation(or)
        .success();

    return Response.created(
            session.getContext().getUri().getAbsolutePathBuilder().path(or.getName()).build())
        .build();
  }
}
