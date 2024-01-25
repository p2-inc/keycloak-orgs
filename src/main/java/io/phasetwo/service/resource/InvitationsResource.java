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
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProvider;
import org.keycloak.email.freemarker.beans.ProfileBean;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;

@JBossLog
public class InvitationsResource extends OrganizationAdminResource {

  private final OrganizationModel organization;

  public InvitationsResource(OrganizationAdminResource parent, OrganizationModel organization) {
    super(parent);
    this.organization = organization;
  }

  @POST
  @Path("")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createInvitation(@Valid InvitationRequest invitation) {
    String email = invitation.getEmail();
    log.debugf("Create invitation for %s %s %s", email, realm.getName(), organization.getId());
    canManage();

    if (email == null || !isValidEmail(email)) {
      throw new BadRequestException("Invalid email: " + email);
    }
    if (!canSetRoles(invitation.getRoles())) {
      throw new BadRequestException("Unknown role in list.");
    }
    email = email.toLowerCase();

    String link = Optional.ofNullable(invitation.getRedirectUri()).orElse("");

    if (organization.getInvitationsByEmail(email).count() > 0) {
      log.infof(
          "invitation for %s %s %s already exists. .",
          email, realm.getName(), organization.getId());
      throw new ClientErrorException(
          String.format("Invitation for %s already exists.", email), Response.Status.CONFLICT);
    }

    UserModel user = KeycloakModelUtils.findUserByNameOrEmail(session, realm, email);
    if (user != null && organization.hasMembership(user)) {
      log.infof("%s is already a member of %s", email, organization.getId());
      throw new ClientErrorException(
          String.format("%s is already a member of this organization.", email),
          Response.Status.CONFLICT);
    }

    try {
      UserModel inviter = null;
      if (invitation.getInviterId() == null || invitation.getInviterId().equals("")) {
        inviter = auth.getUser();
      } else {
        inviter = session.users().getUserById(realm, invitation.getInviterId());
      }

      InvitationModel i = organization.addInvitation(email, inviter);
      i.setUrl(link);
      if (invitation.getRoles() != null) i.setRoles(invitation.getRoles());
      if (invitation.getAttributes() != null && invitation.getAttributes().size() > 0) {
        invitation
            .getAttributes()
            .entrySet()
            .forEach(
                e -> {
                  i.setAttribute(e.getKey(), e.getValue());
                });
      }
      Invitation o = convertInvitationModelToInvitation(i);
      log.debugf("Made invitation %s", o);

      adminEvent
          .resource(INVITATION.name())
          .operation(OperationType.CREATE)
          .resourcePath(session.getContext().getUri(), o.getId())
          .representation(o)
          .success();

      URI location = session.getContext().getUri().getAbsolutePathBuilder().path(o.getId()).build();

      if (invitation.isSend()) {
        try {
          sendInvitationEmail(email, session, realm, inviter, link, o.getAttributes());
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
    if (roles == null || roles.isEmpty()) return true;
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
      String email,
      KeycloakSession session,
      RealmModel realm,
      UserModel inviter,
      String link,
      Map<String, List<String>> attributes)
      throws Exception {
    EmailTemplateProvider emailTemplateProvider = session.getProvider(EmailTemplateProvider.class);

    Method sendMethod =
        FreeMarkerEmailTemplateProvider.class.getDeclaredMethod(
            "send", String.class, List.class, String.class, Map.class, String.class);
    sendMethod.setAccessible(true);

    String realmName =
        Strings.isNullOrEmpty(realm.getDisplayName()) ? realm.getName() : realm.getDisplayName();
    String orgName =
        Strings.isNullOrEmpty(organization.getDisplayName())
            ? organization.getName()
            : organization.getDisplayName();
    String inviterName = getInviterName(inviter).orElse("");

    String templateName = "invitation-email.ftl";
    String subjectKey = "invitationEmailSubject";
    List<Object> subjectAttributes = ImmutableList.of(realmName, orgName, inviterName);
    Map<String, Object> bodyAttributes = Maps.newHashMap();
    bodyAttributes.put("email", email);
    bodyAttributes.put("realmName", realmName);
    bodyAttributes.put("orgName", orgName);
    bodyAttributes.put("inviterName", inviterName);
    bodyAttributes.put("inviter", new ProfileBean(inviter));
    bodyAttributes.put("link", link);
    bodyAttributes.put("attributes", attributes);

    emailTemplateProvider.setRealm(realm).setUser(user).setAttribute("realmName", realmName);

    sendMethod.invoke(
        emailTemplateProvider, subjectKey, subjectAttributes, templateName, bodyAttributes, email);
  }

  Optional<String> getInviterName(UserModel user) {
    if (user == null) return Optional.empty();
    StringBuilder o = new StringBuilder();
    if (!Strings.isNullOrEmpty(user.getFirstName())) {
      o.append(user.getFirstName());
    }
    if (!Strings.isNullOrEmpty(user.getLastName())) {
      if (o.length() > 0) {
        o.append(" ");
      }
      o.append(user.getLastName());
    }
    if (!Strings.isNullOrEmpty(user.getEmail())) {
      if (o.length() > 0) {
        o.append(" ").append("(");
      }
      o.append(user.getEmail());
      if (o.length() > user.getEmail().length()) {
        o.append(")");
      }
    }
    return Optional.ofNullable(Strings.emptyToNull(o.toString()));
  }

  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  public Stream<Invitation> listInvitations(
      @QueryParam("search") String searchQuery,
      @QueryParam("first") Integer firstResult,
      @QueryParam("max") Integer maxResults) {
    log.debugf("Get invitations for %s %s", realm.getName(), organization.getId());
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

  @GET
  @Path("{invitationId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Invitation getInvitation(@PathParam("invitationId") String invitationId) {
    log.debugf("Get invitation for %s %s %s", realm.getName(), organization.getId(), invitationId);
    InvitationModel invitation = organization.getInvitation(invitationId);
    if (invitation == null) {
      throw new NotFoundException(String.format("No invitation with id %s", invitationId));
    }
    return convertInvitationModelToInvitation(invitation);
  }

  @PUT
  @Path("{invitationId}/resend-email")
  public Response resendEmail(@PathParam("invitationId") String invitationId) {
    log.debugf(
        "Resend invitation for %s %s %s", realm.getName(), organization.getId(), invitationId);
    InvitationModel invitation = organization.getInvitation(invitationId);
    if (invitation == null) {
      throw new NotFoundException(String.format("No invitation with id %s", invitationId));
    }

    UserModel inviter = invitation.getInviter();
    if (inviter == null) {
      inviter = auth.getUser();
    }

    try {
      sendInvitationEmail(
          invitation.getEmail(),
          session,
          realm,
          inviter,
          Optional.ofNullable(invitation.getUrl()).orElse(""),
          invitation.getAttributes());
    } catch (Exception e) {
      log.warn("Unable to send invitation email", e);
    }
    return Response.noContent().build();
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
