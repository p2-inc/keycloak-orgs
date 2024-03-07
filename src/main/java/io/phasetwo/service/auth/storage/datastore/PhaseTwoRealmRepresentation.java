package io.phasetwo.service.auth.storage.datastore;

import io.phasetwo.service.auth.storage.datastore.representation.OrganizationRepresentation;
import io.phasetwo.service.representation.Organization;
import org.keycloak.representations.idm.RealmRepresentation;

import java.util.List;

public class PhaseTwoRealmRepresentation extends RealmRepresentation {

    protected List<OrganizationRepresentation> organizations;

    public PhaseTwoRealmRepresentation() {
    }

    public List<OrganizationRepresentation> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<OrganizationRepresentation> organizations) {
        this.organizations = organizations;
    }
}
