package com.example.jwt.security;

import com.example.jwt.domain.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.access.secret}")
    private String accessSecret;

    @Value("${jwt.refresh.secret}")
    private String refreshSecret;

    @Value("${jwt.access.ttl}")
    private long accessTtlMs;

    @Value("${jwt.refresh.ttl}")
    private long refreshTtlMs;

    private Key accessKey;
    private Key refreshKey;

    @PostConstruct
    public void init() {
        accessKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
        refreshKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        System.out.println("userId: " + user.getId());
        System.out.println("email: " + user.getEmail());
        System.out.println("role: " + user.getRole());
        System.out.println("passwordHash: " + user.getPasswordHash());
        System.out.println("provider: " + user.getProvider());
        System.out.println("providerId: " + user.getProviderId());
        System.out.println("tokenVersion: " + user.getTokenVersion());

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole());
        claims.put("sub", user.getId());

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTtlMs))
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user, String tokenId) {
        return Jwts.builder()
            .setSubject(user.getId().toString()) // sub
            .setId(tokenId)                     // jti
            .claim("email", user.getEmail())
            .claim("role", user.getRole())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + refreshTtlMs))
            .signWith(refreshKey, SignatureAlgorithm.HS256)
            .compact();
    }

    public Claims validateAccessToken(String token) throws JwtException {
        return Jwts.parserBuilder().setSigningKey(accessKey).build().parseClaimsJws(token).getBody();
    }

    public Claims validateRefreshToken(String token) throws JwtException {
        return Jwts.parserBuilder().setSigningKey(refreshKey).build().parseClaimsJws(token).getBody();
    }
}
