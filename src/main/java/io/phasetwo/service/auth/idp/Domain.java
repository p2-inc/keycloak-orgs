//package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp;

import java.util.Objects;

class Domain {

    private final String value;

    Domain(String value) {
        Objects.requireNonNull(value);
        this.value = value.toLowerCase();
    }

    boolean isSubDomainOf(Domain domain) {
        return this.value.endsWith("." + domain.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Domain))
            return false;
        if (this == obj)
            return true;
        return this.value.equalsIgnoreCase(((Domain) obj).value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public String toString() {
        return this.value;
    }
}
