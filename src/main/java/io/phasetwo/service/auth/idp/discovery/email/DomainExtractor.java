//package io.phasetwo.service.auth.idp.discovery.email;
package io.phasetwo.service.auth.idp.discovery.email;

import org.jboss.logging.Logger;
import org.keycloak.models.UserModel;

import java.util.Optional;

final class DomainExtractor {

    private static final Logger LOG = Logger.getLogger(DomainExtractor.class);

    private final EmailHomeIdpDiscovererConfig config;

    DomainExtractor(EmailHomeIdpDiscovererConfig config) {
        this.config = config;
    }

    Optional<Domain> extractFrom(UserModel user) {
        if (!user.isEnabled()) {
            LOG.warnf("User '%s' not enabled", user.getId());
            return Optional.empty();
        }
        String userAttribute = user.getFirstAttribute(config.userAttribute());
        if (userAttribute == null) {
            LOG.warnf("Could not find user attribute '%s' for user '%s'", config.userAttribute(), user.getId());
            return Optional.empty();
        }
        return extractFrom(userAttribute);
    }

    Optional<Domain> extractFrom(String usernameOrEmail) {
        Domain domain = null;
        if (usernameOrEmail != null) {
            int atIndex = usernameOrEmail.trim().lastIndexOf("@");
            if (atIndex >= 0) {
                String strDomain = usernameOrEmail.trim().substring(atIndex + 1);
                if (strDomain.length() > 0) {
                    domain = new Domain(strDomain);
                }
            }
        }
        return Optional.ofNullable(domain);
    }

}
