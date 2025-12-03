package io.phasetwo.service.resource;

import static io.phasetwo.service.Orgs.*;
import static io.phasetwo.service.resource.Converters.convertOrganizationModelToOrganization;
import static io.phasetwo.service.resource.OrganizationResourceType.ORGANIZATION;
import static io.phasetwo.service.resource.OrganizationResourceType.ORGANIZATION_IMPORT;
import static org.keycloak.events.EventType.CUSTOM_REQUIRED_ACTION;

import com.google.common.collect.Maps;
import io.phasetwo.service.importexport.KeycloakOrgsExportConverter;
import io.phasetwo.service.importexport.KeycloakOrgsImportConverter;
import io.phasetwo.service.importexport.representation.KeycloakOrgsRepresentation;
import io.phasetwo.service.importexport.representation.OrganizationRepresentation;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.model.OrganizationRoleModel;
import io.phasetwo.service.representation.Invitation;
import io.phasetwo.service.representation.Organization;
import io.phasetwo.service.representation.OrganizationsConfig;
import io.phasetwo.service.util.Invitations;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.common.util.CollectionUtil;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.*;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.resources.admin.AdminEventBuilder;
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
              List<String> roles =
                  o.getRolesByUserStream(user).map(OrganizationRoleModel::getName).toList();
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
  @Path("me/invitations")
  @Produces(MediaType.APPLICATION_JSON)
  public Stream<Invitation> invitations() {
    var accessToken = auth.getToken();

    if (!canManageInvitations(accessToken)) {
      throw new BadRequestException("User needs to be email verified");
    }

    return orgs.getUserInvitationsStream(realm, accessToken.getEmail())
        .map(Converters::convertInvitationModelToInvitation);
  }

  @POST
  @Path("me/invitations/{invitationId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response acceptInvitation(@PathParam("invitationId") String invitationId) {
    var accessToken = auth.getToken();

    if (!canManageInvitations(accessToken)) {
      throw new BadRequestException("User needs to have a verified email");
    }

    var invitation = orgs.getInvitationById(realm, invitationId);
    if (invitation == null) {
      log.errorf("invitation with id: {} not found", invitationId);
      throw new NotFoundException(String.format("Invitation %s not found", invitationId));
    }

    if (!accessToken.getEmail().equals(invitation.getEmail())) {
      log.errorf("email claim differ from invitation email");
      throw new BadRequestException("Email claim differs from invitation email");
    }

    KeycloakModelUtils.runJobInTransaction(
        session.getKeycloakSessionFactory(),
        session.getContext(),
        (session) -> {
          Invitations.memberFromInvitation(invitation, auth.getUser());
          invitation.getOrganization().revokeInvitation(invitationId);
          EventBuilder event = new EventBuilder(realm, session, connection);

          event
              .event(CUSTOM_REQUIRED_ACTION)
              .user(user)
              .detail("org_id", invitation.getOrganization().getId())
              .detail("invitation_id", invitation.getId())
              .success();
        });

    return Response.noContent().build();
  }

  @DELETE
  @Path("me/invitations/{invitationId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response rejectInvitation(@PathParam("invitationId") String invitationId) {
    var accessToken = auth.getToken();

    if (!canManageInvitations(accessToken)) {
      throw new BadRequestException("User needs to be email verified");
    }

    var invitation = orgs.getInvitationById(realm, invitationId);

    if (invitation == null) {
      log.errorf("invitation with id: {} not found", invitationId);
      throw new BadRequestException("invitation not found");
    }

    if (!accessToken.getEmail().equals(invitation.getEmail())) {
      log.errorf("`email` claim differ from invitation email");
      throw new BadRequestException("`email` claim differ from invitation email");
    }

    invitation.getOrganization().revokeInvitation(invitationId);

    EventBuilder event = new EventBuilder(realm, session, connection);

    event
        .event(CUSTOM_REQUIRED_ACTION)
        .detail("org_id", invitation.getOrganization().getId())
        .detail("invitation_id", invitation.getId())
        .user(user)
        .error("User invitation revoked.");

    return Response.noContent().build();
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
        .map(Converters::convertOrganizationModelToOrganization);
  }

  @GET
  @Path("count")
  @Produces(MediaType.APPLICATION_JSON)
  public Long countOrgs(
      @QueryParam("search") String searchQuery, @QueryParam("q") String searchAttributes) {

    log.debugf(
        "countOrgs realm: %s, search: %s, query: %s",
        realm.getName(), searchQuery, searchAttributes);

    if (!auth.hasViewOrgs()) {
      throw new NotAuthorizedException("Insufficient permission to count organizations.");
    }

    Map<String, String> attributes =
        (searchAttributes == null)
            ? Maps.newHashMap()
            : SearchQueryUtils.getFields(searchAttributes);

    if (searchQuery != null) {
      attributes.put("name", searchQuery.trim());
    }

    return orgs.getOrganizationsCount(realm, searchQuery, attributes);
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

    OrganizationModel org;
    if (body.getId() != null && !body.getId().isEmpty()) {
      org =
          orgs.createOrganization(
              realm, body.getId(), body.getName(), auth.getUser(), auth.hasCreateOrg());
    } else {
      org = orgs.createOrganization(realm, body.getName(), auth.getUser(), auth.hasCreateOrg());
    }

    org.setDisplayName(body.getDisplayName());
    org.setUrl(body.getUrl());
    if (body.getAttributes() != null) body.getAttributes().forEach(org::setAttribute);
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

  @PUT
  @Path("config")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response addOrganizationsConfig(@Valid OrganizationsConfig body) {
    log.debugf("Create org config for realm %s", realm.getName());
    if (!auth.hasManageRealm()) {
      throw new NotAuthorizedException("Insufficient permission to update organization config.");
    }
    resetIdentityProviders(body.isSharedIdps());

    realm.setAttribute(ORG_CONFIG_CREATE_ADMIN_USER_KEY, body.isCreateAdminUser());
    realm.setAttribute(ORG_CONFIG_SHARED_IDPS_KEY, body.isSharedIdps());
    realm.setAttribute(ORG_CONFIG_MULTIPLE_IDPS_KEY, body.isMultipleIdps());
    realm.setAttribute(ORG_CONFIG_PORTAL_LINK_EXPIRATION_KEY, body.getExpirationInSecs());
    realm.setAttribute(ORG_CONFIG_DEFAULT_APPLICATION_URI, body.getDefaultApplicationUri());

    return Response.ok(body).build();
  }

  private void resetIdentityProviders(boolean newSharedIdpConfig) {
    var existingSharedIdpConfig = realm.getAttribute(ORG_CONFIG_SHARED_IDPS_KEY, false);
    if (existingSharedIdpConfig && !newSharedIdpConfig) {
      session
          .identityProviders()
          .getAllStream()
          .forEach(
              identityProviderModel -> {
                identityProviderModel.getConfig().put(ORG_SHARED_IDP_KEY, "false");
                identityProviderModel.getConfig().put(ORG_OWNER_CONFIG_KEY, null);

                session.identityProviders().update(identityProviderModel);
              });
    }
  }

  @GET
  @Path("config")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getOrganizationConfig() {
    log.debugf("Create org config for realm %s", realm.getName());
    if (!auth.hasManageRealm()) {
      throw new NotAuthorizedException("Insufficient permission to update organization config.");
    }

    var representation = new OrganizationsConfig();
    representation.setCreateAdminUser(realm.getAttribute(ORG_CONFIG_CREATE_ADMIN_USER_KEY, true));
    representation.setSharedIdps(realm.getAttribute(ORG_CONFIG_SHARED_IDPS_KEY, false));
    representation.setMultipleIdps(realm.getAttribute(ORG_CONFIG_MULTIPLE_IDPS_KEY, false));
    representation.setExpirationInSecs(
        realm.getAttribute(ORG_CONFIG_PORTAL_LINK_EXPIRATION_KEY, 86400));

    return Response.ok(representation).build();
  }

  @GET
  @Path("export")
  @Produces(MediaType.APPLICATION_JSON)
  public Response exportOrgs(
      @QueryParam("exportMembersAndInvitations") Boolean exportMembersAndInvitations) {
    log.debugf("Export org for %s", realm.getName());

    boolean membersAndInvitationsExported =
        exportMembersAndInvitations != null && exportMembersAndInvitations;
    if (!auth.hasManageOrgs()) {
      throw new NotAuthorizedException("Insufficient permission to export organization.");
    }

    var organizations =
        orgs.searchForOrganizationStream(realm, Map.of(), 0, Integer.MAX_VALUE, Optional.empty())
            .map(
                organization ->
                    KeycloakOrgsExportConverter
                        .convertOrganizationModelToOrganizationRepresentation(
                            organization, membersAndInvitationsExported))
            .toList();

    KeycloakOrgsRepresentation keycloakOrgsRepresentation = new KeycloakOrgsRepresentation();
    keycloakOrgsRepresentation.setOrganizations(organizations);

    Response.ResponseBuilder response = Response.ok();
    response.type(MediaType.APPLICATION_JSON);
    response.entity(keycloakOrgsRepresentation);

    return response.build();
  }

  @POST
  @Path("import")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response importOrgs(
      KeycloakOrgsRepresentation keycloakOrgsRealmRepresentation,
      @QueryParam("skipMissingMember") Boolean skipMissingMember,
      @QueryParam("skipMissingIdp") Boolean skipMissingIdp) {

    log.debugf("Import orgs for %s", realm.getName());

    boolean missingMemberSkip = skipMissingMember != null && skipMissingMember;
    boolean missingIdpSkip = skipMissingIdp != null && skipMissingIdp;
    if (!(auth.hasViewOrgs() && auth.hasManageOrgs())) {
      throw new NotAuthorizedException("Insufficient permission to import organization.");
    }

    var organizations = keycloakOrgsRealmRepresentation.getOrganizations();

    if (CollectionUtil.isEmpty(organizations)) {
      Response.ResponseBuilder response = Response.noContent();
      response.type(MediaType.APPLICATION_JSON);
      return response.build();
    }

    KeycloakModelUtils.runJobInTransaction(
        session.getKeycloakSessionFactory(),
        (session) -> {
          session.getContext().setRealm(realm);
          organizations.forEach(
              organizationRepresentation ->
                  createOrganization(
                      missingMemberSkip, missingIdpSkip, session, organizationRepresentation));
          AdminEventBuilder adminEventClone = adminEvent.clone(session);

          // create import event
          adminEventClone
              .resource(ORGANIZATION_IMPORT.name())
              .operation(OperationType.CREATE)
              .resourcePath(session.getContext().getUri())
              .representation(keycloakOrgsRealmRepresentation)
              .success();
        });

    Response.ResponseBuilder response = Response.ok();
    response.type(MediaType.APPLICATION_JSON);

    return response.build();
  }

  private void createOrganization(
      boolean skipMissingMember,
      boolean skipMissingIdp,
      KeycloakSession session,
      OrganizationRepresentation organizationRepresentation) {
    try {
      var organizationProvider = session.getProvider(OrganizationProvider.class);
      OrganizationModel org;

      if (organizationRepresentation.getOrganization().getId() != null
          && !organizationRepresentation.getOrganization().getId().isEmpty()) {

        org =
            organizationProvider.createOrganization(
                realm,
                organizationRepresentation.getOrganization().getId(),
                organizationRepresentation.getOrganization().getName(),
                user,
                false);
      } else {
        org =
            organizationProvider.createOrganization(
                realm, organizationRepresentation.getOrganization().getName(), user, false);
      }

      KeycloakOrgsImportConverter.setOrganizationAttributes(
          organizationRepresentation.getOrganization(), org);

      KeycloakOrgsImportConverter.createOrganizationRoles(
          organizationRepresentation.getRoles(), org);

      KeycloakOrgsImportConverter.createOrganizationIdp(
          session, realm, organizationRepresentation.getIdpLink(), org, skipMissingIdp);

      KeycloakOrgsImportConverter.addMembers(
          session, realm, organizationRepresentation, org, skipMissingMember);

      KeycloakOrgsImportConverter.addInvitations(
          session, realm, organizationRepresentation, org, skipMissingMember);
    } catch (ModelDuplicateException e) {
      throw ErrorResponse.exists(
          "Duplicate organization with id: %s or name: %s"
              .formatted(
                  organizationRepresentation.getOrganization().getId(),
                  organizationRepresentation.getOrganization().getName()));
    } catch (ModelException e) {
      throw ErrorResponse.error(e.getMessage(), Response.Status.BAD_REQUEST);
    } catch (Exception e) {
      log.error("Error: {}", e);
      throw ErrorResponse.error(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  public static boolean canManageInvitations(AccessToken accessToken) {
    var emailVerified = accessToken.getEmailVerified();
    if (emailVerified == null) {
      log.errorf("emailVerified access token claim not found");

      return false;
    }

    if (!emailVerified) {
      log.errorf("emailVerified needs to be true");
      return false;
    }

    var email = accessToken.getEmail();
    if (email == null) {
      log.errorf("email access token claim not found");
      return false;
    }

    return true;
  }
}
