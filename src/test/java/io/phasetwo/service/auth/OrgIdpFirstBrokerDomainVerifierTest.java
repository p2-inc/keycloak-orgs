package io.phasetwo.service.auth;

import static io.phasetwo.service.Orgs.FIRST_BROKER_LOGIN_FLOW_ALIAS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

import io.phasetwo.service.AbstractOrganizationTest;
import java.util.List;
import java.util.UUID;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;

/**
 * Regression guard for GHSA-gvcc-4vw2-6wvr. The first-broker domain verifier must be auto-wired into
 * a new realm's built-in {@code first broker login} flow, as REQUIRED, positioned after {@code
 * idp-review-profile} and before {@code idp-create-user-if-unique} (i.e. before the "User creation
 * or linking" subflow). Without it, an org IdP can create accounts for emails outside the org's
 * domains, which later enables account takeover.
 */
@JBossLog
public class OrgIdpFirstBrokerDomainVerifierTest extends AbstractOrganizationTest {

  @Test
  void firstBrokerDomainVerifierIsWiredIntoNewRealmFlow() {
    String realmName = "fbl-verifier-" + UUID.randomUUID();
    RealmRepresentation realm = new RealmRepresentation();
    realm.setRealm(realmName);
    realm.setEnabled(true);
    keycloak.realms().create(realm);
    try {
      List<AuthenticationExecutionInfoRepresentation> executions =
          keycloak.realm(realmName).flows().getExecutions(FIRST_BROKER_LOGIN_FLOW_ALIAS);

      AuthenticationExecutionInfoRepresentation verifier =
          findTopLevel(executions, OrgIdpFirstBrokerDomainVerifierAuthenticatorFactory.PROVIDER_ID);
      AuthenticationExecutionInfoRepresentation reviewProfile =
          findTopLevel(executions, "idp-review-profile");
      AuthenticationExecutionInfoRepresentation userCreation =
          findTopLevelByDisplay(executions, "User creation or linking");

      assertThat(
          "first-broker domain verifier must be present in the first broker login flow",
          verifier,
          notNullValue());
      assertThat(
          "it must be enforced (REQUIRED), not optional",
          verifier.getRequirement(),
          is("REQUIRED"));
      // Must run after the Review Profile step (so an edited email is also validated) ...
      assertThat(reviewProfile, notNullValue());
      assertThat(verifier.getPriority(), greaterThan(reviewProfile.getPriority()));
      // ... and before user creation / linking (so no out-of-domain account is ever created).
      assertThat(userCreation, notNullValue());
      assertThat(verifier.getPriority(), lessThan(userCreation.getPriority()));
    } finally {
      keycloak.realm(realmName).remove();
    }
  }

  private static AuthenticationExecutionInfoRepresentation findTopLevel(
      List<AuthenticationExecutionInfoRepresentation> executions, String providerId) {
    return executions.stream()
        .filter(e -> e.getLevel() == 0)
        .filter(e -> providerId.equals(e.getProviderId()))
        .findFirst()
        .orElse(null);
  }

  private static AuthenticationExecutionInfoRepresentation findTopLevelByDisplay(
      List<AuthenticationExecutionInfoRepresentation> executions, String displayName) {
    return executions.stream()
        .filter(e -> e.getLevel() == 0)
        .filter(e -> displayName.equals(e.getDisplayName()))
        .findFirst()
        .orElse(null);
  }
}
