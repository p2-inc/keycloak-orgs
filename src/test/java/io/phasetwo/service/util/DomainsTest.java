package io.phasetwo.service.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Set;
import org.junit.jupiter.api.Test;

class DomainsTest {

  @Test
  void matchesDomainsAndSubdomainsIgnoringCase() {
    assertThat(Domains.supportsDomain(Set.of("example.com"), "Example.COM"), is(true));
    assertThat(Domains.supportsDomain(Set.of("Example.COM"), "example.com"), is(true));
    assertThat(Domains.supportsDomain(Set.of("ExAmPlE.CoM"), "MAIL.example.COM"), is(true));
    assertThat(Domains.supportsDomain(Set.of("xn--bcher-kva.de"), "B\u00dcCHER.de"), is(true));
    assertThat(Domains.supportsDomain(Set.of("b\u00fccher.de"), "mail.XN--BCHER-KVA.DE"), is(true));
  }

  @Test
  void rejectsLookalikesInvalidAndUnrelatedDomains() {
    assertThat(Domains.supportsDomain(Set.of("example.com"), "notexample.com"), is(false));
    assertThat(
        Domains.supportsDomain(Set.of("example.com"), "example.com.attacker.com"), is(false));
    assertThat(Domains.supportsDomain(Set.of("example.com"), "mail.example.net"), is(false));
    assertThat(Domains.supportsDomain(Set.of("k.com"), "\u212a.com"), is(false));
    assertThat(Domains.supportsDomain(Set.of("k.com"), "mail.\u212a.com"), is(false));
    assertThat(Domains.supportsDomain(Set.of("github.com"), "g\u0131thub.com"), is(false));
    assertThat(Domains.supportsDomain(Set.of("github.com"), "mail.g\u0131thub.com"), is(false));
    assertThat(Domains.supportsDomain(Set.of("esample.com"), "e\u017fample.com"), is(false));
    assertThat(Domains.supportsDomain(Set.of("esample.com"), "mail.e\u017fample.com"), is(false));
    assertThat(
        Domains.supportsDomain(Set.of("xn--kbcher-4ya.de"), "\u212ab\u00fccher.de"), is(false));
    assertThat(Domains.supportsDomain(Set.of("ss.com"), "\u00df.com"), is(false));
    assertThat(Domains.supportsDomain(Set.of("example.com"), "example_.com"), is(false));
    assertThat(Domains.supportsDomain(Set.of("example.com"), "example%.com"), is(false));
    assertThat(Domains.supportsDomain(Set.of("example.com"), "a..example.com"), is(false));
    assertThat(Domains.supportsDomain(Set.of("."), "."), is(false));
  }
}
