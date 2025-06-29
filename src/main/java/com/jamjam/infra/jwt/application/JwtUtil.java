package com.jamjam.infra.jwt.application;

import com.jamjam.global.exception.ApiException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static io.jsonwebtoken.Jwts.claims;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final JwtParser verifier;

    public JwtUtil(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        this.verifier = Jwts.parser()
                .verifyWith(secretKey)
                .build();
    }

    public Long getUserIdFromToken(String token) {
        Number userId = (Number) Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userId");
        return userId.longValue();
    }

    public String getUserRoleFromToken(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role").toString();
    }

    public boolean isTokenExpired(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date(System.currentTimeMillis()));
    }

    public String getType(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("type").toString();
    }

    public LocalDateTime getExpiration(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration()
                .toInstant().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime();
    }

    public String generateToken(String type,Long userId, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("type",type)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Claims c = claims(token);
            
            if (c.getExpiration().before(new Date())) return false;
            if (!"access".equals(c.get("type", String.class))) return false;

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    private Claims claims(String token) {
        return verifier.parseSignedClaims(token).getPayload();
    }
}
