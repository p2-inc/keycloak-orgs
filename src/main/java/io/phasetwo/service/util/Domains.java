package io.phasetwo.service.util;

import java.util.Optional;
import java.util.Set;

public final class Domains {

  public static Optional<String> extract(String usernameOrEmail) {
    if (usernameOrEmail != null) {
      int atIndex = usernameOrEmail.trim().lastIndexOf("@");
      if (atIndex >= 0) {
        String strDomain = usernameOrEmail.trim().substring(atIndex + 1);
        if (!strDomain.isEmpty()) {
          return Optional.of(strDomain);
        }
      }
    }
    return Optional.empty();
  }

  public static boolean supportsDomain(Set<String> orgDomains, String userEmailDomain) {
    return orgDomains.stream()
        .anyMatch(it -> it.equalsIgnoreCase(userEmailDomain) || isSubDomainOf(userEmailDomain, it));
  }

  private static boolean isSubDomainOf(String subdomain, String orgDomain) {
    return subdomain.length() > orgDomain.length()
        && subdomain
            .substring(subdomain.length() - orgDomain.length() - 1)
            .equalsIgnoreCase("." + orgDomain);
  }
}
