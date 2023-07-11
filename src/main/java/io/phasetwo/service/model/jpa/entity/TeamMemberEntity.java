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
      name = "getTeamMembers",
      query = "SELECT m FROM TeamMemberEntity m WHERE m.team = :team ORDER BY m.createdAt"),
  @NamedQuery(
      name = "getTeamMemberByUserId",
      query = "SELECT m FROM TeamMemberEntity m WHERE m.team = :team AND m.userId = :id"),
  @NamedQuery(
      name = "getTeamMembershipsByUserId",
      query = "SELECT m FROM TeamMemberEntity m WHERE m.userId = :id"),
})
@Table(
    name = "TEAM_MEMBER",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"TEAM_ID", "USER_ID"})})
@Entity
public class TeamMemberEntity {

  @Id
  @Column(name = "ID", length = 36)
  @Access(
      AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This
  // avoids an extra SQL
  protected String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TEAM_ID")
  protected TeamEntity team;

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

  public TeamEntity getTeam() {
    return team;
  }

  public void setTeam(TeamEntity team) {
    this.team = team;
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
    if (!(o instanceof TeamMemberEntity)) return false;

    TeamMemberEntity key = (TeamMemberEntity) o;

    if (!userId.equals(key.userId)) return false;
    if (!team.equals(key.team)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(team, userId);
  }
}
