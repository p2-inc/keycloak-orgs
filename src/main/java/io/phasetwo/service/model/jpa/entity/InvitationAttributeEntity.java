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

/** */
@Table(
    name = "INVITATION_ATTRIBUTE",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"INVITATION_ID", "NAME"})})
@Entity
public class InvitationAttributeEntity {

  @Id
  @Column(name = "ID", length = 36)
  @Access(
      AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This
  // avoids an extra SQL
  protected String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "INVITATION_ID")
  protected InvitationEntity invitation;

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

  public InvitationEntity getInvitation() {
    return invitation;
  }

  public void setInvitation(InvitationEntity invitation) {
    this.invitation = invitation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (!(o instanceof InvitationAttributeEntity)) return false;

    InvitationAttributeEntity key = (InvitationAttributeEntity) o;

    if (!name.equals(key.name)) return false;
    if (!invitation.equals(key.invitation)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(invitation, name);
  }
}
