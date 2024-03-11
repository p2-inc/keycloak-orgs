package io.phasetwo.service.datastore;

import io.phasetwo.service.datastore.representation.OrganizationRepresentation;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.keycloak.representations.idm.RealmRepresentation;

@Setter
@Getter
@NoArgsConstructor
public class KeycloakOrgsRealmRepresentation extends RealmRepresentation {
  private List<OrganizationRepresentation> organizations;
}
