package io.phasetwo.service.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Argon2idEncoderTest {

  @Test
  void isAlreadyHashedRecognisesArgonPrefix() {
    assertThat(Argon2idEncoder.isAlreadyHashed("$argon2id$v=19$m=16,t=2,p=1$salt$hash"), is(true));
    assertThat(Argon2idEncoder.isAlreadyHashed("$argon2i$v=19$..."), is(true));
    assertThat(Argon2idEncoder.isAlreadyHashed("$argon2d$..."), is(true));
  }

  @Test
  void isAlreadyHashedRejectsNonArgonValues() {
    assertThat(Argon2idEncoder.isAlreadyHashed(null), is(false));
    assertThat(Argon2idEncoder.isAlreadyHashed(""), is(false));
    assertThat(Argon2idEncoder.isAlreadyHashed("plaintext"), is(false));
    assertThat(Argon2idEncoder.isAlreadyHashed("$pbkdf2-sha512$..."), is(false));
    assertThat(Argon2idEncoder.isAlreadyHashed("$2a$..."), is(false));
  }

  @Test
  void encodeReturnsNullForNullInput() {
    assertThat(Argon2idEncoder.encode(null), is(nullValue()));
  }

  @Test
  void encodeReturnsEmptyForEmptyInput() {
    assertThat(Argon2idEncoder.encode(""), is(equalTo("")));
  }

  @Test
  void encodeIsIdempotentOnAlreadyHashedInput() {
    String existing = "$argon2id$v=19$m=16,t=2,p=1$Z21uSVZmSFBxbzcycnZpdA$SJtF8lsYQ5vSysKtGBKIdg";
    assertThat(Argon2idEncoder.encode(existing), is(equalTo(existing)));
  }

  @Test
  void encodeProducesPhcStringWithExpectedHeader() {
    String hash = Argon2idEncoder.encode("hunter2");
    assertThat(hash, is(notNullValue()));
    assertThat(hash, startsWith("$argon2id$v=19$m=16,t=2,p=1$"));
  }

  @Test
  void encodedPhcStringHasFiveSegmentsAfterTheDollarPrefix() {
    String hash = Argon2idEncoder.encode("hunter2");
    // Expect: ['', 'argon2id', 'v=19', 'm=16,t=2,p=1', '<salt>', '<hash>']
    String[] parts = hash.split("\\$", -1);
    assertThat(parts.length, is(6));
    assertThat(parts[0], is(equalTo("")));
    assertThat(parts[1], is(equalTo("argon2id")));
    assertThat(parts[2], is(equalTo("v=19")));
    assertThat(parts[3], is(equalTo("m=16,t=2,p=1")));
    assertTrue(parts[4].length() > 0, "salt segment must be non-empty");
    assertTrue(parts[5].length() > 0, "hash segment must be non-empty");
  }

  @Test
  void successiveEncodesOfTheSamePlaintextProduceDifferentHashes() {
    // Different random salts → different outputs.
    String a = Argon2idEncoder.encode("hunter2");
    String b = Argon2idEncoder.encode("hunter2");
    assertThat(a, is(not(equalTo(b))));
  }

  @Test
  void differentPlaintextsProduceDifferentHashes() {
    String a = Argon2idEncoder.encode("hunter2");
    String b = Argon2idEncoder.encode("hunter3");
    assertThat(a, is(not(equalTo(b))));
  }
}
