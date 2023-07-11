package io.phasetwo.service.model.jpa.entity;

import static io.phasetwo.service.model.jpa.entity.Entities.setCollection;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import org.hibernate.annotations.Nationalized;

// import org.hibernate.validator.constraints.URL; todo

/** */
@NamedQueries({
  @NamedQuery(
      name = "getOrganizationbyRealmIdAndId",
      query = "SELECT o FROM OrganizationEntity o WHERE o.realmId = :realmId AND o.id = :id"),
  @NamedQuery(
      name = "getOrganizationsByRealmId",
      query = "SELECT o FROM OrganizationEntity o WHERE o.realmId = :realmId"),
  @NamedQuery(
      name = "getOrganizationsByRealmIdAndName",
      query =
          "SELECT o FROM OrganizationEntity o WHERE o.realmId = :realmId AND lower(o.name) LIKE lower(:search) ORDER BY o.name"),
  @NamedQuery(
      name = "countOrganizationsByRealmIdAndName",
      query =
          "SELECT count(o) FROM OrganizationEntity o WHERE o.realmId = :realmId AND lower(o.name) LIKE lower(:search)"),
  @NamedQuery(
      name = "getOrganizationCount",
      query = "select count(o) from OrganizationEntity o where o.realmId = :realmId"),
  @NamedQuery(
      name = "removeAllOrganizations",
      query = "delete from OrganizationEntity o where o.realmId = :realmId")
})
@Entity
@Table(
    name = "ORGANIZATION",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"REALM_ID", "NAME"})})
public class OrganizationEntity {
  @Id
  @Column(name = "ID", length = 36)
  @Access(
      AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This
  // avoids an extra SQL
  protected String id;

  @Column(name = "NAME", nullable = false)
  protected String name;

  @Nationalized
  @Column(name = "DISPLAY_NAME")
  protected String displayName;

  @Column(name = "URL")
  protected String url;

  @Column(name = "REALM_ID", nullable = false)
  protected String realmId;

  @Column(name = "CREATED_BY_USER_ID")
  protected String createdBy;

  @OneToMany(
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      mappedBy = "organization")
  protected Collection<DomainEntity> domains = new ArrayList<DomainEntity>();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "organization")
  protected Collection<OrganizationAttributeEntity> attributes =
      new ArrayList<OrganizationAttributeEntity>();

  @OneToMany(
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      mappedBy = "organization")
  protected Collection<OrganizationMemberEntity> members =
      new ArrayList<OrganizationMemberEntity>();

  @OneToMany(
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      mappedBy = "organization")
  protected Collection<OrganizationRoleEntity> roles = new ArrayList<OrganizationRoleEntity>();

  @OneToMany(
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      mappedBy = "organization")
  protected Collection<TeamEntity> teams = new ArrayList<TeamEntity>();

  @OneToMany(
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      mappedBy = "organization")
  protected Collection<InvitationEntity> invitations = new ArrayList<InvitationEntity>();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Collection<DomainEntity> getDomains() {
    return domains;
  }

  public void setDomains(Collection<DomainEntity> domains) {
    setCollection(domains, this.domains);
  }

  public Collection<OrganizationAttributeEntity> getAttributes() {
    return attributes;
  }

  public void setAttributes(Collection<OrganizationAttributeEntity> attributes) {
    setCollection(attributes, this.attributes);
  }

  public Collection<OrganizationMemberEntity> getMembers() {
    return members;
  }

  public void setMembers(Collection<OrganizationMemberEntity> members) {
    setCollection(members, this.members);
  }

  public Collection<OrganizationRoleEntity> getRoles() {
    return roles;
  }

  public void setRoles(Collection<OrganizationRoleEntity> roles) {
    setCollection(roles, this.roles);
  }

  public Collection<TeamEntity> getTeams() {
    return teams;
  }

  public void setTeams(Collection<TeamEntity> teams) {
    setCollection(teams, this.teams);
  }

  public Collection<InvitationEntity> getInvitations() {
    return invitations;
  }

  public void setInvitations(Collection<InvitationEntity> invitations) {
    setCollection(invitations, this.invitations);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getRealmId() {
    return realmId;
  }

  public void setRealmId(String realmId) {
    this.realmId = realmId;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (!(o instanceof OrganizationEntity)) return false;

    OrganizationEntity that = (OrganizationEntity) o;

    if (!id.equals(that.id)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
