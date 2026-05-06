package com.tarique.bankstatement.security.jwt;

import com.tarique.bankstatement.security.service.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JwtProvider — generates and validates JWT tokens.
 *
 * <p>Uses HMAC-SHA512 signing with a base64-encoded secret key (from config).
 * Token claims carry username, email, and role so downstream services can
 * authorise without an extra DB round-trip.
 *
 * <p>Mirrors the approach in the reference my-project but upgraded to
 * jjwt 0.11.x fluent API (no deprecated methods).
 */
@Slf4j
@Component
public class JwtProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    // ------------------------------------------------------------------ //
    //  Token Generation                                                     //
    // ------------------------------------------------------------------ //

    public String generateToken(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return buildToken(principal);
    }

    public String generateTokenForUser(UserPrincipal principal) {
        return buildToken(principal);
    }

    private String buildToken(UserPrincipal principal) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", principal.getUsername());
        claims.put("email",    principal.getEmail());
        claims.put("role",     principal.getRole());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(principal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // ------------------------------------------------------------------ //
    //  Token Validation                                                     //
    // ------------------------------------------------------------------ //

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Malformed JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    // ------------------------------------------------------------------ //
    //  Claims Extraction                                                    //
    // ------------------------------------------------------------------ //

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
