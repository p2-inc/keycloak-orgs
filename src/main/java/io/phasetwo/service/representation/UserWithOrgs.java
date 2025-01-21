package io.phasetwo.service.representation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.keycloak.representations.idm.UserRepresentation;

@Getter
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class UserWithOrgs extends UserRepresentation {

  final Map<String, List<OrganizationRole>> organizations = Maps.newHashMap();

  public UserWithOrgs(UserRepresentation user) {
    super(user);
  }

  @JsonIgnore
  public void addOrganization(String id, List<OrganizationRole> roles) {
    this.organizations.put(id, roles);
  }
}
