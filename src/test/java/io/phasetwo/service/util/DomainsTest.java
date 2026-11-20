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
    assertThat(Domains.supportsDomain(Set.of("k.com"), "\u212a.com"), is(true));
    assertThat(Domains.supportsDomain(Set.of("k.com"), "MAIL.\u212a.com"), is(true));
  }

  @Test
  void rejectsUnrelatedDomains() {
    assertThat(Domains.supportsDomain(Set.of("example.com"), "notexample.com"), is(false));
    assertThat(
        Domains.supportsDomain(Set.of("example.com"), "example.com.attacker.com"), is(false));
    assertThat(Domains.supportsDomain(Set.of("example.com"), "mail.example.net"), is(false));
    assertThat(Domains.supportsDomain(Set.of("k.com"), "not\u212a.com"), is(false));
  }
}
