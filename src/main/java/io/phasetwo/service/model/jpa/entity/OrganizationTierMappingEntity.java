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
import java.time.LocalDate;
import lombok.Data;

@Table(
    name = "EXT_P2_ORGANIZATION_ROLE_MAPPING",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"ORGANIZATION_ID", "KC_ROLE_ID"})})
@Entity
@Data
public class OrganizationTierMappingEntity {

  @Id
  @Column(name = "ID", length = 36)
  @Access(AccessType.PROPERTY)
  protected String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ORGANIZATION_ID")
  protected OrganizationEntity organization;

  @Id
  @Column(name = "KC_ROLE_ID")
  protected String roleId;

  @Column(name = "EXPIRE_DATE")
  protected LocalDate expireDate;
}
