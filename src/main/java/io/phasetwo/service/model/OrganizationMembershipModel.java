package io.phasetwo.service.model;

public interface OrganizationMembershipModel extends WithAttributes {

  String getId();

  String getUserId();

  OrganizationModel getOrganization();
}
