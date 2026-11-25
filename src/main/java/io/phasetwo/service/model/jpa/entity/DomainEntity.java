package io.phasetwo.service.model.jpa.entity;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** */
@NamedQueries({
  @NamedQuery(
      name = "getDomainsByName",
      query =
          "SELECT t FROM DomainEntity t WHERE (lower(t.domain) = :domain OR lower(t.domain) = :unicodeDomain OR lower(t.domain) = :rawDomain) AND t.organization.realmId = :realmId"),
  @NamedQuery(
      name = "getVerifiedDomainsByName",
      query =
          "SELECT t FROM DomainEntity t WHERE (lower(t.domain) = :domain OR lower(t.domain) = :unicodeDomain OR lower(t.domain) = :rawDomain) AND t.verified = :verified AND t.organization.realmId = :realmId"),
  @NamedQuery(
      name = "getDomainsByOrganization",
      query = "SELECT t FROM DomainEntity t WHERE t.organization = :organization"),
  @NamedQuery(
      name = "getDomainByOrganizationAndDomainName",
      query =
          "SELECT t FROM DomainEntity t WHERE t.organization = :organization AND (lower(t.domain) = :search OR lower(t.domain) = :unicodeSearch OR lower(t.domain) = :rawSearch)"),
  @NamedQuery(
      name = "getDomainCount",
      query = "select count(t) from DomainEntity t where t.organization = :organization")
})
@Entity
@Table(
    name = "ORGANIZATION_DOMAIN",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"ORGANIZATION_ID", "DOMAIN"})})
public class DomainEntity {
  @Id
  @Column(name = "ID", length = 36)
  @Access(AccessType.PROPERTY)
  protected String id;

  @Column(name = "DOMAIN")
  protected String domain;

  @Column(name = "VERIFIED")
  private boolean verified;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ORGANIZATION_ID")
  private ExtOrganizationEntity organization;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public boolean isVerified() {
    return verified;
  }

  public void setVerified(boolean verified) {
    this.verified = verified;
  }

  public ExtOrganizationEntity getOrganization() {
    return organization;
  }

  public void setOrganization(ExtOrganizationEntity organization) {
    this.organization = organization;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (!(o instanceof DomainEntity)) return false;

    DomainEntity that = (DomainEntity) o;

    if (!id.equals(that.id)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
