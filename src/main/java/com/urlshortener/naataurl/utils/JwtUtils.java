package com.urlshortener.naataurl.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.urlshortener.naataurl.persistence.model.User;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Utility
 * This is the utility for required JWT functions.
 */
@Component
public class JwtUtils {

    @Value("${spring.jwt.secret}")
    private String jwtSecret;

    private Key getSigningKey() throws RuntimeException {
        try {
            if (jwtSecret == null || jwtSecret.isEmpty()) {
                throw new IllegalStateException("JWT secret is not set");
            }
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error getting signing key", e);
        }
    }

    public String extractEmail(String token) throws RuntimeException {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error extracting username from token", e);
        }
    }

    public Date extractExpiration(String token) throws RuntimeException {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error extracting expiration from token", e);
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) throws RuntimeException {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error extracting claims from token", e);
        }
    }

    private Claims extractAllClaims(String token) throws RuntimeException {
        try {
            return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
        } catch (RuntimeException e) {
            throw new RuntimeException("Error extracting all claims from token", e);
        }
    }

    private Boolean isTokenExpired(String token) throws RuntimeException {
        try {
            return extractExpiration(token).before(new Date());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error checking if token is expired", e);
        }
    }

    public String generateToken(User user) throws RuntimeException {
        try {
            Map<String, Object> claims = new HashMap<>();
            Long userId = user.getUserId();
            String userEmail = user.getUserEmail();
            String userName = user.getUserName();
            claims.put("userId", userId);
            claims.put("email", userEmail);
            claims.put("userName", userName);
            return createToken(claims, userEmail);
        } catch (Exception e) {
            throw new RuntimeException("Error generating token", e);
        }
    }

    private String createToken(Map<String, Object> claims, String subject) throws RuntimeException {
        try {
            String token = Jwts.builder().setClaims(claims).setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
            return token;
        } catch (Exception e) {
            throw new RuntimeException("Error generating token", e);
        }
    }

    // Add a method to extract userId from token
    public Long extractUserId(String token) throws RuntimeException {
        try {
            return extractClaim(token, claims -> claims.get("userId", Long.class));
        } catch (RuntimeException e) {
            throw new RuntimeException("Error extracting userId from token", e);
        }
    }

    public Boolean validateToken(String token, User user) throws RuntimeException {
        try {
            final String email = extractEmail(token);
            return (email.equals(user.getUserEmail()) && !isTokenExpired(token));
        } catch (RuntimeException e) {
            throw new RuntimeException("Error validating token", e);
        }
    }
}