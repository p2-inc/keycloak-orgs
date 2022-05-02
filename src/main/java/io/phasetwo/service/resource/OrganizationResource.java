package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.Converters.*;
import static io.phasetwo.service.resource.OrganizationAdminAuth.DEFAULT_ORG_ROLES;
import static io.phasetwo.service.resource.OrganizationResourceType.*;

import com.google.common.collect.ImmutableMap;
import io.phasetwo.service.auth.action.PortalLinkActionToken;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationRoleModel;
import io.phasetwo.service.representation.Organization;
import java.io.IOException;
import java.net.URI;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.common.util.Time;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.Urls;
import org.keycloak.services.resources.LoginActionsService;
import org.keycloak.services.resources.RealmsResource;
import org.keycloak.theme.Theme;

@JBossLog
public class OrganizationResource extends OrganizationAdminResource {

  protected final OrganizationModel organization;
  protected final String orgId;

  public OrganizationResource(RealmModel realm, OrganizationModel organization) {
    super(realm);
    this.organization = organization;
    this.orgId = organization.getId();
  }

  @Path("idps")
  public IdentityProvidersResource identityProviders() {
    if (auth.hasViewOrgs() || auth.hasOrgViewIdentityProviders(organization)) {
      return setupResource(new IdentityProvidersResource(realm, organization));
    } else {
      throw new NotAuthorizedException(
          String.format(
              "Insufficient permission to access identity providers for %s", organization.getId()));
    }
  }

  @Path("roles")
  public RolesResource roles() {
    if (auth.hasViewOrgs() || auth.hasOrgViewRoles(organization)) {
      return setupResource(new RolesResource(realm, organization));
    } else {
      throw new NotAuthorizedException(
          String.format("Insufficient permission to access role for %s", organization.getId()));
    }
  }

  @Path("invitations")
  public InvitationsResource invitations() {
    if (auth.hasViewOrgs() || auth.hasOrgViewInvitations(organization)) {
      return setupResource(new InvitationsResource(realm, organization));
    } else {
      throw new NotAuthorizedException(
          String.format(
              "Insufficient permission to access invitation for %s", organization.getId()));
    }
  }

  @Path("members")
  public MembersResource members() {
    if (auth.hasViewOrgs() || auth.hasOrgViewMembers(organization)) {
      return setupResource(new MembersResource(realm, organization));
    } else {
      throw new NotAuthorizedException(
          String.format("Insufficient permission to access member for %s", organization.getId()));
    }
  }

  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getOrg() {
    log.infof("Get org for %s %s", realm.getName(), orgId);
    return Response.ok().entity(convertOrganizationModelToOrganization(organization)).build();
  }

  @DELETE
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteOrg() {
    log.infof("Delete org for %s %s", realm.getName(), orgId);

    auth.requireManageOrgs();

    if (orgs.removeOrganization(realm, orgId)) {
      adminEvent
          .resource(ORGANIZATION.name())
          .operation(OperationType.DELETE)
          .resourcePath(session.getContext().getUri(), orgId)
          .success();
    }
    return Response.status(204).build();
  }

  @PUT
  @Path("")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateOrg(@Valid Organization body) {
    log.infof("Update org for %s", realm.getName());

    if (auth.hasManageOrgs() || auth.hasOrgManageOrg(organization)) {

      organization.setName(body.getName());
      organization.setDisplayName(body.getDisplayName());
      organization.setUrl(body.getUrl());
      organization.removeAttributes();
      body.getAttributes().forEach((k, v) -> organization.setAttribute(k, v));
      if (body.getDomains() != null) organization.setDomains(body.getDomains());

      Organization o = convertOrganizationModelToOrganization(organization);

      adminEvent
          .resource(ORGANIZATION.name())
          .operation(OperationType.UPDATE)
          .resourcePath(session.getContext().getUri(), o.getId())
          .representation(o)
          .success();

      return Response.noContent().build();
    } else {
      throw new NotAuthorizedException(
          String.format("Insufficient permission to modify %s", organization.getId()));
    }
  }

  /////////////////////////////////////////////////////
  // phasetwo internal code
  /////////////////////////////////////////////////////

  private static final String IDP_WIZARD_CLIENT = "idp-wizard";
  private static final String WIZARD_THEME = "wizard";

  private Theme getTheme(String name) {
    try {
      return session.theme().getTheme(name, Theme.Type.LOGIN);
    } catch (IOException e) {
      return null;
    }
  }

  @POST
  @Path("portal-link")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPortalLink(
      @DefaultValue("") @FormParam("userId") String userId,
      @DefaultValue("") @FormParam("baseUri") String baseUri) {
    // check for existence of idp-wizard client and wizard theme
    ClientModel idpWizardClient = session.clients().getClientByClientId(realm, IDP_WIZARD_CLIENT);
    Theme wizardTheme = getTheme(WIZARD_THEME);
    if (idpWizardClient == null || wizardTheme == null) {
      throw new BadRequestException(
          "portal-link is only supported in paid Phase Two distributions");
    }

    if (!auth.hasManageOrgs() && !auth.hasOrgManageOrg(organization)) {
      throw new NotAuthorizedException(
          String.format(
              "Insufficient permission to create portal link for %s", organization.getId()));
    }
    try {
      UriInfo uriInfo = session.getContext().getUri();
      URI base = "".equals(baseUri) ? uriInfo.getBaseUri() : newUri(baseUri);
      String redirectUri =
          Urls.realmBase(base).path(realm.getName()).path("wizard").build().toString();
      log.infof("%s redirectUri %s", IDP_WIZARD_CLIENT, redirectUri);

      UserModel user = null;
      if (userId != null && !"".equals(userId)) {
        user = session.users().getUserById(realm, userId);
      }
      if (user == null) {
        user =
            session
                .users()
                .getUserByUsername(realm, String.format("org-admin-%s", organization.getId()));
      }
      if (user == null) {
        throw new BadRequestException(String.format("User %s not found", userId));
      }
      log.infof("Using user %s (%s) for portal-link", user.getUsername(), user.getId());

      // check membership and roles
      if (!organization.hasMembership(user)) {
        throw new BadRequestException(
            String.format("User %s is not a member of this organization", userId));
      }
      for (String role : DEFAULT_ORG_ROLES) {
        OrganizationRoleModel roleModel = organization.getRoleByName(role);
        if (!roleModel.hasRole(user)) {
          throw new BadRequestException(
              String.format("User has insufficient permissions. Needs %s.", role));
        }
      }

      // build the action token
      int validityInSecs = 60 * 60 * 24; // 1 day
      int absoluteExpirationInSecs = Time.currentTime() + validityInSecs;
      PortalLinkActionToken token =
          new PortalLinkActionToken(
              user.getId(),
              absoluteExpirationInSecs,
              organization.getId(),
              IDP_WIZARD_CLIENT,
              redirectUri);
      UriBuilder builder =
          actionTokenBuilder(
              uriInfo.getBaseUri(), token.serialize(session, realm, uriInfo), IDP_WIZARD_CLIENT);
      String link = builder.build(realm.getName()).toString();
      log.debugf("Created portal link for %s: %s", user.getUsername(), link);
      return Response.ok()
          .entity(ImmutableMap.of("user", user.getId(), "link", link, "redirect", redirectUri))
          .build();

    } catch (Exception e) {
      log.warn("Error creating portal link", e);
    }
    return Response.serverError().build();
  }

  private UriBuilder actionTokenBuilder(URI baseUri, String tokenString, String clientId) {
    log.debugf("baseUri: %s, tokenString: %s, clientId: %s", baseUri, tokenString, clientId);
    return Urls.realmBase(baseUri)
        .path(RealmsResource.class, "getLoginActionsService")
        .path(LoginActionsService.class, "executeActionToken")
        .queryParam(Constants.KEY, tokenString)
        .queryParam(Constants.CLIENT_ID, clientId);
  }

  private URI newUri(String u) {
    try {
      return new URI(u);
    } catch (Exception e) {
      log.warnf(e, "Error creating URI from %s", u);
    }
    return null;
  }

  /*
  teams is on hold for now

    @GET
    @Path("teams")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listTeams(
        @QueryParam("search") String searchQuery,
        @QueryParam("first") Integer firstResult,
        @QueryParam("max") Integer maxResults) {
      log.infof("Get teams for %s %s", realm.getName(), orgId);
      Optional<String> search = Optional.ofNullable(searchQuery);
      firstResult = firstResult != null ? firstResult : 0;
      maxResults = maxResults != null ? maxResults : Constants.DEFAULT_MAX_RESULTS;
      Teams teams =
          mgr
              .getTeamsByOrganizationId(
                  orgId, search, Optional.ofNullable(firstResult), Optional.ofNullable(maxResults))
              .stream()
              .map(e -> convertTeamEntityToTeam(e))
              .collect(Collectors.toCollection(() -> new Teams()));
      return Response.ok().entity(teams).build();
    }

    @POST
    @Path("teams")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTeam(@Valid Team body) {
      log.infof("Create team for %s %s", realm.getName(), orgId);

      TeamEntity e = mgr.createTeamForOrganization(orgId, body);
      Team o = convertTeamEntityToTeam(e);

      adminEvent
          .resource(TEAM.name())
          .operation(OperationType.CREATE)
          .resourcePath(session.getContext().getUri(), e.getId())
          .representation(o)
          .success();

      return Response.ok().entity(o).build();
    }
    */
}
