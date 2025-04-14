package com.springbackend.springBackend.service;

import com.springbackend.springBackend.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.internal.Function;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey); // Decodificar Base64
        return Keys.hmacShaKeyFor(decodedKey);
    }

    public String generateToken(User user, boolean mfaEnabled) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole());
        claims.put("mfaValidated", !mfaEnabled);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Corrección aquí
                .compact();
    }

    public Boolean validateToken(String token, String username, String expectedRole) {
        final String extractedUsername = extractUsername(token);
        final String extractedRole = extractRole(token);

        if (extractedRole == null) {
            throw new IllegalArgumentException("Role is missing from the token");
        }

        return (extractedUsername.equals(username)
                && extractedRole.equals(expectedRole)
                && !isTokenExpired(token));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> {
            String role = claims.get("role", String.class);
            if (role == null) {
                throw new IllegalArgumentException("Role claim is missing in the token");
            }
            return role;
        });
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
        return claimsResolver.apply(claims);
    }
}