package io.phasetwo.service.representation;

import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Map;

public class UserOrganizationMember extends UserRepresentation {

    Map<String, List<String>> organizationAttributes;
    String organizationId;
    List<String> organizationRoles;

    public Map<String, List<String>> getOrganizationAttributes() {
        return organizationAttributes;
    }

    public void setOrganizationAttributes(Map<String, List<String>> organizationAttributes) {
        this.organizationAttributes = organizationAttributes;
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
