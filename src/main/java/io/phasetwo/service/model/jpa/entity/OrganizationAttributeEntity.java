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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import org.hibernate.annotations.Nationalized;

/** */
@NamedQueries({
  @NamedQuery(
      name = "getOrganizationAttributesByNameAndValue",
      query =
          "SELECT attr FROM OrganizationAttributeEntity attr WHERE attr.organization = :organization AND attr.name = :name AND attr.value = :value"),
  @NamedQuery(
      name = "getOrganizationAttributesByName",
      query =
          "SELECT attr FROM OrganizationAttributeEntity attr WHERE attr.organization = :organization AND attr.name = :name"),
})
@Table(
    name = "ORGANIZATION_ATTRIBUTE",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"ORGANIZATION_ID", "NAME"})})
@Entity
public class OrganizationAttributeEntity {

  @Id
  @Column(name = "ID", length = 36)
  @Access(
      AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This
  // avoids an extra SQL
  protected String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ORGANIZATION_ID")
  protected OrganizationEntity organization;

  @Column(name = "NAME")
  protected String name;

  @Nationalized
  @Column(name = "VALUE")
  protected String value;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public OrganizationEntity getOrganization() {
    return organization;
  }

  public void setOrganization(OrganizationEntity organization) {
    this.organization = organization;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (!(o instanceof OrganizationAttributeEntity)) return false;

    OrganizationAttributeEntity key = (OrganizationAttributeEntity) o;

    if (!name.equals(key.name)) return false;
    if (!organization.equals(key.organization)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(organization, name);
  }
}
