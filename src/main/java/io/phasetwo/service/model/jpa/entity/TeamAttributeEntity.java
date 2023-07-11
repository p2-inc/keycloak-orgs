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
      name = "getTeamAttributesByNameAndValue",
      query =
          "SELECT attr FROM TeamAttributeEntity attr WHERE attr.team = :team AND attr.name = :name AND attr.value = :value"),
  @NamedQuery(
      name = "getTeamAttributesByName",
      query =
          "SELECT attr FROM TeamAttributeEntity attr WHERE attr.team = :team AND attr.name = :name"),
})
@Table(
    name = "TEAM_ATTRIBUTE",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"TEAM_ID", "NAME"})})
@Entity
public class TeamAttributeEntity {

  @Id
  @Column(name = "ID", length = 36)
  @Access(
      AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This
  // avoids an extra SQL
  protected String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TEAM_ID")
  protected TeamEntity team;

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

  public TeamEntity getTeam() {
    return team;
  }

  public void setTeam(TeamEntity team) {
    this.team = team;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (!(o instanceof TeamAttributeEntity)) return false;

    TeamAttributeEntity key = (TeamAttributeEntity) o;

    if (!name.equals(key.name)) return false;
    if (!team.equals(key.team)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(team, name);
  }
}
