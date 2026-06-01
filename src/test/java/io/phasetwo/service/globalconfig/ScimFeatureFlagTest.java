package io.phasetwo.service.globalconfig;

import static io.phasetwo.service.Helpers.objectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.representation.OrganizationsConfig;
import io.restassured.response.Response;
import jakarta.ws.rs.core.Response.Status;
import java.io.IOException;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies the realm-level SCIM feature flag exposed at {@code PUT/GET
 * /realms/{realm}/orgs/config}.
 */
@JBossLog
public class ScimFeatureFlagTest extends AbstractOrganizationTest {

  @AfterEach
  public void resetFlag() throws JsonProcessingException {
    setScimEnabled(false);
  }

  @Test
  void testScimEnabledDefaultsToFalse() throws IOException {
    OrganizationsConfig fetched = getOrgsConfigPayload();
    assertThat(fetched.isScimEnabled(), is(false));
  }

  @Test
  void testScimEnabledRoundtripsTrue() throws IOException {
    setScimEnabled(true);
    OrganizationsConfig fetched = getOrgsConfigPayload();
    assertThat(fetched.isScimEnabled(), is(true));
  }

  @Test
  void testScimEnabledRoundtripsBackToFalse() throws IOException {
    setScimEnabled(true);
    setScimEnabled(false);
    OrganizationsConfig fetched = getOrgsConfigPayload();
    assertThat(fetched.isScimEnabled(), is(false));
  }

  private void setScimEnabled(boolean enabled) throws JsonProcessingException {
    var url = getAuthUrl() + "/realms/master/orgs/config";
    var orgConfig = new OrganizationsConfig();
    orgConfig.setScimEnabled(enabled);
    Response resp = putRequest(orgConfig, url);
    assertThat(resp.getStatusCode(), is(Status.OK.getStatusCode()));
  }

  private OrganizationsConfig getOrgsConfigPayload() throws IOException {
    var url = getAuthUrl() + "/realms/master/orgs/config";
    Response resp = getRequest(keycloak, url);
    assertThat(resp.getStatusCode(), is(Status.OK.getStatusCode()));
    return objectMapper().readValue(resp.getBody().asString(), OrganizationsConfig.class);
  }
}
