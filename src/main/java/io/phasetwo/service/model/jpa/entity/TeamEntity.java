package io.phasetwo.service.model.jpa.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import org.hibernate.annotations.Nationalized;

/** */
@NamedQueries({
  @NamedQuery(
      name = "getTeamIdsByParent",
      query = "select t.id from TeamEntity t where t.parentId = :parent"),
  @NamedQuery(
      name = "getTopLevelTeamIds",
      query =
          "select t.id from TeamEntity t where t.parentId = :parent and t.organization = :organization order by t.name ASC"),
  @NamedQuery(name = "getTeamById", query = "SELECT t FROM TeamEntity t WHERE t.id = :id"),
  @NamedQuery(
      name = "getTeamsByOrganization",
      query = "SELECT t FROM TeamEntity t WHERE t.organization = :organization"),
  @NamedQuery(
      name = "getTeamsByOrganizationIdAndName",
      query =
          "SELECT t FROM TeamEntity t WHERE t.organization = :organization AND lower(t.name) LIKE lower(:search) ORDER BY t.name"),
  @NamedQuery(
      name = "getTeamCount",
      query = "select count(t) from TeamEntity t where t.organization = :organization")
})
@Entity
@Table(
    name = "TEAM",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"ORGANIZATION_ID", "NAME"})})
public class TeamEntity {

  /** ID set in the PARENT column to mark the group as top level. */
  public static String TOP_PARENT_ID = " ";

  @Id
  @Column(name = "ID", length = 36)
  @Access(
      AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This
  // avoids an extra SQL
  protected String id;

  @Nationalized
  @Column(name = "NAME")
  protected String name;

  @Column(name = "PARENT_ID")
  private String parentId;

  @Column(name = "VISIBLE")
  private boolean visible;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ORGANIZATION_ID")
  private OrganizationEntity organization;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "team")
  protected Collection<TeamAttributeEntity> attributes = new ArrayList<TeamAttributeEntity>();

  @OneToMany(
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      mappedBy = "team")
  protected Collection<TeamMemberEntity> members = new ArrayList<TeamMemberEntity>();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Collection<TeamAttributeEntity> getAttributes() {
    return attributes;
  }

  public void setAttributes(Collection<TeamAttributeEntity> attributes) {
    if (this.attributes == null) {
      this.attributes = attributes;
    } else if (this.attributes != attributes) {
      this.attributes.clear();
      if (attributes != null) {
        this.attributes.addAll(attributes);
      }
    }
  }

  public Collection<TeamMemberEntity> getMembers() {
    return members;
  }

  public void setMembers(Collection<TeamMemberEntity> members) {
    if (this.members == null) {
      this.members = members;
    } else if (this.members != members) {
      this.members.clear();
      if (members != null) {
        this.members.addAll(members);
      }
    }
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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
    if (!(o instanceof TeamEntity)) return false;

    TeamEntity that = (TeamEntity) o;

    if (!id.equals(that.id)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
