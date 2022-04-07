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
