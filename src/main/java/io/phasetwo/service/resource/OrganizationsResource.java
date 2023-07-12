package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.Converters.*;
import static io.phasetwo.service.resource.OrganizationResourceType.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.representation.Organization;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.utils.SearchQueryUtils;

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
  @Path("me")
  @Produces(MediaType.APPLICATION_JSON)
  public Response me() {
    Map<String, Object> claim = Maps.newHashMap();
    orgs.getUserOrganizationsStream(realm, user)
        .forEach(
            o -> {
              List<String> roles = Lists.newArrayList();
              o.getRolesStream()
                  .forEach(
                      r -> {
                        if (r.hasRole(user)) roles.add(r.getName());
                      });
              Map<String, Object> org = Maps.newHashMap();
              org.put("name", o.getName());
              if (o.getDisplayName() != null) org.put("displayName", o.getDisplayName());
              if (o.getUrl() != null) org.put("url", o.getUrl());
              org.put("attributes", o.getAttributes());
              org.put("roles", roles);
              claim.put(o.getId(), org);
            });
    return Response.ok(claim).build();
  }

  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  public Stream<Organization> listOrgs(
      @QueryParam("search") String search,
      @QueryParam("first") Integer firstResult,
      @QueryParam("max") Integer maxResults,
      @QueryParam("q") String searchQuery) {
    firstResult = firstResult != null ? firstResult : 0;
    maxResults =
        (maxResults != null && maxResults <= Constants.DEFAULT_MAX_RESULTS)
            ? maxResults
            : Constants.DEFAULT_MAX_RESULTS;

    log.debugf(
        "listOrgs realm: %s, search: %s, query: %s, first: %d, max: %d",
        realm.getName(), search, searchQuery, firstResult, maxResults);

    Map<String, String> searchAttributes =
        searchQuery == null ? Maps.newHashMap() : SearchQueryUtils.getFields(searchQuery);
    if (search != null) {
      searchAttributes.put("name", search.trim());
    }

    return orgs.searchForOrganizationStream(
            realm,
            searchAttributes,
            firstResult,
            maxResults,
            auth.hasViewOrgs() ? Optional.empty() : Optional.of(auth.getUser()))
        .filter(m -> (auth.hasViewOrgs() || auth.hasOrgViewOrg(m)))
        .map(m -> convertOrganizationModelToOrganization(m));
  }

  @GET
  @Path("count")
  @Produces(MediaType.APPLICATION_JSON)
  public Long countOrgs(@QueryParam("search") String searchQuery) {
    log.debugf("countOrgs %s %s", realm.getName(), searchQuery);
    if (!auth.hasViewOrgs()) {
      throw new NotAuthorizedException("Insufficient permission to count organizations.");
    }
    return orgs.getOrganizationsCount(realm, searchQuery);
  }

  @POST
  @Path("")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createOrg(@Valid Organization body) {
    log.debugf("Create org for %s", realm.getName());
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
