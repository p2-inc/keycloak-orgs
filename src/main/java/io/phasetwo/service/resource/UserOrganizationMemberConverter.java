package io.phasetwo.service.resource;

import io.phasetwo.service.model.OrganizationMemberModel;
import io.phasetwo.service.representation.UserOrganizationMember;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.OTPCredentialModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.keycloak.models.light.LightweightUserAdapter.isLightweightUser;

public final class UserOrganizationMemberConverter {

    public static UserOrganizationMember toRepresentation(KeycloakSession session,
                                                          RealmModel realm,
                                                          OrganizationMemberModel organizationMemberModel
                                                                                ) {
        var rep = new UserOrganizationMember();

        var user = session.users().getUserById(realm, organizationMemberModel.getUserId());
        rep.setId(user.getId());
        rep.setUsername(user.getUsername());
        rep.setCreatedTimestamp(user.getCreatedTimestamp());
        rep.setLastName(user.getLastName());
        rep.setFirstName(user.getFirstName());
        rep.setEmail(user.getEmail());
        rep.setEnabled(user.isEnabled());
        rep.setEmailVerified(user.isEmailVerified());
        rep.setTotp(user.credentialManager().isConfiguredFor(OTPCredentialModel.TYPE));
        rep.setDisableableCredentialTypes(user.credentialManager()
                .getDisableableCredentialTypesStream().collect(Collectors.toSet()));
        rep.setFederationLink(user.getFederationLink());
        rep.setNotBefore(isLightweightUser(user) ? user.getCreatedTimestamp().intValue() : session.users().getNotBeforeOfUser(realm, user));
        rep.setRequiredActions(user.getRequiredActionsStream().collect(Collectors.toList()));

        Map<String, List<String>> attributes = user.getAttributes();
        Map<String, List<String>> copy = null;

        if (attributes != null) {
            copy = new HashMap<>(attributes);
            copy.remove(UserModel.LAST_NAME);
            copy.remove(UserModel.FIRST_NAME);
            copy.remove(UserModel.EMAIL);
            copy.remove(UserModel.USERNAME);
        }
        if (attributes != null && !copy.isEmpty()) {
            Map<String, List<String>> attrs = new HashMap<>(copy);
            rep.setAttributes(attrs);
        }

        rep.setOrganizationAttributes(organizationMemberModel.getAttributes());
        rep.setOrganizationId(organizationMemberModel.getOrganization().getId());
        rep.setOrganizationRoles(organizationMemberModel.getRoles());

        return rep;
    }
}
