//package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp.discovery.extemail;

import io.phasetwo.service.auth.idp.PublicAPI;
import io.phasetwo.service.util.Domains;

import java.util.Objects;
import java.util.Set;

@PublicAPI(unstable = true)
public final class Domain {

    private final String value;

    Domain(String value) {
        Objects.requireNonNull(value);
        this.value = value;
    }

    boolean isSubDomainOf(Domain domain) {
        return !Domains.equalDomains(this.value, domain.value)
                && Domains.supportsDomain(Set.of(domain.value), this.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Domain))
            return false;
        if (this == obj)
            return true;
        return Domains.equalDomains(this.value, ((Domain) obj).value);
    }

    @Override
    public int hashCode() {
        return Domains.normalize(this.value).orElse(this.value).hashCode();
    }

    @Override
    public String toString() {
        return this.value;
    }
}
