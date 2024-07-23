//package io.phasetwo.service.auth.idp.discovery.email;
package io.phasetwo.service.auth.idp.discovery.email;

import java.util.Objects;
import io.phasetwo.service.auth.idp.PublicAPI;

@PublicAPI(unstable = true)
public final class Domain {

    private final String value;

    Domain(String value) {
        Objects.requireNonNull(value);
        this.value = value.toLowerCase();
    }

    boolean isSubDomainOf(Domain domain) {
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
