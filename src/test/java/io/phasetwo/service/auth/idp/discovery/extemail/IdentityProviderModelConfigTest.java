package io.phasetwo.service.auth.idp.discovery.extemail;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.keycloak.models.IdentityProviderModel;

class IdentityProviderModelConfigTest {

  @Test
  void matchesNormalizedDomainsWithoutAcceptingLookalikes() {
    IdentityProviderModel idp = new IdentityProviderModel();
    idp.setConfig(
        Map.of(
            "home.idp.discovery.domains", "github.com##xn--bcher-kva.de",
            "home.idp.discovery.matchSubdomains", "true"));
    IdentityProviderModelConfig config = new IdentityProviderModelConfig(idp);

    assertThat(config.supportsDomain(null, new Domain("GITHUB.COM")), is(true));
    assertThat(config.supportsDomain(null, new Domain("mail.GitHub.com")), is(true));
    assertThat(config.supportsDomain(null, new Domain("b\u00fccher.de")), is(true));
    assertThat(config.supportsDomain(null, new Domain("g\u0131thub.com")), is(false));
    assertThat(config.supportsDomain(null, new Domain("mail.g\u0131thub.com")), is(false));
  }

  @Test
  void respectsTheSubdomainConfiguration() {
    IdentityProviderModel idp = new IdentityProviderModel();
    idp.setConfig(Map.of("home.idp.discovery.domains", "example.com"));
    IdentityProviderModelConfig config = new IdentityProviderModelConfig(idp);

    assertThat(config.supportsDomain(null, new Domain("example.com")), is(true));
    assertThat(config.supportsDomain(null, new Domain("mail.example.com")), is(false));
  }
}
