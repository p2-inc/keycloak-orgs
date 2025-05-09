package io.phasetwo.service.model.jpa.entity;

import static io.phasetwo.service.model.jpa.entity.Entities.setCollection;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/** */
@NamedQueries({
  @NamedQuery(
      name = "getInvitationByOrganization",
      query =
          "SELECT t FROM InvitationEntity t WHERE t.organization = :organization AND t.id = :id"),
  @NamedQuery(
          name = "getInvitationById",
          query =
                  "SELECT t FROM InvitationEntity t WHERE t.id = :id AND t.organization.realmId = :realmId"),
  @NamedQuery(
      name = "getInvitationsByOrganizationAndEmail",
      query =
          "SELECT t FROM InvitationEntity t WHERE t.organization = :organization AND lower(t.email) LIKE lower(:search) ORDER BY t.email"),
  @NamedQuery(
      name = "getInvitationsByRealmAndEmail",
      query =
          "SELECT i FROM InvitationEntity i WHERE i.organization in (SELECT o FROM ExtOrganizationEntity o WHERE o.realmId = :realmId) AND lower(i.email) = lower(:search) ORDER BY i.createdAt"),
  @NamedQuery(
      name = "getInvitationCount",
      query = "SELECT COUNT(t) FROM InvitationEntity t WHERE t.organization = :organization")
})
@Entity
@Table(
    name = "INVITATION",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"ORGANIZATION_ID", "EMAIL"})})
public class InvitationEntity {
  @Id
  @Column(name = "ID", length = 36)
  @Access(
      AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This
  // avoids an extra SQL
  protected String id;

  @Email
  @Column(name = "EMAIL")
  protected String email;

  @Column(name = "URL")
  protected String url;

  @Column(name = "INVITER_ID")
  protected String inviterId;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATED_AT")
  protected Date createdAt;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ORGANIZATION_ID")
  private ExtOrganizationEntity organization;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "invitation")
  protected Collection<InvitationAttributeEntity> attributes =
      new ArrayList<InvitationAttributeEntity>();

  @ElementCollection
  @Column(name = "ROLE")
  @CollectionTable(
      name = "INVITATION_ROLE",
      joinColumns = {@JoinColumn(name = "INVITATION_ID")})
  protected Set<String> roles = new HashSet();

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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getInviterId() {
    return inviterId;
  }

  public void setInviterId(String inviterId) {
    this.inviterId = inviterId;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date at) {
    createdAt = at;
  }

  public ExtOrganizationEntity getOrganization() {
    return organization;
  }

  public void setOrganization(ExtOrganizationEntity organization) {
    this.organization = organization;
  }

  public Set<String> getRoles() {
    return roles;
  }

  public void setRoles(Set<String> roles) {
    this.roles = roles;
  }

  public Collection<InvitationAttributeEntity> getAttributes() {
    return attributes;
  }

  public void setAttributes(Collection<InvitationAttributeEntity> attributes) {
    setCollection(attributes, this.attributes);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (!(o instanceof InvitationEntity)) return false;

    InvitationEntity that = (InvitationEntity) o;

    if (!id.equals(that.id)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
