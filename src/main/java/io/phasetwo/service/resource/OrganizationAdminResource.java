package io.phasetwo.service.resource;

import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.model.OrganizationsConfigProvider;
import jakarta.persistence.EntityManager;

import java.net.URI;
import java.net.URISyntaxException;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;

/**
 *
 */
@JBossLog
public class OrganizationAdminResource extends AbstractAdminResource<OrganizationAdminAuth> {

    protected OrganizationProvider orgs;
    protected OrganizationsConfigProvider orgsConfig;
    protected EntityManager em;

    protected OrganizationAdminResource(KeycloakSession session) {
        super(session);
        init();
    }

    protected OrganizationAdminResource(OrganizationAdminResource parent) {
        super(parent);
        init();
    }

    protected final String getServerUrl() {
        URI u = session.getContext().getUri().getRequestUri();
        try {
            return new URI(u.getScheme(), null, u.getHost(), u.getPort(), "/", null, null).toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    protected final void init() {
        this.em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        this.orgs = session.getProvider(OrganizationProvider.class);
        this.orgsConfig = session.getProvider(OrganizationsConfigProvider.class);
    }
}
