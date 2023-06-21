package io.phasetwo.service.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import org.keycloak.representations.idm.UserRepresentation;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
public class UserRepresentationWithRoles {
    private @Valid UserRepresentation user = null;
    private @Valid List<OrganizationRole> organizationRoles = Lists.newArrayList();

    public UserRepresentationWithRoles user(UserRepresentation user) {
        this.user = user;
        return this;
    }
    @JsonProperty("user")
    public UserRepresentation getUser() {
        return user;
    }

    public void setId(UserRepresentation user) {
        this.user = user;
    }

    public UserRepresentationWithRoles organizationRoles(List<OrganizationRole> orgRoles) {
        this.organizationRoles = orgRoles;
        return this;
    }
    @JsonProperty("organizationRoles")
    public List<OrganizationRole> getOrganizationRoles() {
        return organizationRoles;
    }


    public void setOrganizationRoles(String name) {
        this.organizationRoles = organizationRoles;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserRepresentationWithRoles userRepresentationWithRole = (UserRepresentationWithRoles) o;
        return Objects.equals(user, userRepresentationWithRole.user) &&
                Objects.equals(organizationRoles, userRepresentationWithRole.organizationRoles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, organizationRoles);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserRepresentationWithRole {\n");
        sb.append("    user: ").append(toIndentedString(user)).append("\n");
        sb.append("    orgRoles: ").append(toIndentedString(organizationRoles)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
