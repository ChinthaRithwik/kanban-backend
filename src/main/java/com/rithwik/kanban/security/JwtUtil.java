package com.rithwik.kanban.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // Injected from application.properties: jwt.secret
    @Value("${jwt.secret}")
    private String secret;

    // Injected from application.properties: jwt.expiration
    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Generate a JWT that contains BOTH:
     *   "id"  → the numeric user ID  (required by frontend isAdmin check)
     *   "sub" → the user's email      (used by JwtAuthenticationFilter)
     *
     * Payload contract:
     *   { "sub": "<email>", "id": <number>, "iat": ..., "exp": ... }
     */
    public String generateToken(Long id, String email) {
        return Jwts.builder()
                .setSubject(email)          // "sub" claim = email
                .claim("id", id)            // "id" claim  = numeric user id
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key())
                .compact();
    }

    /**
     * Extract the email address from the JWT subject ("sub" claim).
     * Used by JwtAuthenticationFilter to load UserDetails.
     */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extract the numeric user ID from the "id" claim.
     * Returned as Long; safe to cast because we always write it as Long.
     */
    public Long extractId(String token) {
        Object id = parseClaims(token).get("id");
        if (id instanceof Integer) {
            return ((Integer) id).longValue();
        }
        return (Long) id;
    }

    /**
     * Token is valid when the email in the token matches the loaded UserDetails
     * and the token is not yet expired (JJWT throws ExpiredJwtException automatically).
     */
    public boolean isTokenValid(String token, String email) {
        return extractEmail(token).equals(email);
    }

    // ── private helpers ──────────────────────────────────────────────

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
