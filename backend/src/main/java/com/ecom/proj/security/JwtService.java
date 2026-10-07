package com.ecom.proj.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTtlMs;
    private final long refreshTtlMs;
    private final String issuer;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-ttl-ms}") long accessTtlMs,
            @Value("${app.jwt.refresh-token-ttl-ms}") long refreshTtlMs,
            @Value("${app.jwt.issuer}") String issuer) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlMs = accessTtlMs;
        this.refreshTtlMs = refreshTtlMs;
        this.issuer = issuer;
    }

    public String generateAccessToken(String email, String role, Long userId) {
        return buildToken(email, Map.of("role", role, "uid", userId), accessTtlMs, "access");
    }

    public String generateRefreshToken(String email) {
        return buildToken(email, Map.of(), refreshTtlMs, "refresh");
    }

    private String buildToken(String subject, Map<String, Object> extra, long ttl, String type) {
        Date now = new Date();
        return Jwts.builder()
                .issuer(issuer).subject(subject).claims(extra)
                .claim("typ", type)
                .issuedAt(now).expiration(new Date(now.getTime() + ttl))
                .signWith(key).compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).requireIssuer(issuer).build()
                .parseSignedClaims(token).getPayload();
    }

    public String extractEmail(String token) { return parse(token).getSubject(); }
    public boolean isRefreshToken(String token) { return "refresh".equals(parse(token).get("typ", String.class)); }
    public <T> T extractClaim(String token, Function<Claims, T> resolver) { return resolver.apply(parse(token)); }
}
