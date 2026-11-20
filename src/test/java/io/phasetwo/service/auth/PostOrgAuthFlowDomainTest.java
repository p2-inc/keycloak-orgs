package io.phasetwo.service.auth;

import static io.phasetwo.service.Orgs.ORG_OWNER_CONFIG_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.phasetwo.service.model.DomainModel;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.keycloak.models.KeycloakSession;

class PostOrgAuthFlowDomainTest {

  @Test
  void matchesBrokeredEmailDomainsIgnoringCase() {
    KeycloakSession session = session("example.com", true);
    assertThat(matches(session, "user@Example.COM", false), is(true));
    assertThat(matches(session, "user@MAIL.Example.COM", false), is(true));
    assertThat(matches(session, "user@notexample.com", false), is(false));
    assertThat(matches(session, "user@example.com.attacker.com", false), is(false));
  }

  @Test
  void requiresTheMatchingDomainToBeVerified() {
    KeycloakSession verified = session("Example.com", true);
    KeycloakSession unverified = session("example.com", false);
    assertThat(matches(verified, "user@EXAMPLE.COM", true), is(true));
    assertThat(matches(unverified, "user@Example.com", true), is(false));
    assertThat(matches(verified, "user@mail.example.com", true), is(false));
    assertThat(matches(verified, "user@notexample.com", true), is(false));
  }

  private static boolean matches(KeycloakSession session, String email, boolean requireVerified) {
    return PostOrgAuthFlow.emailDomainMatchesOrg(
        session, null, Map.of(ORG_OWNER_CONFIG_KEY, "org"), email, requireVerified);
  }

  private static KeycloakSession session(String domainName, boolean verified) {
    DomainModel domain =
        proxy(
            DomainModel.class,
            (proxy, method, args) -> {
              if (method.getName().equals("isVerified")) {
                return verified;
              }
              throw new AssertionError(method.getName());
            });
    OrganizationModel organization =
        proxy(
            OrganizationModel.class,
            (proxy, method, args) -> {
              if (method.getName().equals("getDomains")) {
                return Set.of(domainName);
              }
              if (method.getName().equals("getDomain")) {
                return domainName.equalsIgnoreCase((String) args[0]) ? domain : null;
              }
              throw new AssertionError(method.getName());
            });
    OrganizationProvider provider =
        proxy(
            OrganizationProvider.class,
            (proxy, method, args) -> {
              if (method.getName().equals("getOrganizationById")) {
                return "org".equals(args[1]) ? organization : null;
              }
              throw new AssertionError(method.getName());
            });
    return proxy(
        KeycloakSession.class,
        (proxy, method, args) -> {
          if (method.getName().equals("getProvider") && args[0] == OrganizationProvider.class) {
            return provider;
          }
          throw new AssertionError(method.getName());
        });
  }

  private static <T> T proxy(Class<T> type, InvocationHandler handler) {
    return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler));
  }
}
