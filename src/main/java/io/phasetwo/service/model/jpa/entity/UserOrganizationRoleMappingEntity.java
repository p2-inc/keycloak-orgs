package io.phasetwo.service.model.jpa.entity;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import java.util.Date;
import java.util.Objects;
import org.keycloak.models.jpa.entities.UserEntity;

/**
 * Maps a User within a specific Organization to a Role. This is different than
 * UserRoleMappingEntity, as the role only applies to the User for the given Organization. This is
 * necessary, as it is legal for a User to be a member of more than one Organization
 */
@NamedQueries({
  @NamedQuery(
      name = "getMappingByRole",
      query = "SELECT m FROM UserOrganizationRoleMappingEntity m  WHERE m.role=:role"),
  @NamedQuery(
      name = "getMappingByRoleExcludeAdmin",
      query =
          "SELECT m FROM UserOrganizationRoleMappingEntity m  WHERE m.role=:role AND NOT (m.user.username LIKE 'org-admin-%' AND LENGTH(m.user.username) = 46)"),
  @NamedQuery(
      name = "getMappingByRoleAndUser",
      query =
          "SELECT m FROM UserOrganizationRoleMappingEntity m WHERE m.user.id = :userId AND m.role = :role"),
  @NamedQuery(
      name = "getMappingsByUser",
      query =
          "SELECT m FROM UserOrganizationRoleMappingEntity m WHERE m.user = :user AND m.role.organization.id = :orgId"),
  @NamedQuery(
      name = "deleteMappingsByRoleAndUser",
      query =
          "DELETE FROM UserOrganizationRoleMappingEntity m WHERE m.role = :role AND m.user = :user"),
  @NamedQuery(
      name = "deleteMappingsByUser",
      query = "DELETE FROM UserOrganizationRoleMappingEntity m WHERE m.user = :user")
})
@Table(
    name = "USER_ORGANIZATION_ROLE_MAPPING",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"USER_ID", "ROLE_ID"})})
@Entity
public class UserOrganizationRoleMappingEntity {

  @Id
  @Column(name = "ID", length = 36)
  @Access(
      AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This
  // avoids an extra SQL
  protected String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "USER_ID")
  protected UserEntity user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ROLE_ID")
  protected OrganizationRoleEntity role;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATED_AT")
  protected Date createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) createdAt = new Date();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUserId() {
    return user.getId();
  }

  public UserEntity getUser() {
    return user;
  }

  public void setUser(UserEntity user) {
    this.user = user;
  }

  public OrganizationRoleEntity getRole() {
    return role;
  }

  public void setRole(OrganizationRoleEntity role) {
    this.role = role;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date at) {
    createdAt = at;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (!(o instanceof UserOrganizationRoleMappingEntity)) return false;

    UserOrganizationRoleMappingEntity key = (UserOrganizationRoleMappingEntity) o;

    if (!user.equals(key.user)) return false;
    if (!role.equals(key.role)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(role, user);
  }
}
