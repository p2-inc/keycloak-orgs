package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.Converters.*;
import static io.phasetwo.service.resource.OrganizationResourceType.*;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.phasetwo.service.model.InvitationModel;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.representation.Invitation;
import io.phasetwo.service.representation.InvitationRequest;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProvider;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.resources.admin.AdminRoot;

@JBossLog
public class InvitationsResource extends OrganizationAdminResource {

  private final OrganizationModel organization;

  public InvitationsResource(RealmModel realm, OrganizationModel organization) {
    super(realm);
    this.organization = organization;
  }

  @POST
  @Path("")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createInvitation(@Valid InvitationRequest invitation) {
    String email = invitation.getEmail();
    try {
      log.infof("Create invitation for %s %s %s", email, realm.getName(), organization.getId());
      canManage();

      if (email == null || !isValidEmail(email)) {
        throw new BadRequestException("Invalid email: " + email);
      }
      if (!canSetRoles(invitation.getRoles())) {
        throw new BadRequestException("Unknown role in list.");
      }
      InvitationModel i = organization.addInvitation(email, auth.getUser());
      i.setRoles(invitation.getRoles());
      Invitation o = convertInvitationModelToInvitation(i);

      adminEvent
          .resource(INVITATION.name())
          .operation(OperationType.CREATE)
          .resourcePath(session.getContext().getUri(), o.getId())
          .representation(o)
          .success();

      // /auth/realms/:realm/orgs/:orgId/invtations/:name"
      URI location =
          AdminRoot.realmsUrl(session.getContext().getUri())
              .path(realm.getName())
              .path("orgs")
              .path(organization.getId())
              .path("invitations")
              .path(o.getId())
              .build();

      if (invitation.isSend()) {
        try {
          sendInvitationEmail(email, session, realm, user);
        } catch (Exception e) {
          log.warn("Unable to send invitation email", e);
        }
      }

      return Response.created(location).build();
    } catch (Exception e) {
      throw new InternalServerErrorException(e);
    }
  }

  boolean canSetRoles(Collection<String> roles) {
    Set<String> orgRoles =
        organization.getRolesStream().map(r -> r.getName()).collect(Collectors.toSet());
    for (String role : roles) {
      if (!orgRoles.contains(role)) {
        return false;
      }
    }
    return true;
  }

  void sendInvitationEmail(
      String email, KeycloakSession session, RealmModel realm, UserModel inviter) throws Exception {
    EmailTemplateProvider emailTemplateProvider = session.getProvider(EmailTemplateProvider.class);

    // protected void send(String subjectFormatKey, List<Object> subjectAttributes, String
    // bodyTemplate, Map<String, Object> bodyAttributes, String address) throws EmailException {
    Method sendMethod =
        FreeMarkerEmailTemplateProvider.class.getDeclaredMethod(
            "send", String.class, List.class, String.class, Map.class, String.class);
    sendMethod.setAccessible(true);
    ;

    String realmName =
        Strings.isNullOrEmpty(realm.getDisplayName()) ? realm.getName() : realm.getDisplayName();
    String orgName =
        Strings.isNullOrEmpty(organization.getDisplayName())
            ? organization.getName()
            : organization.getDisplayName();
    String inviterName = inviter.getEmail(); // todo better display name for inviter

    String templateName = "invitation-email.ftl";
    String subjectKey = "invitationEmailSubject";
    List<Object> subjectAttributes = ImmutableList.of(realmName, orgName, inviterName);
    Map<String, Object> bodyAttributes = Maps.newHashMap();
    bodyAttributes.put("email", email);
    bodyAttributes.put("realmName", realmName);
    bodyAttributes.put("orgName", orgName);
    bodyAttributes.put("inviterName", inviterName);
    // bodyAttributes.put("link", link);

    sendMethod.invoke(
        emailTemplateProvider, subjectKey, subjectAttributes, templateName, bodyAttributes, email);
  }

  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  public Stream<Invitation> listInvitations(
      @QueryParam("search") String searchQuery,
      @QueryParam("first") Integer firstResult,
      @QueryParam("max") Integer maxResults) {
    log.infof("Get invitations for %s %s", realm.getName(), organization.getId());
    Optional<String> search = Optional.ofNullable(searchQuery);
    firstResult = firstResult != null ? firstResult : 0;
    maxResults = maxResults != null ? maxResults : Constants.DEFAULT_MAX_RESULTS;

    return organization
        .getInvitationsStream()
        .filter(i -> !search.isPresent() || i.getEmail().contains(search.get()))
        .skip(firstResult)
        .limit(maxResults)
        .map(i -> convertInvitationModelToInvitation(i));
  }

  @DELETE
  @Path("{invitationId}")
  public Response removeInvitation(@PathParam("invitationId") String invitationId) {
    canManage();

    // inefficient
    InvitationModel invitation =
        organization
            .getInvitationsStream()
            .filter(i -> i.getId().equals(invitationId))
            .findAny()
            .orElse(null);
    if (invitation == null)
      throw new NotFoundException(String.format("No invitation with id %s", invitationId));

    organization.revokeInvitation(invitationId);

    adminEvent
        .resource(INVITATION.name())
        .operation(OperationType.DELETE)
        .resourcePath(session.getContext().getUri(), invitation.getId())
        .representation(convertInvitationModelToInvitation(invitation))
        .success();

    return Response.status(204).build();
  }

  private void canManage() {
    if (!auth.hasManageOrgs() && !auth.hasOrgManageInvitations(organization)) {
      throw new NotAuthorizedException(
          String.format(
              "User %s doesn't have permission to manage invitations in org %s",
              auth.getUser().getId(), organization.getName()));
    }
  }

  /** Get a validated email address */
  private static InternetAddress getValidEmail(String email) throws AddressException {
    Objects.requireNonNull(email, "email must not be null to validate");
    try {
      if (email.startsWith("mailto:")) email = email.substring(7);
      InternetAddress emailAddr = new InternetAddress(email);
      emailAddr.validate();
      return emailAddr;
    } catch (AddressException e) {
      throw e;
    }
  }

  private static boolean isValidEmail(String email) {
    try {
      getValidEmail(email);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
