package io.phasetwo.service.resource;

import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationRoleModel;
import io.phasetwo.service.representation.BulkResponseItem;
import io.phasetwo.service.representation.OrganizationRole;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.events.admin.OperationType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static io.phasetwo.service.resource.Converters.convertOrganizationRole;
import static io.phasetwo.service.resource.OrganizationResourceType.ORGANIZATION_ROLE;

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
    return new RoleResource(this, organization, name, this::deleteOrganizationRole);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Stream<OrganizationRole> getRoles() {
    return organization.getRolesStream().map(r -> convertOrganizationRole(r));
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createRole(OrganizationRole representation) {
    canManage();

    OrganizationRole or = createOrganizationRole(representation);

    return Response.created(
            session.getContext().getUri().getAbsolutePathBuilder().path(or.getName()).build())
        .build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createRoles(List<OrganizationRole> representation) {
    canManage();

    List<BulkResponseItem> responseItems = new ArrayList<>();

    representation.forEach(role -> {
      BulkResponseItem item = new BulkResponseItem()
        .status(Response.Status.CREATED.getStatusCode());
      try {
        item.setItem(createOrganizationRole(role));
      } catch (Exception ex){
        item.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
        item.setError(ex.getMessage());
      }
      responseItems.add(item);
    });

    return Response
            .status(207) //<-Multi-Status
            .location(session.getContext().getUri().getAbsolutePathBuilder().build())
            .entity(responseItems)
            .build();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteRoles(List<OrganizationRole> representation) {
    canManage();

    List<BulkResponseItem> responseItems = new ArrayList<>();

    representation.forEach(role->{
      BulkResponseItem item = new BulkResponseItem()
        .status(Response.Status.NO_CONTENT.getStatusCode());
      try {
        deleteOrganizationRole(role.getName());
        item.setItem(role);
      } catch (Exception ex){
        item.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
        item.setError(ex.getMessage());
      }
      responseItems.add(item);
    });

    return Response
            .status(207) //<-Multi-Status
            .location(session.getContext().getUri().getAbsolutePathBuilder().build())
            .entity(responseItems)
            .build();
  }

  private OrganizationRole createOrganizationRole(OrganizationRole representation) {
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
    return or;
  }

  public void deleteOrganizationRole(String roleName) {
    if (Arrays.asList(OrganizationAdminAuth.DEFAULT_ORG_ROLES).contains(roleName)) {
      throw new BadRequestException(
              String.format("Default organization role %s cannot be deleted.", roleName));
    }

    organization.removeRole(roleName);

    adminEvent
        .resource(ORGANIZATION_ROLE.name())
        .operation(OperationType.DELETE)
        .resourcePath(session.getContext().getUri(), roleName)
        .representation(roleName)
        .success();
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
