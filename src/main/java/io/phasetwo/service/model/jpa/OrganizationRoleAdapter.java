package io.phasetwo.service.model.jpa;

import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationRoleModel;
import io.phasetwo.service.model.jpa.entity.OrganizationRoleEntity;
import io.phasetwo.service.model.jpa.entity.UserOrganizationRoleMappingEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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
  protected final OrganizationModel org;

  public OrganizationRoleAdapter(
      KeycloakSession session,
      RealmModel realm,
      EntityManager em,
      OrganizationModel org,
      OrganizationRoleEntity role) {
    this.session = session;
    this.em = em;
    this.role = role;
    this.realm = realm;
    this.org = org;
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
    // user must be a member
    if (!org.hasMembership(user)) return;
    // skip if they already have the role
    if (hasRole(user)) return;
    UserOrganizationRoleMappingEntity m = new UserOrganizationRoleMappingEntity();
    m.setId(KeycloakModelUtils.generateId());
    m.setUserId(user.getId());
    m.setRole(role);
    em.persist(m);
    role.getUserMappings().add(m);
  }

  @Override
  public void revokeRole(UserModel user) {
    UserOrganizationRoleMappingEntity e = getByUser(user);
    if (e != null) {
      role.getUserMappings().remove(e);
      em.remove(e);
      em.flush();
    }
  }

  @Override
  public boolean hasRole(UserModel user) {
    return (getByUser(user) != null);
  }

  UserOrganizationRoleMappingEntity getByUser(UserModel user) {
    TypedQuery<UserOrganizationRoleMappingEntity> query =
        em.createNamedQuery("getMappingByRoleAndUser", UserOrganizationRoleMappingEntity.class);
    query.setParameter("userId", user.getId());
    query.setParameter("role", role);
    try {
      return query.getSingleResult();
    } catch (Exception ignore) {
      return null;
    }
  }
}
