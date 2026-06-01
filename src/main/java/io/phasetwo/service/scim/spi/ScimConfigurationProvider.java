package io.phasetwo.service.scim.spi;

import io.phasetwo.service.scim.ComponentScimConfig;
import org.keycloak.provider.Provider;

/**
 * SPI for managing per-organization SCIM configurations. Abstracts away the underlying persistence
 * mechanism.
 */
public interface ScimConfigurationProvider extends Provider {

  /**
   * Gets the SCIM configuration for the given organization.
   *
   * @param organizationId the organization ID
   * @return the configuration, or null if none exists
   */
  ComponentScimConfig getConfiguration(String organizationId);

  /**
   * Creates a new SCIM configuration for the given organization.
   *
   * @param organizationId the organization ID
   * @param config the configuration to create
   * @return the created configuration
   * @throws IllegalStateException if a configuration already exists for this organization
   */
  ComponentScimConfig createConfiguration(String organizationId, ComponentScimConfig config);

  /**
   * Updates an existing SCIM configuration for the given organization.
   *
   * @param organizationId the organization ID
   * @param config the updated configuration
   * @return the updated configuration
   * @throws IllegalArgumentException if no configuration exists for this organization
   */
  ComponentScimConfig updateConfiguration(String organizationId, ComponentScimConfig config);

  /**
   * Deletes the SCIM configuration for the given organization.
   *
   * @param organizationId the organization ID
   * @throws IllegalArgumentException if no configuration exists for this organization
   */
  void deleteConfiguration(String organizationId);

  /**
   * Checks whether a SCIM configuration exists for the given organization.
   *
   * @param organizationId the organization ID
   * @return true if a configuration exists
   */
  boolean hasConfiguration(String organizationId);
}
