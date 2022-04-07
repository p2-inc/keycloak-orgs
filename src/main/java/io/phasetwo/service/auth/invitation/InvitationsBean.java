package io.phasetwo.service.auth.invitation;

import io.phasetwo.service.model.InvitationModel;
import java.util.List;
import java.util.stream.Collectors;
import org.keycloak.models.RealmModel;

public class InvitationsBean {

  private final List<Organization> orgs;
  private String realm;

  public InvitationsBean(RealmModel realm, List<InvitationModel> invites) {
    this.realm = realm != null ? realm.getName() : null;
    this.orgs =
        invites.stream()
            .map(
                i ->
                    new Organization(
                        i.getOrganization().getId(),
                        i.getOrganization().getName(),
                        i.getOrganization().getDisplayName()))
            .collect(Collectors.toList());
  }

  public List<Organization> getOrgs() {
    return this.orgs;
  }

  public static class Organization {

    private final String id;
    private final String name;
    private final String displayName;

    public Organization(String id, String name, String displayName) {
      this.id = id;
      this.name = name;
      this.displayName = displayName == null ? name : displayName;
    }

    public String getId() {
      return this.id;
    }

    public String getName() {
      return this.name;
    }

    public String getDisplayName() {
      return this.displayName;
    }
  }
}
