//package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp;

import org.keycloak.authentication.AuthenticationFlowContext;

final class HomeIdpAuthenticationFlowContext {

    private final AuthenticationFlowContext context;
    private HomeIdpDiscoveryConfig config;
    private LoginPage loginPage;
    private LoginHint loginHint;
    private HomeIdpDiscoverer discoverer;
    private RememberMe rememberMe;
    private AuthenticationChallenge authenticationChallenge;
    private Redirector redirector;
    private BaseUriLoginFormsProvider loginFormsProvider;
    private LoginForm loginForm;

    HomeIdpAuthenticationFlowContext(AuthenticationFlowContext context) {
        this.context = context;
    }

    HomeIdpDiscoveryConfig config() {
        if (config == null) {
            config = new HomeIdpDiscoveryConfig(context.getAuthenticatorConfig());
        }
        return config;
    }

    LoginPage loginPage() {
        if (loginPage == null) {
            loginPage = new LoginPage(context, config());
        }
        return  loginPage;
    }

    LoginHint loginHint() {
        if (loginHint == null) {
            loginHint = new LoginHint(context);
        }
        return loginHint;
    }

    HomeIdpDiscoverer discoverer() {
        if (discoverer == null) {
            discoverer = new HomeIdpDiscoverer(context);
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
            authenticationChallenge = new AuthenticationChallenge(context, rememberMe(), loginHint(), loginForm());
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
}
