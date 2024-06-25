package io.phasetwo.service.model;

import io.phasetwo.service.model.jpa.OrganizationsConfigAdapter;
import io.phasetwo.service.representation.OrganizationsConfig;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.Provider;

public interface OrganizationsConfigProvider extends Provider {

    OrganizationsConfigModel addConfig(RealmModel realm, OrganizationsConfig config);

    OrganizationsConfigModel getConfig(RealmModel realm);

    OrganizationsConfigAdapter update(RealmModel realm, OrganizationsConfigModel existingConfig);
}
