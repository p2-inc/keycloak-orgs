package io.phasetwo.service.model.jpa.entity;

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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
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
