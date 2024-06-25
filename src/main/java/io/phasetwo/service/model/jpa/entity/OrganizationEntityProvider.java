package io.phasetwo.service.model.jpa.entity;

import java.util.Arrays;
import java.util.List;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;

/** */
public class OrganizationEntityProvider implements JpaEntityProvider {

  private static Class<?>[] entities = {
    DomainEntity.class,
    ExtOrganizationEntity.class,
    OrganizationAttributeEntity.class,
    OrganizationMemberEntity.class,
    OrganizationRoleEntity.class,
    UserOrganizationRoleMappingEntity.class,
    InvitationEntity.class,
    InvitationAttributeEntity.class,
          OrganizationsConfigEntity.class
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
