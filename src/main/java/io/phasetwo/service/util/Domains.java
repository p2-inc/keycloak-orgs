package io.phasetwo.service.util;

import java.net.IDN;
import java.util.Locale;
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

  public static Optional<String> normalize(String domain) {
    if (domain == null || domain.isEmpty()) {
      return Optional.empty();
    }
    try {
      // IDN uses IDNA2003, which maps some non-ASCII lookalikes to ASCII letters.
      for (int offset = 0; offset < domain.length(); ) {
        int codePoint = domain.codePointAt(offset);
        if (codePoint > 127) {
          String label =
              IDN.toASCII(new String(Character.toChars(codePoint)), IDN.USE_STD3_ASCII_RULES);
          if (!label.startsWith("xn--")) {
            return Optional.empty();
          }
        }
        offset += Character.charCount(codePoint);
      }
      String ascii = IDN.toASCII(domain, IDN.USE_STD3_ASCII_RULES).toLowerCase(Locale.ROOT);
      return ascii.isEmpty()
              || ascii.equals(".")
              || ascii.length() > (ascii.endsWith(".") ? 254 : 253)
          ? Optional.empty()
          : Optional.of(ascii);
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public static boolean equalDomains(String first, String second) {
    Optional<String> normalizedFirst = normalize(first);
    return normalizedFirst.isPresent() && normalizedFirst.equals(normalize(second));
  }

  public static boolean supportsDomain(Set<String> orgDomains, String userEmailDomain) {
    Optional<String> normalizedUserDomain = normalize(userEmailDomain);
    if (normalizedUserDomain.isEmpty()) {
      return false;
    }
    return orgDomains.stream()
        .map(Domains::normalize)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .anyMatch(
            domain ->
                domain.equals(normalizedUserDomain.get())
                    || normalizedUserDomain.get().endsWith("." + domain));
  }
}
