package com.springbackend.webbackend.service;

import com.springbackend.webbackend.repository.RevokedTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String SECRET_KEY = "TuClaveSecretaSuperSeguraDeAlMenos32Caracteres"; // 🔥 Usa una clave fuerte en producción
    private final RevokedTokenRepository revokedTokenRepository;
    @Value("${jwt.expiration}")
    private long jwtExpirationMs; // ⬅️ Guardamos la expiración como variable de instancia

    @Autowired
    public JwtService(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes()); // ✅ Generamos la clave correctamente
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername()) // ✅ Forma moderna de definir el subject
                .claims(extraClaims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // ⬅️ Usamos la variable con @Value
                .signWith(getSigningKey(), Jwts.SIG.HS256) // ✅ Firma con clave segura
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (revokedTokenRepository.findByToken(token).isPresent()) {
            return false; // ❌ El token ha sido invalidado
        }
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }


    private boolean isTokenExpired(String token) {
        return Date.from(extractExpiration(token)).before(new Date());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser() // Si usas JJWT 0.12.6, esto debe cambiar
                .verifyWith(getSigningKey()) // 🔥 La clave debe ser `SecretKey`, no un `String`
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Instant extractExpiration(String token) {
        Claims claims = extractAllClaims(token);
        System.out.println("Claims extraídas: " + claims);
        return claims.getExpiration().toInstant();
    }

}
