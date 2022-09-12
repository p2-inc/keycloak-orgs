package io.phasetwo.service.model;

public interface DomainModel {

  String getDomain();

  boolean isVerified();

  void setVerified(boolean verified);

  OrganizationModel getOrganization();
}
