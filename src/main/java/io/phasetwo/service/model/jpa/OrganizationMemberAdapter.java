package io.phasetwo.service.model.jpa;

import io.phasetwo.service.model.OrganizationMemberModel;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.jpa.entity.OrganizationMemberAttributeEntity;
import io.phasetwo.service.model.jpa.entity.OrganizationMemberEntity;
import io.phasetwo.service.model.jpa.entity.OrganizationRoleEntity;
import io.phasetwo.service.model.jpa.entity.UserOrganizationRoleMappingEntity;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.JpaModel;
import org.keycloak.models.utils.KeycloakModelUtils;

public class OrganizationMemberAdapter
    implements OrganizationMemberModel, JpaModel<OrganizationMemberEntity> {

  protected final KeycloakSession session;
  protected final OrganizationMemberEntity organizationMemberEntity;
  protected final EntityManager em;
  protected final RealmModel realm;

  public OrganizationMemberAdapter(
      KeycloakSession session,
      RealmModel realm,
      EntityManager em,
      OrganizationMemberEntity organizationMemberEntity) {
    this.session = session;
    this.em = em;
    this.organizationMemberEntity = organizationMemberEntity;
    this.realm = realm;
  }

  @Override
  public OrganizationMemberEntity getEntity() {
    return organizationMemberEntity;
  }

  @Override
  public String getId() {
    return organizationMemberEntity.getId();
  }

  @Override
  public String getUserId() {
    return organizationMemberEntity.getUserId();
  }

  @Override
  public OrganizationModel getOrganization() {
    return new OrganizationAdapter(session, realm, em, organizationMemberEntity.getOrganization());
  }

  @Override
  public List<String> getRoles() {
    return organizationMemberEntity.getOrganization().getRoles().stream()
        .flatMap(organizationRoleEntity -> organizationRoleEntity.getUserMappings().stream())
        .filter(
            userOrganizationRoleMappingEntity ->
                userOrganizationRoleMappingEntity
                    .getUserId()
                    .equals(organizationMemberEntity.getUserId()))
        .map(UserOrganizationRoleMappingEntity::getRole)
        .map(OrganizationRoleEntity::getName)
        .toList();
  }

  @Override
  public Map<String, List<String>> getAttributes() {
    MultivaluedHashMap<String, String> result = new MultivaluedHashMap<>();
    for (OrganizationMemberAttributeEntity attr : organizationMemberEntity.getAttributes()) {
      result.add(attr.getName(), attr.getValue());
    }
    return result;
  }

  @Override
  public void removeAttribute(String name) {
    organizationMemberEntity
        .getAttributes()
        .removeIf(attribute -> attribute.getName().equals(name));
  }

  @Override
  public void removeAttributes() {
    organizationMemberEntity.getAttributes().clear();
  }

  @Override
  public void setAttribute(String name, List<String> values) {
    removeAttribute(name);
    for (String value : values) {
      OrganizationMemberAttributeEntity a = new OrganizationMemberAttributeEntity();
      a.setId(KeycloakModelUtils.generateId());
      a.setName(name);
      a.setValue(value);
      a.setOrganizationMember(organizationMemberEntity);
      em.persist(a);
      organizationMemberEntity.getAttributes().add(a);
    }
  }
}
