package io.phasetwo.service.resource;

import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MySQLCompatibilityTest extends AbstractDbCompatibilityTest {

  @Override
  protected JdbcDatabaseContainer<?> createDbContainer(Network network) {
    return new MySQLContainer<>("mysql:8.4")
        .withNetwork(network)
        .withNetworkAliases("mysql-db")
        .withDatabaseName("keycloak")
        .withUsername("keycloak")
        .withPassword("keycloak");
  }

  @Override
  protected String getDbVendor() {
    return "mysql";
  }

  @Override
  protected String getDbInternalUrl() {
    return "jdbc:mysql://mysql-db:3306/keycloak";
  }
}
