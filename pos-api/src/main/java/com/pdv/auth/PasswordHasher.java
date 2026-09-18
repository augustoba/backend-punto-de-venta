package com.pdv.auth;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Hash de contraseñas con PBKDF2-HMAC-SHA256 (sólo JDK). Formato: iteraciones$sal$hash en Base64. */
public final class PasswordHasher {
    private static final int ITERATIONS = 120_000;
    private static final SecureRandom RANDOM = new SecureRandom();
    private PasswordHasher() {}

    public static String hash(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS));
    }

    public static boolean matches(String password, String stored) {
        try {
            String[] p = stored.split("\\$");
            byte[] expected = Base64.getDecoder().decode(p[2]);
            return MessageDigest.isEqual(expected, derive(password, Base64.getDecoder().decode(p[1]), Integer.parseInt(p[0])));
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(new PBEKeySpec(password.toCharArray(), salt, iterations, 256)).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
