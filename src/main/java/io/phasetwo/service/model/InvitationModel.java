package io.phasetwo.service.model;

import java.util.Date;
import org.keycloak.models.UserModel;

public interface InvitationModel {

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
}
