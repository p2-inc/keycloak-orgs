package io.phasetwo.service.model.jpa;

import io.phasetwo.service.model.OrganizationRoleModel;
import io.phasetwo.service.model.jpa.entity.OrganizationRoleEntity;
import io.phasetwo.service.model.jpa.entity.UserOrganizationRoleMappingEntity;
import jakarta.persistence.EntityManager;
import java.util.stream.Stream;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.JpaModel;
import org.keycloak.models.utils.KeycloakModelUtils;

public class OrganizationRoleAdapter
    implements OrganizationRoleModel, JpaModel<OrganizationRoleEntity> {

  protected final KeycloakSession session;
  protected final OrganizationRoleEntity role;
  protected final EntityManager em;
  protected final RealmModel realm;

  public OrganizationRoleAdapter(
      KeycloakSession session, RealmModel realm, EntityManager em, OrganizationRoleEntity role) {
    this.session = session;
    this.em = em;
    this.role = role;
    this.realm = realm;
  }

  @Override
  public OrganizationRoleEntity getEntity() {
    return role;
  }

  @Override
  public String getId() {
    return role.getId();
  }

  @Override
  public String getName() {
    return role.getName();
  }

  @Override
  public void setName(String name) {
    role.setName(name);
  }

  @Override
  public String getDescription() {
    return role.getDescription();
  }

  @Override
  public void setDescription(String description) {
    role.setDescription(description);
  }

  @Override
  public Stream<UserModel> getUserMappingsStream() {
    return role.getUserMappings().stream()
        .map(m -> m.getUserId())
        .map(uid -> session.users().getUserById(realm, uid));
  }

  @Override
  public void grantRole(UserModel user) {
    // todo must be a member
    revokeRole(user);
    UserOrganizationRoleMappingEntity m = new UserOrganizationRoleMappingEntity();
    m.setId(KeycloakModelUtils.generateId());
    m.setUserId(user.getId());
    m.setRole(role);
    em.persist(m);
    role.getUserMappings().add(m);
  }

  @Override
  public void revokeRole(UserModel user) {
    role.getUserMappings().removeIf(m -> m.getUserId().equals(user.getId()));
  }

  @Override
  public boolean hasRole(UserModel user) {
    return role.getUserMappings().stream().anyMatch(m -> m.getUserId().equals(user.getId()));
  }
}
