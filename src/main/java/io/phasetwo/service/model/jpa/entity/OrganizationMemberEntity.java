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

/** */
@NamedQueries({
  @NamedQuery(
      name = "getOrganizationMembersCount",
      query =
          "SELECT COUNT(m) FROM OrganizationMemberEntity m WHERE m.organization = :organization"),
  @NamedQuery(
      name = "getOrganizationMembers",
      query =
          "SELECT m FROM OrganizationMemberEntity m WHERE m.organization = :organization ORDER BY m.createdAt"),
  @NamedQuery(
      name = "getOrganizationMemberByUserId",
      query =
          "SELECT m FROM OrganizationMemberEntity m WHERE m.organization = :organization AND m.userId = :id"),
  @NamedQuery(
      name = "getOrganizationMembershipsByUserId",
      query = "SELECT m FROM OrganizationMemberEntity m WHERE m.userId = :id")
})
@Table(
    name = "ORGANIZATION_MEMBER",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"ORGANIZATION_ID", "USER_ID"})})
@Entity
public class OrganizationMemberEntity {

  @Id
  @Column(name = "ID", length = 36)
  @Access(
      AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This
  // avoids an extra SQL
  protected String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ORGANIZATION_ID")
  protected OrganizationEntity organization;

  @Column(name = "USER_ID")
  protected String userId;

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
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public OrganizationEntity getOrganization() {
    return organization;
  }

  public void setOrganization(OrganizationEntity organization) {
    this.organization = organization;
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
    if (!(o instanceof OrganizationMemberEntity)) return false;

    OrganizationMemberEntity key = (OrganizationMemberEntity) o;

    if (!userId.equals(key.userId)) return false;
    if (!organization.equals(key.organization)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(organization, userId);
  }
}
