package io.phasetwo.service.representation;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrganizationTierMapping {

  private List<OrganizationTier> realmMappings = new ArrayList<>();
}
