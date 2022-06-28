package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.Converters.*;
import static io.phasetwo.service.resource.OrganizationResourceType.*;

import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationRoleModel;
import io.phasetwo.service.representation.OrganizationRole;
import java.net.URI;
import java.util.stream.Stream;
import javax.validation.constraints.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.admin.AdminRoot;

@JBossLog
public class RolesResource extends OrganizationAdminResource {

  private final OrganizationModel organization;

  public RolesResource(RealmModel realm, OrganizationModel organization) {
    super(realm);
    this.organization = organization;
  }

  @Path("{alias}")
  public RoleResource roles(@PathParam("alias") String name) {
    if (organization.getRoleByName(name) == null) {
      throw new NotFoundException();
    }
    RoleResource resource = new RoleResource(realm, organization, name);
    ResteasyProviderFactory.getInstance().injectProperties(resource);
    resource.setup();
    return resource;
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
    // /auth/realms/:realm/orgs/:orgId/roles/:name"
    URI location =
        AdminRoot.realmsUrl(session.getContext().getUri())
            .path(realm.getName())
            .path("orgs")
            .path(organization.getId())
            .path("roles")
            .path(r.getName())
            .build();
    return Response.created(location).build();
  }
}
