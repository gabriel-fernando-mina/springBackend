package com.springbackend.webbackend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String SECRET_KEY = "TuClaveSecretaSuperSeguraDeAlMenos32Caracteres"; // Usa una más segura en prod
    @Value("${jwt.expiration}")

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes()); // ✅ Seguridad mejorada
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername()) // En lugar de setSubject()
                .claims(extraClaims) // En lugar de setClaims()
                .issuedAt(new Date(System.currentTimeMillis())) // En lugar de setIssuedAt()
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 horas
                .signWith(getSigningKey(), Jwts.SIG.HS256) // Nueva forma de firmar el token
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser() // 🔹 En JJWT 0.12.6 se usa `Jwts.parser()`
                .verifyWith(getSigningKey()) // metodo `verifyWith()` en lugar de `setSigningKey()`
                .build()
                .parseSignedClaims(token) // En lugar de `parseClaimsJws(token)`
                .getPayload();
    }
}
