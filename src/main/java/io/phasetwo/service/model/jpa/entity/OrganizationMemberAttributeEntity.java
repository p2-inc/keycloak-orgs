package io.phasetwo.service.model.jpa.entity;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import org.hibernate.annotations.Nationalized;

@Table(
    name = "ORGANIZATION_MEMBER_ATTRIBUTE",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"ORGANIZATION_MEMBER_ID", "NAME"})})
@Entity
public class OrganizationMemberAttributeEntity {

  @Id
  @Column(name = "ID", length = 36)
  @Access(
      AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This
  // avoids an extra SQL
  protected String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ORGANIZATION_MEMBER_ID")
  protected OrganizationMemberEntity organizationMember;

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

  public OrganizationMemberEntity getOrganizationMember() {
    return organizationMember;
  }

  public void setOrganizationMember(OrganizationMemberEntity organizationMember) {
    this.organizationMember = organizationMember;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    OrganizationMemberAttributeEntity that = (OrganizationMemberAttributeEntity) o;
    return Objects.equals(id, that.id)
        && Objects.equals(organizationMember, that.organizationMember);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, organizationMember);
  }
}
