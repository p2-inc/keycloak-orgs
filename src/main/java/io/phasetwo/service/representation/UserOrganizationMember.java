package io.phasetwo.service.representation;

import java.util.List;
import java.util.Map;
import org.keycloak.representations.idm.UserRepresentation;

public class UserOrganizationMember extends UserRepresentation {

  Map<String, List<String>> organizationMemberAttributes;
  String organizationId;
  List<String> organizationRoles;

  public Map<String, List<String>> getOrganizationMemberAttributes() {
    return organizationMemberAttributes;
  }

  public void setOrganizationMemberAttributes(Map<String, List<String>> organizationMemberAttributes) {
    this.organizationMemberAttributes = organizationMemberAttributes;
  }

  public String getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  public List<String> getOrganizationRoles() {
    return organizationRoles;
  }

  public void setOrganizationRoles(List<String> organizationRoles) {
    this.organizationRoles = organizationRoles;
  }
}
