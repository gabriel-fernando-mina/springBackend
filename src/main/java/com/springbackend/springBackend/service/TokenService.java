package com.springbackend.springBackend.service;

import com.springbackend.springBackend.exception.InvalidTokenException;
import com.springbackend.springBackend.model.RoleType;
import com.springbackend.springBackend.model.Token;
import com.springbackend.springBackend.model.User;
import com.springbackend.springBackend.repository.TokenRepository;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private final TokenRepository tokenRepository;
    private final UserService userService; // Inyección de UserService

    /**
     * Genera un token JWT.
     *
     * @param username   El nombre de usuario.
     * @param role       El rol del usuario.
     * @param mfaEnabled Indica si MFA está habilitado.
     * @return El token JWT generado.
     */
    public String generateToken(String username, RoleType role, boolean mfaEnabled) {
        try {
            Date now = new Date();
            Date expirationDate = new Date(now.getTime() + jwtExpiration);

            // Decodificar la clave secreta de Base64 a bytes
            byte[] secretBytes = Base64.getDecoder().decode(jwtSecret);

            return Jwts.builder()
                    .setSubject(username)
                    .claim("role", role.name())
                    .claim("mfaEnabled", mfaEnabled)
                    .setIssuedAt(now)
                    .setExpiration(expirationDate)
                    .signWith(SignatureAlgorithm.HS512, secretBytes) // Usar la clave secreta decodificada
                    .compact();
        } catch (Exception e) {
            logger.error("Error al generar el token para el usuario {}: {}", username, e.getMessage());
            throw new RuntimeException("Error al generar el token JWT", e);
        }
    }

    /**
     * Crea un token de refresco (ejemplo básico).
     *
     * @param user El usuario para el cual se crea el token de refresco.
     * @return El token de refresco generado.
     */
    public String createRefreshToken(User user) {
        // Implementación básica para el token de refresco (similar al de acceso)
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration * 2)) // Token más largo
                .signWith(SignatureAlgorithm.HS512, Base64.getDecoder().decode(jwtSecret))
                .compact();
    }

    /**
     * Valida un token JWT.
     *
     * @param token El token JWT a validar.
     * @return true si el token es válido, false en caso contrario.
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            User user = userService.findByUsernameOrEmail(username); // Usar UserService para cargar el usuario

            return user != null && !isTokenExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Verifica si un token JWT está expirado.
     *
     * @param token El token JWT a verificar.
     * @return true si el token ha expirado, false en caso contrario.
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expirationDate = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return expirationDate.before(new Date());
        } catch (ExpiredJwtException e) {
            return true; // El token ya expiró
        } catch (JwtException e) {
            return false; // Otro error relacionado con el token
        }
    }

    /**
     * Obtiene el nombre de usuario de un token JWT.
     *
     * @param token El token JWT.
     * @return El nombre de usuario.
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Revoca un token específico.
     *
     * @param tokenId El ID del token a revocar.
     */
    public void revokeToken(String tokenId) {
        Token token = tokenRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new InvalidTokenException("Token no encontrado"));
        token.setRevoked(true);
        tokenRepository.save(token);
    }

    /**
     * Limpia los tokens expirados de la base de datos.
     * Programado para ejecutarse diariamente a las 3:00 AM.
     */
    @Scheduled(cron = "0 0 3 * * ?") // Ejecutar diariamente a las 3:00 AM
    public void cleanupExpiredRefreshTokens() {
        List<Token> expiredRefreshTokens = tokenRepository.findAll().stream()
                .filter(token -> token.isRefreshToken() && isTokenExpired(token.getTokenId()))
                .collect(Collectors.toList());

        tokenRepository.deleteAll(expiredRefreshTokens);
        logger.info("Tokens de refresco expirados eliminados: {}", expiredRefreshTokens.size());
    }
}