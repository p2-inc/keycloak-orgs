package io.phasetwo.service.auth.idp;

import java.net.URI;
import java.util.List;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.ClientSessionCode;

/** */
@JBossLog
public class IdpSelectorAuthenticator implements Authenticator {

  protected static final String ACCEPTS_PROMPT_NONE = "acceptsPromptNoneForwardFromClient";

  private final KeycloakSession session;

  public IdpSelectorAuthenticator(KeycloakSession session) {
    this.session = session;
  }

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    Response challenge = context.form().createForm("login-select-idp.ftl");
    context.challenge(challenge);
    return;
  }

  private void redirect(AuthenticationFlowContext context, String providerId) {
    IdentityProviderModel identityProvider = context.getRealm().getIdentityProviderByAlias(providerId);
    if (identityProvider != null && identityProvider.isEnabled()) {
      new Redirector(context).redirectTo(identityProvider);
        /*
        String accessCode =
            new ClientSessionCode<>(
                    context.getSession(), context.getRealm(), context.getAuthenticationSession())
                .getOrGenerateCode();
        String clientId = context.getAuthenticationSession().getClient().getClientId();
        String tabId = context.getAuthenticationSession().getTabId();
        URI location =
            Urls.identityProviderAuthnRequest(
                context.getUriInfo().getBaseUri(),
                providerId,
                context.getRealm().getName(),
                accessCode,
                clientId,
                tabId);
        if (context.getAuthenticationSession().getClientNote(OAuth2Constants.DISPLAY) != null) {
          location =
              UriBuilder.fromUri(location)
                  .queryParam(
                      OAuth2Constants.DISPLAY,
                      context.getAuthenticationSession().getClientNote(OAuth2Constants.DISPLAY))
                  .build();
        }
        Response response = Response.seeOther(location).build();
        // will forward the request to the IDP with prompt=none if the IDP accepts forwards with
        // prompt=none.
        if ("none"
                .equals(
                    context
                        .getAuthenticationSession()
                        .getClientNote(OIDCLoginProtocol.PROMPT_PARAM))
            && Boolean.valueOf(identityProvider.getConfig().get(ACCEPTS_PROMPT_NONE))) {
          context
              .getAuthenticationSession()
              .setAuthNote(AuthenticationProcessor.FORWARDED_PASSIVE_LOGIN, "true");
        }

        log.debugf("Redirecting to %s", providerId);
        context.forceChallenge(response);
        */
      return;
    }

    log.warnf("Provider not found or not enabled for realm %s", providerId);
    if (context.getExecution().getRequirement() == AuthenticationExecutionModel.Requirement.REQUIRED) {
      context.success();
    } else {
      context.attempted();
    }
  }

  @Override
  public void action(AuthenticationFlowContext context) {
    MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
    String providerId = formData.getFirst("providerId");
    log.infof("Redirecting to %s", providerId);
    redirect(context, providerId);
  }

  @Override
  public boolean requiresUser() {
    return false;
  }

  @Override
  public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
    return true;
  }

  @Override
  public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {}

  @Override
  public void close() {}
}
