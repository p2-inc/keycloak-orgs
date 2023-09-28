//package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp;

import org.jboss.logging.Logger;
import org.keycloak.models.UserModel;

import java.util.Optional;

final class DomainExtractor {

    private static final Logger LOG = Logger.getLogger(DomainExtractor.class);
    private static final String EMAIL_ATTRIBUTE = "email";

    private final HomeIdpDiscoveryConfig config;

    DomainExtractor(HomeIdpDiscoveryConfig config) {
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
        if (EMAIL_ATTRIBUTE.equalsIgnoreCase(config.userAttribute()) && !user.isEmailVerified()) {
            LOG.warnf("Email address of user '%s' is not verified", user.getId());
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
