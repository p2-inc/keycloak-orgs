//package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.model.IdentityProviderBean;
import org.keycloak.models.IdentityProviderModel;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

final class LoginForm {

    private final AuthenticationFlowContext context;
    private final BaseUriLoginFormsProvider loginFormsProvider;

    LoginForm(AuthenticationFlowContext context, BaseUriLoginFormsProvider loginFormsProvider) {
        this.context = context;
        this.loginFormsProvider = loginFormsProvider;
    }

    Response create(MultivaluedMap<String, String> formData) {
        LoginFormsProvider forms = context.form();
        if (!formData.isEmpty()) {
            forms.setFormData(formData);
        }
        return forms.createLoginUsername();
    }

    /*
    Response create(List<IdentityProviderModel> idps) {
        URI baseUriWithCodeAndClientId = loginFormsProvider.getBaseUriWithCodeAndClientId();
        LoginFormsProvider forms = context.form();
        forms.setAttribute("hidpd", new IdentityProviderBean(context.getSession(),
            context.getRealm(),
            baseUriWithCodeAndClientId,
            context));
        return forms.createForm("hidpd-select-idp.ftl");
    }
    */

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
