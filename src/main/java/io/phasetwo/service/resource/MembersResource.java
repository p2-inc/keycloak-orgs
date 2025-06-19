package io.phasetwo.service.resource;

import static io.phasetwo.service.Orgs.ACTIVE_ORGANIZATION;
import static io.phasetwo.service.resource.OrganizationResourceType.*;
import static org.keycloak.events.EventType.UPDATE_PROFILE;
import static org.keycloak.models.utils.ModelToRepresentation.*;

import com.google.common.base.Strings;
import io.phasetwo.service.model.OrganizationMemberModel;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.representation.OrganizationMemberAttribute;
import io.phasetwo.service.representation.UserOrganizationMember;
import io.phasetwo.service.representation.UserWithOrgs;
import io.phasetwo.service.util.ActiveOrganization;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.Constants;
import org.keycloak.models.UserModel;

@JBossLog
public class MembersResource extends OrganizationAdminResource {

  private final OrganizationModel organization;

  public MembersResource(OrganizationAdminResource parent, OrganizationModel organization) {
    super(parent);
    this.organization = organization;
  }

  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  public Stream<UserWithOrgs> getMembers(
      @QueryParam("search") String searchQuery,
      @QueryParam("first") Integer firstResult,
      @QueryParam("max") Integer maxResults,
      @QueryParam("includeOrgs") Boolean includeOrgs,
      @QueryParam("excludeAdminAccounts") Boolean excludeAdminAccounts) {
    log.debugf("Get members for %s %s [%s]", realm.getName(), organization.getId(), searchQuery);
    boolean excludeAdmin = excludeAdminAccounts != null && excludeAdminAccounts;
    firstResult = firstResult != null ? firstResult : 0;
    maxResults = maxResults != null ? maxResults : Constants.DEFAULT_MAX_RESULTS;
    boolean addOrgs = includeOrgs != null && includeOrgs;

    return organization
        .searchForMembersStream(searchQuery, firstResult, maxResults, excludeAdmin)
        .map(
            userModel -> {
              UserWithOrgs u = new UserWithOrgs(toBriefRepresentation(userModel));
              if (addOrgs) {
                u.addOrganization(
                    organization.getId(),
                    organization
                        .getRolesByUserStream(userModel)
                        .map(Converters::convertOrganizationRole)
                        .toList());
              }
              return u;
            });
  }

  @GET
  @Path("org-members")
  @Produces(MediaType.APPLICATION_JSON)
  public Stream<UserOrganizationMember> getOrganizationMembers(
          @QueryParam("search") String searchQuery,
          @QueryParam("first") Integer firstResult,
          @QueryParam("max") Integer maxResults) {
    log.debugf("Get organization members for %s %s [%s]", realm.getName(), organization.getId(), searchQuery);
    firstResult = firstResult != null ? firstResult : 0;
    maxResults = maxResults != null ? maxResults : Constants.DEFAULT_MAX_RESULTS;

    return organization
            .searchForOrganizationMembersStream(searchQuery, firstResult, maxResults)
            .map(m -> UserOrganizationMemberConverter.toRepresentation(session, realm, m));
  }

  @PUT
  @Path("org-members/{userId}/attributes")
  @Produces(MediaType.APPLICATION_JSON)
  public Response addOrganizationMemberAttributes(@PathParam("userId") String userId,
                                                 @Valid OrganizationMemberAttribute body ) {
    canManage();
    log.debugf("Add organization member attribute to user %s from %s %s", userId, realm.getName(), organization.getId());
    UserModel member = session.users().getUserById(realm, userId);
    if (member != null) {
      if (!organization.hasMembership(member)) {
        throw new BadRequestException(
                String.format(
                        "User %s must be a member of %s to be granted role.",
                        userId, organization.getName()));
    }

      OrganizationMemberModel orgMembership = organization.getMembershipDetails(member);
      if (body.getAttributes() != null) {
        orgMembership.removeAttributes();
        for (Map.Entry<String, List<String>> entry : body.getAttributes().entrySet()) {
          orgMembership.setAttribute(entry.getKey(), entry.getValue());
        }
      }

      return Response.ok()
              .entity(UserOrganizationMemberConverter.toRepresentation(session, realm, orgMembership))
              .build();
    } else {
      throw new NotFoundException(String.format("User %s doesn't exist", userId));
    }
  }

  @GET
  @Path("org-members/{userId}/attributes")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getOrganizationMemberAttributes(@PathParam("userId") String userId) {
    canManage();
    log.debugf("Get organization member attribute to user %s from %s %s", userId, realm.getName(), organization.getId());
    UserModel member = session.users().getUserById(realm, userId);
    if (member != null) {
      if (!organization.hasMembership(member)) {
        throw new BadRequestException(
                String.format(
                        "User %s must be a member of %s to be granted role.",
                        userId, organization.getName()));
      }

      OrganizationMemberModel orgMembership = organization.getMembershipDetails(member);

      return Response.ok()
              .entity(UserOrganizationMemberConverter.toRepresentation(session, realm, orgMembership))
              .build();
    } else {
      throw new NotFoundException(String.format("User %s doesn't exist", userId));
    }
  }

  @GET
  @Path("count")
  @Produces(MediaType.APPLICATION_JSON)
  public Long getMembersCount(@QueryParam("excludeAdminAccounts") Boolean excludeAdminAccounts) {
    log.debugf("Get members count for %s %s", realm.getName(), organization.getId());
    boolean excludeAdmin = excludeAdminAccounts != null && excludeAdminAccounts;
    return organization.getMembersCount(excludeAdmin);
  }

  @DELETE
  @Path("{userId}")
  public Response removeMember(@PathParam("userId") String userId) {
    canDelete(userId);

    log.debugf("Remove member %s from %s %s", userId, realm.getName(), organization.getId());
    UserModel member = session.users().getUserById(realm, userId);
    if (!Strings.isNullOrEmpty(member.getUsername())
        && member
            .getUsername()
            .equals(OrganizationResourceProviderFactory.getDefaultAdminUsername(organization))) {
      throw new ForbiddenException("Cannot remove default organization user.");
    }

    if (!organization.hasMembership(member)) throw new NotFoundException();

    ActiveOrganization activeOrganizationUtil =
        ActiveOrganization.fromContext(session, realm, member);
    if (activeOrganizationUtil.isValid()
        && activeOrganizationUtil.isCurrentActiveOrganization(organization.getId())) {
      member.setAttribute(ACTIVE_ORGANIZATION, new ArrayList<>());

      EventBuilder event = new EventBuilder(realm, session, connection);
      event
          .event(UPDATE_PROFILE)
          .user(user)
          .detail(
              "removed_active_organization_id", activeOrganizationUtil.getOrganization().getId())
          .success();
    }

    organization.revokeMembership(member);
    adminEvent
        .resource(ORGANIZATION_MEMBERSHIP.name())
        .operation(OperationType.DELETE)
        .resourcePath(session.getContext().getUri())
        .representation(userId)
        .success();
    return Response.noContent().build();
  }

  @GET
  @Path("{userId}")
  public Response getMember(@PathParam("userId") String userId) {
    log.debugf("Check membership %s for %s %s", userId, realm.getName(), organization.getId());
    UserModel member = session.users().getUserById(realm, userId);
    if (member != null && organization.hasMembership(member)) {
      return Response.noContent().build();
    } else {
      throw new NotFoundException();
    }
  }

  @PUT
  @Path("{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response addMember(@PathParam("userId") String userId) {
    log.debugf("Add %s as member for %s %s", userId, realm.getName(), organization.getId());
    canManage();

    UserModel member = session.users().getUserById(realm, userId);
    if (member != null) {
      if (!organization.hasMembership(member)) {
        organization.grantMembership(member);
        adminEvent
            .resource(ORGANIZATION_MEMBERSHIP.name())
            .operation(OperationType.CREATE)
            .resourcePath(session.getContext().getUri())
            .representation(userId)
            .success();
      }
      return Response.created(session.getContext().getUri().getAbsolutePathBuilder().build())
          .build();
    } else {
      throw new NotFoundException();
    }
  }

  private void canManage() {
    if (!auth.hasManageOrgs() && !auth.hasOrgManageMembers(organization)) {
      throw new NotAuthorizedException(
          String.format(
              "User %s doesn't have permission to manage members in org %s",
              auth.getUser().getId(), organization.getName()));
    }
  }

  private void canDelete(String userId) {
    if (!userId.equals(user.getId())) {
      canManage();
    }
  }
}
