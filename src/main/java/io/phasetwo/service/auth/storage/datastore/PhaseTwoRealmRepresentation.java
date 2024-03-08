package io.phasetwo.service.auth.storage.datastore;

import io.phasetwo.service.auth.storage.datastore.representation.OrganizationRepresentation;
import io.phasetwo.service.representation.Organization;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.keycloak.representations.idm.RealmRepresentation;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class PhaseTwoRealmRepresentation extends RealmRepresentation {

    private List<OrganizationRepresentation> organizations;
}
