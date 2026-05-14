package io.phasetwo.service.util;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

/**
 * Utility for encoding cleartext strings into the argon2id PHC string format.
 *
 * <p>Format: {@code $argon2id$v=19$m=16,t=2,p=1$<base64-salt>$<base64-hash>}
 *
 * <p>Used to hash SCIM shared secrets and basic auth passwords before persisting them.
 */
public final class Argon2idEncoder {

  private static final int MEMORY_KB = 16;
  private static final int ITERATIONS = 2;
  private static final int PARALLELISM = 1;
  private static final int SALT_LENGTH = 16;
  private static final int HASH_LENGTH = 16;
  private static final int VERSION = Argon2Parameters.ARGON2_VERSION_13;

  private static final String PHC_PREFIX = "$argon2";

  private static final Base64.Encoder ENCODER = Base64.getEncoder().withoutPadding();
  private static final SecureRandom RANDOM = new SecureRandom();

  private Argon2idEncoder() {}

  /**
   * Returns true if the given value already looks like a PHC-formatted argon2 string.
   */
  public static boolean isAlreadyHashed(String value) {
    return value != null && value.startsWith(PHC_PREFIX);
  }

  /**
   * Encodes the given cleartext value into a PHC-formatted argon2id hash.
   * Returns the input unchanged if it already looks like a PHC string.
   */
  public static String encode(String cleartext) {
    if (cleartext == null || cleartext.isEmpty() || isAlreadyHashed(cleartext)) {
      return cleartext;
    }

    byte[] salt = new byte[SALT_LENGTH];
    RANDOM.nextBytes(salt);

    Argon2Parameters params =
        new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(VERSION)
            .withMemoryAsKB(MEMORY_KB)
            .withIterations(ITERATIONS)
            .withParallelism(PARALLELISM)
            .withSalt(salt)
            .build();

    Argon2BytesGenerator generator = new Argon2BytesGenerator();
    generator.init(params);

    byte[] hash = new byte[HASH_LENGTH];
    generator.generateBytes(cleartext.getBytes(StandardCharsets.UTF_8), hash);

    return String.format(
        "$argon2id$v=%d$m=%d,t=%d,p=%d$%s$%s",
        VERSION, MEMORY_KB, ITERATIONS, PARALLELISM,
        ENCODER.encodeToString(salt),
        ENCODER.encodeToString(hash));
  }
}
