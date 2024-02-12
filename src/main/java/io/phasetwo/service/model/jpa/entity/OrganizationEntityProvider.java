package io.phasetwo.service.model.jpa.entity;

import java.util.Arrays;
import java.util.List;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;

/** */
public class OrganizationEntityProvider implements JpaEntityProvider {

  // remember to also put new entity classes in src/main/resources/META-INF/persistence.xml
  private static Class<?>[] entities = {
    DomainEntity.class,
    OrganizationEntity.class,
    OrganizationAttributeEntity.class,
    OrganizationMemberEntity.class,
    OrganizationRoleEntity.class,
    TeamEntity.class,
    TeamAttributeEntity.class,
    TeamMemberEntity.class,
    UserOrganizationRoleMappingEntity.class,
    InvitationEntity.class,
    InvitationAttributeEntity.class
  };

  @Override
  public List<Class<?>> getEntities() {
    return Arrays.<Class<?>>asList(entities);
  }

  @Override
  public String getChangelogLocation() {
    return "META-INF/jpa-changelog-phasetwo-master.xml";
  }

  @Override
  public void close() {}

  @Override
  public String getFactoryId() {
    return OrganizationEntityProviderFactory.ID;
  }
}
