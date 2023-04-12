package io.phasetwo.service.model.jpa.entity;

import java.util.Date;
import java.util.Objects;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

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
