package io.phasetwo.service.resource;

import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.Network;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MariaDBCompatibilityTest extends AbstractDbCompatibilityTest {

  @Override
  protected JdbcDatabaseContainer<?> createDbContainer(Network network) {
    return new MariaDBContainer<>("mariadb:11.8")
        .withNetwork(network)
        .withNetworkAliases("mariadb-db")
        .withDatabaseName("keycloak")
        .withUsername("keycloak")
        .withPassword("keycloak");
  }

  @Override
  protected String getDbVendor() {
    return "mariadb";
  }

  @Override
  protected String getDbInternalUrl() {
    return "jdbc:mariadb://mariadb-db:3306/keycloak";
  }
}
