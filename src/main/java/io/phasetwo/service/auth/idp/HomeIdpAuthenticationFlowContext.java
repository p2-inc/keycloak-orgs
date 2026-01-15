//package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp;

import io.phasetwo.service.auth.idp.discovery.spi.HomeIdpDiscoverer;
import org.keycloak.authentication.AuthenticationFlowContext;

final class HomeIdpAuthenticationFlowContext {

    private final AuthenticationFlowContext context;
    private HomeIdpForwarderConfig config;
    private LoginPage loginPage;
    private LoginHint loginHint;
    private HomeIdpDiscoverer discoverer;
    private RememberMe rememberMe;
    private AuthenticationChallenge authenticationChallenge;
    private Redirector redirector;
    private BaseUriLoginFormsProvider loginFormsProvider;
    private LoginForm loginForm;
    private Reauthentication reauthentication;

    HomeIdpAuthenticationFlowContext(AuthenticationFlowContext context) {
        this.context = context;
    }

    HomeIdpForwarderConfig config() {
        if (config == null) {
            config = new HomeIdpForwarderConfig(context.getAuthenticatorConfig());
        }
        return config;
    }

    LoginPage loginPage() {
        if (loginPage == null) {
            loginPage = new LoginPage(context, config(), reauthentication());
        }
        return  loginPage;
    }

    LoginHint loginHint() {
        if (loginHint == null) {
            loginHint = new LoginHint(context, new Users(context.getSession()));
        }
        return loginHint;
    }

    HomeIdpDiscoverer discoverer(AbstractHomeIdpDiscoveryAuthenticatorFactory.DiscovererConfig discovererConfig) {
        if (discoverer == null) {
            discoverer = context.getSession().getProvider(HomeIdpDiscoverer.class, discovererConfig.getProviderId());
        }
        return  discoverer;
    }

    RememberMe rememberMe() {
        if (rememberMe == null) {
            rememberMe = new RememberMe(context);
        }
        return rememberMe;
    }

    AuthenticationChallenge authenticationChallenge() {
        if (authenticationChallenge == null) {
            authenticationChallenge = new AuthenticationChallenge(context, rememberMe(), loginHint(), loginForm(), reauthentication());
        }
        return authenticationChallenge;
    }

    Redirector redirector() {
        if (redirector == null) {
            redirector = new Redirector(context);
        }
        return redirector;
    }

    LoginForm loginForm() {
        if (loginForm == null) {
            loginForm = new LoginForm(context, loginFormsProvider());
        }
        return loginForm;
    }

    BaseUriLoginFormsProvider loginFormsProvider() {
        if (loginFormsProvider == null) {
            loginFormsProvider = new BaseUriLoginFormsProvider(context);
        }
        return loginFormsProvider;
    }

    Reauthentication reauthentication() {
        if (reauthentication == null) {
            reauthentication = new Reauthentication(context);
        }
        return reauthentication;
    }
}
