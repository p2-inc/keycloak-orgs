package io.phasetwo.service.model;

import java.util.List;
import java.util.Map;

public interface OrganizationMemberModel extends WithAttributes {

  String getId();

  String getUserId();

  OrganizationModel getOrganization();

  List<String> getRoles();
}
