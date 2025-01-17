package io.phasetwo.service.representation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.keycloak.representations.idm.UserRepresentation;

public class UserWithOrgs extends UserRepresentation {

  Map<String, List<OrganizationRole>> organizations;

  public UserWithOrgs(UserRepresentation user) {
    super(user);
    this.organizations = Maps.newHashMap();
  }

  public Map<String, List<OrganizationRole>> getOrganizations() {
    return this.organizations;
  }

  public void setOrganizations(Map<String, List<OrganizationRole>> organizations) {
    this.organizations = organizations;
  }

  @JsonIgnore
  public void addOrganization(String id, List<OrganizationRole> roles) {
    this.organizations.put(id, roles);
  }
}
