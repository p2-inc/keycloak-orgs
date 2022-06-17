package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.Converters.*;
import static io.phasetwo.service.resource.OrganizationResourceType.*;

import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.representation.Organization;
import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.Constants;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.admin.AdminRoot;

@JBossLog
public class OrganizationsResource extends OrganizationAdminResource {

  public OrganizationsResource(RealmModel realm) {
    super(realm);
  }

  @Path("{orgId}")
  public OrganizationResource getOrg(@PathParam("orgId") String orgId) {
    OrganizationModel org = orgs.getOrganizationById(realm, orgId);
    if (org == null) throw new NotFoundException(String.format("%s not found", orgId));
    if ((auth.hasViewOrgs() || auth.hasOrgViewOrg(org)) && auth.isOrgInRealm(org)) {
      OrganizationResource resource = new OrganizationResource(realm, org);
      ResteasyProviderFactory.getInstance().injectProperties(resource);
      resource.setup();
      return resource;
    } else {
      throw new NotAuthorizedException(
          String.format("Insufficient permission to access %s", orgId));
    }
  }

  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  public Stream<Organization> listOrgs(
      @QueryParam("search") String searchQuery,
      @QueryParam("first") Integer firstResult,
      @QueryParam("max") Integer maxResults) {
    log.infof("Get orgs for %s", realm.getName());
    log.debugf("listOrgs %s %d %d", searchQuery, firstResult, maxResults);
    Optional<String> search = Optional.ofNullable(searchQuery);
    firstResult = firstResult != null ? firstResult : 0;
    maxResults = maxResults != null ? maxResults : Constants.DEFAULT_MAX_RESULTS;
    return orgs.searchForOrganizationByNameStream(realm, searchQuery, firstResult, maxResults)
        .filter(m -> (auth.hasViewOrgs() || auth.hasOrgViewOrg(m)))
        .map(m -> convertOrganizationModelToOrganization(m));
  }

  @POST
  @Path("")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createOrg(@Valid Organization body) {
    log.infof("Create org for %s", realm.getName());
    if (!(auth.hasCreateOrg() || (auth.hasViewOrgs() && auth.hasManageOrgs()))) {
      throw new NotAuthorizedException("Insufficient permission to create organization.");
    }

    OrganizationModel org =
        orgs.createOrganization(realm, body.getName(), auth.getUser(), auth.hasCreateOrg());
    org.setDisplayName(body.getDisplayName());
    org.setUrl(body.getUrl());
    body.getAttributes().forEach((k, v) -> org.setAttribute(k, v));
    if (body.getDomains() != null) org.setDomains(body.getDomains());

    Organization o = convertOrganizationModelToOrganization(org);

    adminEvent
        .resource(ORGANIZATION.name())
        .operation(OperationType.CREATE)
        .resourcePath(session.getContext().getUri(), o.getId())
        .representation(o)
        .success();

    // /auth/realms/:realm/orgs/:orgId/roles/:name"
    URI location =
        AdminRoot.realmsUrl(session.getContext().getUri())
            .path(realm.getName())
            .path("orgs")
            .path(o.getId())
            .build();
    return Response.created(location).build();
  }
}
