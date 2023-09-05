//package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp;

import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.IdentityProviderFactory;
import org.keycloak.broker.provider.util.IdentityBrokerState;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakUriInfo;
import org.keycloak.models.RealmModel;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.sessions.AuthenticationSessionModel;

import static org.keycloak.services.resources.IdentityBrokerService.getIdentityProviderFactory;

final class Redirector {

    private static final Logger LOG = Logger.getLogger(Redirector.class);

    private final AuthenticationFlowContext context;

    Redirector(AuthenticationFlowContext context) {
        this.context = context;
    }

    void redirectTo(IdentityProviderModel idp) {
        String providerAlias = idp.getAlias();
        RealmModel realm = context.getRealm();
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();
        KeycloakSession keycloakSession = context.getSession();
        ClientSessionCode<AuthenticationSessionModel> clientSessionCode =
            new ClientSessionCode<>(keycloakSession, realm, authenticationSession);
        clientSessionCode.setAction(AuthenticationSessionModel.Action.AUTHENTICATE.name());
        if (!idp.isEnabled()) {
            LOG.warnf("Identity Provider %s is disabled.", providerAlias);
            return;
        }
        if (idp.isLinkOnly()) {
            LOG.warnf("Identity Provider %s is not allowed to perform a login.", providerAlias);
            return;
        }
        new HomeIdpAuthenticationFlowContext(context).loginHint().copyTo(clientSessionCode);
        IdentityProviderFactory providerFactory = getIdentityProviderFactory(keycloakSession, idp);
        IdentityProvider identityProvider = providerFactory.create(keycloakSession, idp);

        Response response = identityProvider.performLogin(createAuthenticationRequest(providerAlias, clientSessionCode));
        context.forceChallenge(response);
    }

    private AuthenticationRequest createAuthenticationRequest(String providerId, ClientSessionCode<AuthenticationSessionModel> clientSessionCode) {
        AuthenticationSessionModel authSession = null;
        IdentityBrokerState encodedState = null;

        if (clientSessionCode != null) {
            authSession = clientSessionCode.getClientSession();
            String relayState = clientSessionCode.getOrGenerateCode();
            encodedState = IdentityBrokerState.decoded(relayState, authSession.getClient().getId(), authSession.getClient().getClientId(), authSession.getTabId());
        }

        KeycloakSession keycloakSession = context.getSession();
        KeycloakUriInfo keycloakUriInfo = keycloakSession.getContext().getUri();
        RealmModel realm = context.getRealm();
        String redirectUri = Urls.identityProviderAuthnResponse(keycloakUriInfo.getBaseUri(), providerId, realm.getName()).toString();
        return new AuthenticationRequest(keycloakSession, realm, authSession, context.getHttpRequest(), keycloakUriInfo, encodedState, redirectUri);
    }

}
