package io.phasetwo.service.auth;

import static io.phasetwo.service.Orgs.FIRST_BROKER_LOGIN_FLOW_ALIAS;
import static io.phasetwo.service.Orgs.ORG_AUTH_FLOW_ALIAS;
import static io.phasetwo.service.Orgs.ORG_OWNER_CONFIG_KEY;

import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.util.Domains;
import io.phasetwo.service.util.IdentityProviders;
import jakarta.ws.rs.core.Response;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.PostBrokerLoginConstants;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.events.Errors;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

@JBossLog
public class PostOrgAuthFlow {

  static void setStatus(AuthenticationFlowContext context) {
    if (context.getExecution().getRequirement()
        == AuthenticationExecutionModel.Requirement.REQUIRED) {
      context.success();
    } else {
      context.attempted();
    }
  }

  static BrokeredIdentityContext getBrokeredIdentityContext(AuthenticationFlowContext context) {
    AuthenticationSessionModel clientSession = context.getAuthenticationSession();
    SerializedBrokeredIdentityContext serializedCtx =
        SerializedBrokeredIdentityContext.readFromAuthenticationSession(
            clientSession, AbstractIdpAuthenticator.BROKERED_CONTEXT_NOTE);
    if (serializedCtx == null) {
      // get the identity context for post login flow
      // https://groups.google.com/g/keycloak-user/c/eiX5dCjN3qc
      serializedCtx =
          SerializedBrokeredIdentityContext.readFromAuthenticationSession(
              clientSession, PostBrokerLoginConstants.PBL_BROKERED_IDENTITY_CONTEXT);
    }
    if (serializedCtx == null) {
      throw new AuthenticationFlowException(
          "Not found serialized context in clientSession",
          AuthenticationFlowError.IDENTITY_PROVIDER_ERROR);
    }
    return serializedCtx.deserialize(context.getSession(), clientSession);
  }

  static boolean brokeredIdpEnabled(
      AuthenticationFlowContext context, BrokeredIdentityContext brokerContext) {
    if (!brokerContext.getIdpConfig().isEnabled()) {
      context.getEvent().user(context.getUser()).error(Errors.IDENTITY_PROVIDER_ERROR);
      Response challengeResponse =
          context
              .form()
              .setError(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR)
              .createErrorPage(Response.Status.BAD_REQUEST);
      context.failureChallenge(AuthenticationFlowError.IDENTITY_PROVIDER_ERROR, challengeResponse);
      return false;
    } else {
      return true;
    }
  }

  static void realmPostCreate(
      RealmModel.RealmPostCreateEvent event,
      String providerId,
      AuthenticationExecutionModel.Requirement requirement) {
    realmPostCreate(event, providerId, requirement, null);
  }

  static void realmPostCreate(
      RealmModel.RealmPostCreateEvent event,
      String providerId,
      AuthenticationExecutionModel.Requirement requirement,
      Integer priority) {
    KeycloakSession session = event.getKeycloakSession();
    RealmModel realm = event.getCreatedRealm();
    AuthenticationFlowModel flow = realm.getFlowByAlias(ORG_AUTH_FLOW_ALIAS);
    if (flow == null) {
      log.infof("creating built-in auth flow for %s", ORG_AUTH_FLOW_ALIAS);
      flow = new AuthenticationFlowModel();
      flow.setAlias(ORG_AUTH_FLOW_ALIAS);
      flow.setBuiltIn(true);
      flow.setProviderId("basic-flow");
      flow.setDescription("Post broker login flow used for organization IdPs.");
      flow.setTopLevel(true);
      flow = realm.addAuthenticationFlow(flow);
    }

    boolean hasExecution =
        realm
                .getAuthenticationExecutionsStream(flow.getId())
                .filter(e -> providerId.equals(e.getAuthenticator()))
                .count()
            > 0;

    if (!hasExecution) {
      log.infof("adding execution %s for auth flow for %s", providerId, ORG_AUTH_FLOW_ALIAS);
      ProviderFactory f =
          session.getKeycloakSessionFactory().getProviderFactory(Authenticator.class, providerId);
      AuthenticationExecutionModel execution = new AuthenticationExecutionModel();
      execution.setParentFlow(flow.getId());
      execution.setRequirement(requirement);
      execution.setAuthenticatorFlow(false);
      execution.setAuthenticator(providerId);
      if (priority != null) execution.setPriority(priority.intValue());
      execution = realm.addAuthenticatorExecution(execution);
    }
  }

  /**
   * Returns {@code true} if the (brokered) email's domain is owned by at least one of the
   * organizations that own the IdP identified by {@code idpConfig}. When {@code requireVerifiedDomain}
   * is set, the owning organization's matching domain must additionally be verified.
   *
   * <p>This is the single source of truth for the org-domain check shared by the post-broker {@link
   * OrgIdpDomainVerifierAuthenticatorFactory} and the first-broker {@link
   * OrgIdpFirstBrokerDomainVerifierAuthenticatorFactory}, so both enforce identical rules.
   */
  static boolean emailDomainMatchesOrg(
      KeycloakSession session,
      RealmModel realm,
      java.util.Map<String, String> idpConfig,
      String email,
      boolean requireVerifiedDomain) {
    Optional<String> identityEmailDomain = Domains.extract(email);
    if (identityEmailDomain.isEmpty()) {
      return false;
    }
    OrganizationProvider orgs = session.getProvider(OrganizationProvider.class);
    Set<String> orgIds = IdentityProviders.getAttributeMultivalued(idpConfig, ORG_OWNER_CONFIG_KEY);
    return orgIds.stream()
        .map(orgId -> orgs.getOrganizationById(realm, orgId))
        .filter(Objects::nonNull)
        .filter(org -> Domains.supportsDomain(org.getDomains(), identityEmailDomain.get()))
        .anyMatch(org -> isVerifiedDomain(org, requireVerifiedDomain, identityEmailDomain.get()));
  }

  private static boolean isVerifiedDomain(
      OrganizationModel org, boolean requireVerifiedDomain, String identityEmailDomain) {
    if (requireVerifiedDomain) {
      var domain = org.getDomain(identityEmailDomain);
      return domain != null && domain.isVerified();
    }
    return true;
  }

  /**
   * Adds {@code providerId} to the realm's built-in {@code first broker login} flow, positioned
   * immediately after {@code idp-review-profile} so it runs after any email edit on the Review
   * Profile page but before {@code idp-create-user-if-unique}. Used to gate org-IdP account creation
   * on the brokered email domain. No-op if the flow is missing or the execution already exists.
   */
  static void firstBrokerPostCreate(
      RealmModel.RealmPostCreateEvent event,
      String providerId,
      AuthenticationExecutionModel.Requirement requirement) {
    addFirstBrokerExecution(
        event.getKeycloakSession(), event.getCreatedRealm(), providerId, requirement);
  }

  /**
   * Same as {@link #firstBrokerPostCreate} but for an arbitrary realm, so existing realms can be
   * retrofitted on {@code PostMigrationEvent}.
   */
  static void addFirstBrokerExecution(
      KeycloakSession session,
      RealmModel realm,
      String providerId,
      AuthenticationExecutionModel.Requirement requirement) {
    AuthenticationFlowModel flow = realm.getFlowByAlias(FIRST_BROKER_LOGIN_FLOW_ALIAS);
    if (flow == null) {
      log.warnf(
          "flow %s not found; cannot add %s execution", FIRST_BROKER_LOGIN_FLOW_ALIAS, providerId);
      return;
    }

    boolean hasExecution =
        realm
            .getAuthenticationExecutionsStream(flow.getId())
            .anyMatch(e -> providerId.equals(e.getAuthenticator()));
    if (hasExecution) {
      return;
    }

    // Place it just after the review-profile step (if present) so edits to the email on the
    // Review Profile page are also validated; otherwise fall back to the top of the flow.
    Integer reviewProfilePriority =
        realm
            .getAuthenticationExecutionsStream(flow.getId())
            .filter(e -> "idp-review-profile".equals(e.getAuthenticator()))
            .map(AuthenticationExecutionModel::getPriority)
            .findFirst()
            .orElse(null);

    log.infof("adding execution %s to flow %s", providerId, FIRST_BROKER_LOGIN_FLOW_ALIAS);
    ProviderFactory f =
        session.getKeycloakSessionFactory().getProviderFactory(Authenticator.class, providerId);
    AuthenticationExecutionModel execution = new AuthenticationExecutionModel();
    execution.setParentFlow(flow.getId());
    execution.setRequirement(requirement);
    execution.setAuthenticatorFlow(false);
    execution.setAuthenticator(providerId);
    if (reviewProfilePriority != null) {
      execution.setPriority(reviewProfilePriority + 1);
    }
    execution = realm.addAuthenticatorExecution(execution);
  }
}
