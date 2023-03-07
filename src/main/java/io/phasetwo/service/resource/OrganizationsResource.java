package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.Converters.*;
import static io.phasetwo.service.resource.OrganizationResourceType.*;

import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.representation.Organization;
import java.util.Optional;
import java.util.stream.Stream;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;

@JBossLog
public class OrganizationsResource extends OrganizationAdminResource {

  public OrganizationsResource(KeycloakSession session) {
    super(session);
  }

  @Path("{orgId}")
  public OrganizationResource getOrg(@PathParam("orgId") String orgId) {
    OrganizationModel org = orgs.getOrganizationById(realm, orgId);
    if (org == null) throw new NotFoundException(String.format("%s not found", orgId));
    if ((auth.hasViewOrgs() || auth.hasOrgViewOrg(org)) && auth.isOrgInRealm(org)) {
      return new OrganizationResource(this, org);
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
    log.debugf("listOrgs %s %s %d %d", realm.getName(), searchQuery, firstResult, maxResults);
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
    if (body.getAttributes() != null)
      body.getAttributes().forEach((k, v) -> org.setAttribute(k, v));
    if (body.getDomains() != null) org.setDomains(body.getDomains());

    Organization o = convertOrganizationModelToOrganization(org);

    adminEvent
        .resource(ORGANIZATION.name())
        .operation(OperationType.CREATE)
        .resourcePath(session.getContext().getUri(), o.getId())
        .representation(o)
        .success();

    return Response.created(
            session.getContext().getUri().getAbsolutePathBuilder().path(o.getId()).build())
        .build();
  }
}
