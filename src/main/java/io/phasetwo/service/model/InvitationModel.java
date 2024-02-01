package io.phasetwo.service.model;

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import org.keycloak.models.UserModel;

public interface InvitationModel extends WithAttributes {

  String getId();

  OrganizationModel getOrganization();

  String getEmail();

  void setEmail(String email);

  String getUrl();

  void setUrl(String url);

  UserModel getInviter();

  void setInviter(UserModel user);

  Date getCreatedAt();

  void setCreatedAt(Date date);

  Set<String> getRoles();

  void setRoles(Collection<String> roles);
}
