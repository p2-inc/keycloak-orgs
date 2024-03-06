package io.phasetwo.service.auth.storage.datastore;

import io.phasetwo.service.representation.Organization;
import org.keycloak.representations.idm.RealmRepresentation;

import java.util.List;

public class PhaseTwoRealmRepresentation extends RealmRepresentation {

    protected List<Organization> organizations;

    public PhaseTwoRealmRepresentation() {
    }

    public List<Organization> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<Organization> organizations) {
        this.organizations = organizations;
    }
}
