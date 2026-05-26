package io.phasetwo.service.resource;

import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PostgreSQLCompatibilityTest extends AbstractDbCompatibilityTest {

  @Override
  protected JdbcDatabaseContainer<?> createDbContainer(Network network) {
    return new PostgreSQLContainer<>("postgres:18")
        .withNetwork(network)
        .withNetworkAliases("postgres-db")
        .withDatabaseName("keycloak")
        .withUsername("keycloak")
        .withPassword("keycloak");
  }

  @Override
  protected String getDbVendor() {
    return "postgres";
  }

  @Override
  protected String getDbInternalUrl() {
    return "jdbc:postgresql://postgres-db:5432/keycloak";
  }
}
