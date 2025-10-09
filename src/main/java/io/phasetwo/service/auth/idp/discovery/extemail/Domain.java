package io.phasetwo.service.auth.idp.discovery.extemail;

//import de.sventorben.keycloak.authentication.hidpd.PublicAPI;

import io.phasetwo.service.auth.idp.PublicAPI;

import java.util.Objects;

@PublicAPI(unstable = true)
public final class Domain {

    private final String value;

    public Domain(String value) {
        Objects.requireNonNull(value);
        this.value = value.toLowerCase();
    }

    public boolean isSubDomainOf(Domain domain) {
        return this.value.endsWith("." + domain.value);
    }

    public String getRawValue() {
        return this.value;
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
