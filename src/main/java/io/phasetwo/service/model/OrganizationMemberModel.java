package io.phasetwo.service.model;

import java.util.List;

public interface OrganizationMemberModel extends WithAttributes {

  String getId();

  String getUserId();

  OrganizationModel getOrganization();

  List<String> getRoles();
}
