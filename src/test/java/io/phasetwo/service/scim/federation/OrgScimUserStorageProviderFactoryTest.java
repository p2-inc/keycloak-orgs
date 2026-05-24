package io.phasetwo.service.scim.federation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.Config;

/**
 * Unit tests for the EnvironmentDependentProviderFactory gate on
 * OrgScimUserStorageProviderFactory. These run without docker.
 */
class OrgScimUserStorageProviderFactoryTest {

  private OrgScimUserStorageProviderFactory factory;

  @BeforeEach
  void setUp() {
    factory = new OrgScimUserStorageProviderFactory();
    System.clearProperty(OrgScimUserStorageProviderFactory.SYS_PROP_USER_FEDERATION_UI);
  }

  @AfterEach
  void tearDown() {
    System.clearProperty(OrgScimUserStorageProviderFactory.SYS_PROP_USER_FEDERATION_UI);
  }

  @Test
  void isSupportedReturnsFalseWithNullConfigAndNoSystemProperty() {
    assertThat(factory.isSupported(null), is(false));
  }

  @Test
  void isSupportedReturnsFalseWithEmptyConfigAndNoSystemProperty() {
    assertThat(factory.isSupported(new MapConfigScope(Map.of())), is(false));
  }

  @Test
  void isSupportedReturnsFalseWhenSpiConfigFlagFalse() {
    Config.Scope cfg =
        new MapConfigScope(
            Map.of(OrgScimUserStorageProviderFactory.SPI_CONFIG_USER_FEDERATION_UI, "false"));
    assertThat(factory.isSupported(cfg), is(false));
  }

  @Test
  void isSupportedReturnsTrueWhenSpiConfigFlagTrue() {
    Config.Scope cfg =
        new MapConfigScope(
            Map.of(OrgScimUserStorageProviderFactory.SPI_CONFIG_USER_FEDERATION_UI, "true"));
    assertThat(factory.isSupported(cfg), is(true));
  }

  @Test
  void isSupportedReturnsTrueWhenSystemPropertyTrue() {
    System.setProperty(OrgScimUserStorageProviderFactory.SYS_PROP_USER_FEDERATION_UI, "true");
    // null Config.Scope is sufficient when the system property is set.
    assertThat(factory.isSupported(null), is(true));
    assertThat(factory.isSupported(new MapConfigScope(Map.of())), is(true));
  }

  @Test
  void systemPropertyTakesPrecedenceOverFalseSpiConfig() {
    System.setProperty(OrgScimUserStorageProviderFactory.SYS_PROP_USER_FEDERATION_UI, "true");
    Config.Scope cfg =
        new MapConfigScope(
            Map.of(OrgScimUserStorageProviderFactory.SPI_CONFIG_USER_FEDERATION_UI, "false"));
    assertThat(factory.isSupported(cfg), is(true));
  }

  @Test
  void factoryIdMatchesLegacyValue() {
    // Existing ComponentModel rows in the DB reference the factory by this ID.
    // Changing it would orphan them, so this test pins the value.
    assertThat(OrgScimUserStorageProviderFactory.PROVIDER_ID, is("Organization SCIM"));
    assertThat(factory.getId(), is("Organization SCIM"));
  }

  /**
   * Minimal hand-rolled {@link Config.Scope} for the only methods this gate uses.
   * Avoids pulling in a mocking framework just for one test.
   */
  private static final class MapConfigScope implements Config.Scope {
    private final Map<String, String> values;

    MapConfigScope(Map<String, String> values) {
      this.values = new HashMap<>(values);
    }

    @Override
    public String get(String key) {
      return values.get(key);
    }

    @Override
    public String get(String key, String defaultValue) {
      return values.getOrDefault(key, defaultValue);
    }

    @Override
    public String[] getArray(String key) {
      String v = values.get(key);
      return v == null ? null : v.split(",");
    }

    @Override
    public Integer getInt(String key) {
      String v = values.get(key);
      return v == null ? null : Integer.valueOf(v);
    }

    @Override
    public Integer getInt(String key, Integer defaultValue) {
      Integer v = getInt(key);
      return v == null ? defaultValue : v;
    }

    @Override
    public Long getLong(String key) {
      String v = values.get(key);
      return v == null ? null : Long.valueOf(v);
    }

    @Override
    public Long getLong(String key, Long defaultValue) {
      Long v = getLong(key);
      return v == null ? defaultValue : v;
    }

    @Override
    public Boolean getBoolean(String key) {
      String v = values.get(key);
      return v == null ? null : Boolean.valueOf(v);
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue) {
      Boolean v = getBoolean(key);
      return v == null ? defaultValue : v;
    }

    @Override
    public Config.Scope scope(String... scope) {
      return new MapConfigScope(Map.of());
    }

    @Override
    public java.util.Set<String> getPropertyNames() {
      return values.keySet();
    }

    @Override
    public Config.Scope root() {
      return this;
    }
  }
}
