package com.pdv.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/** Token firmado con HMAC-SHA256: base64url("id|usuario|rol|vence") + "." + firma. Sin estado en el servidor. */
@Service
public class TokenService {
    public record Claims(Long userId, String username, AppUser.Role role) {}

    private final byte[] secret;
    private final long ttlSeconds;

    public TokenService(@Value("${pos.auth.secret}") String secret, @Value("${pos.auth.ttl-hours:12}") long ttlHours) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlSeconds = ttlHours * 3600;
    }

    public String issue(AppUser u) { return issue(u, Instant.now().getEpochSecond() + ttlSeconds); }

    String issue(AppUser u, long expiresAt) {
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString((u.getId() + "|" + u.getUsername() + "|" + u.getRole() + "|" + expiresAt).getBytes(StandardCharsets.UTF_8));
        return payload + "." + sign(payload);
    }

    /** Devuelve los datos del token si la firma es válida y no venció. */
    public Optional<Claims> verify(String token) {
        try {
            int dot = token.indexOf('.');
            if (dot < 0) return Optional.empty();
            String payload = token.substring(0, dot);
            if (!MessageDigest.isEqual(sign(payload).getBytes(StandardCharsets.UTF_8), token.substring(dot + 1).getBytes(StandardCharsets.UTF_8))) return Optional.empty();
            String[] p = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8).split("\\|");
            if (Long.parseLong(p[3]) < Instant.now().getEpochSecond()) return Optional.empty();
            return Optional.of(new Claims(Long.valueOf(p[0]), p[1], AppUser.Role.valueOf(p[2])));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
