package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.OrganizationResourceType.ORGANIZATION_TIER_MAPPING;

import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.representation.OrganizationTierMapping;
import io.phasetwo.service.representation.OrganizationTier;
import io.phasetwo.service.util.DateFormatter;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleContainerModel;
import org.keycloak.models.RoleModel;
import org.keycloak.services.ErrorResponseException;

@JBossLog
public class OrganizationTierMappingsResource extends OrganizationAdminResource {

  private static final String INVALID_REQ = "invalid_request";

  private final OrganizationModel organization;

  public OrganizationTierMappingsResource(
      OrganizationAdminResource parent, OrganizationModel organization) {
    super(parent);
    this.organization = organization;
  }

  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  public OrganizationTierMapping getRoleMappings() {

    List<OrganizationTier> realmTiers = new ArrayList<>();
    organization.getTierMappingsStream().forEach(t -> {
      RoleContainerModel container = t.getRole().getContainer();

      if (container instanceof RealmModel) {
        OrganizationTier realmTier = Converters.convertTierModelToOrganizationTier(t);
        realmTiers.add(realmTier);
      }
    });

    OrganizationTierMapping organizationRoleMapping = new OrganizationTierMapping();
    organizationRoleMapping.setRealmMappings(realmTiers);

    return organizationRoleMapping;
  }

  @PUT
  @Path("realm")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response add(List<OrganizationTier> tiers) {
    if (!(auth.hasCreateOrg() || (auth.hasViewOrgs() && auth.hasManageOrgs()))) {
      log.warnf("Unauthorized action: ADD tier on organization '%s' by '%s'",
          organization, auth.getUser().getId());
      throw new NotAuthorizedException("Insufficient permission to manage tiers.");
    }

    try {
      for (OrganizationTier tier : tiers) {
        RoleModel roleModel = realm.getRoleById(tier.getRole().getId());
        if (roleModel == null) {
          throw new NotFoundException("Role not found");
        }
        organization.addTier(roleModel, DateFormatter.parse(tier.getExpireDate()));
      }

    } catch (ModelException me) {
      log.error(me.getMessage(), me);
      throw new ErrorResponseException(
          INVALID_REQ, "Could not add organization role mappings!",
          Response.Status.BAD_REQUEST);

    } catch (ParseException pe) {
      log.error(pe.getMessage(), pe);
      throw new ErrorResponseException(
          INVALID_REQ, "Could not parse date format, expected format: yyyy-MM-dd",
          Response.Status.BAD_REQUEST);
    }

    adminEvent
        .resource(ORGANIZATION_TIER_MAPPING.name())
        .operation(OperationType.CREATE)
        .resourcePath(session.getContext().getUri())
        .representation(tiers)
        .success();

    return Response.created(
        session.getContext().getUri().getAbsolutePathBuilder().build())
        .build();
  }

  @DELETE
  @Path("realm")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response remove(List<OrganizationTier> tiers) {
    if (!(auth.hasCreateOrg() || (auth.hasViewOrgs() && auth.hasManageOrgs()))) {
      log.warnf("Unauthorized action: REMOVE tier from organization '%s' by '%s'",
          organization, auth.getUser().getId());
      throw new NotAuthorizedException("Insufficient permission to manage tiers.");
    }

    try {
      for (OrganizationTier tier : tiers) {
        RoleModel roleModel = realm.getRoleById(tier.getRole().getId());
        if (roleModel == null) {
          throw new NotFoundException("Role not found");
        }
        organization.removeTier(roleModel);
      }
    } catch (ModelException me) {
      log.warn(me.getMessage(), me);
      throw new ErrorResponseException(
          INVALID_REQ, "Could not remove organization role mappings!",
          Response.Status.BAD_REQUEST);
    }

    adminEvent
        .resource(ORGANIZATION_TIER_MAPPING.name())
        .operation(OperationType.DELETE)
        .resourcePath(session.getContext().getUri())
        .representation(tiers)
        .success();

    return Response.noContent().build();
  }
}
