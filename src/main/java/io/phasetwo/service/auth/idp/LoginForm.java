package io.phasetwo.service.auth.idp;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.model.IdentityProviderBean;
import org.keycloak.models.IdentityProviderModel;

import java.net.URI;
import java.util.List;

final class LoginForm {

    private final AuthenticationFlowContext context;
    private final BaseUriLoginFormsProvider loginFormsProvider;

    LoginForm(AuthenticationFlowContext context, BaseUriLoginFormsProvider loginFormsProvider) {
        this.context = context;
        this.loginFormsProvider = loginFormsProvider;
    }

    Response createWithSignInButtonOnly(MultivaluedMap<String, String> formData) {
        LoginFormsProvider form = createForm(formData);
        form.setAttribute(LoginFormsProvider.USERNAME_HIDDEN, "true");
        form.setAttribute(LoginFormsProvider.REGISTRATION_DISABLED, "true");
        return form.createLoginUsername();
    }

    Response create(MultivaluedMap<String, String> formData) {
        LoginFormsProvider forms = createForm(formData);
        return forms.createLoginUsername();
    }

    private LoginFormsProvider createForm(MultivaluedMap<String, String> formData) {
        LoginFormsProvider forms = context.form();
        if (!formData.isEmpty()) {
            forms.setFormData(formData);
        }
        return forms;
    }

    Response create(List<IdentityProviderModel> idps) {
        URI baseUriWithCodeAndClientId = loginFormsProvider.getBaseUriWithCodeAndClientId();
        LoginFormsProvider forms = context.form();
        forms.setAttribute("hidpd", new IdentityProviderBean(
            context.getSession(),
            context.getRealm(),
            baseUriWithCodeAndClientId,
            context
            ) {
                @Override
                public List<IdentityProvider> getProviders() {
                    return idps.stream()
                        .map(AlwaysSelectableIdentityProviderModel::new)
                        .map(idp -> createIdentityProvider(this.realm, this.baseURI, idp))
                        .toList();
                }
            }
        );
        return forms.createForm("hidpd-select-idp.ftl");
    }
}
