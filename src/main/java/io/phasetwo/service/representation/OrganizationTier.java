package io.phasetwo.service.representation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.keycloak.representations.idm.RoleRepresentation;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationTier {

  private String id;
  private RoleRepresentation role;
  private String expireDate;
}
