package com.springbackend.springBackend.service;

import com.springbackend.springBackend.model.RevokedToken;
import com.springbackend.springBackend.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RevokedTokenService {

    private final RevokedTokenRepository revokedTokenRepository;

    /**
     * Guarda un nuevo token revocado en la base de datos.
     * @param token El token JWT a guardar.
     * @param username El nombre de usuario asociado con el token.
     * @param isRefreshToken Indica si el token es un token de actualización.
     */
    public void saveRevokedToken(String token, String username, boolean isRefreshToken) {
        if (token == null || username == null) {
            throw new IllegalArgumentException("Token and username must not be null");
        }

        RevokedToken revokedToken = RevokedToken.builder()
                .token(token)
                .username(username)
                .revoked(true)
                .refreshToken(isRefreshToken)
                .createdAt(Instant.now())
                .revokedAt(Instant.now())
                .build();

        try {
            revokedTokenRepository.save(revokedToken);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save revoked token", e);
        }
    }

    /**
     * Marca un token como revocado en la base de datos.
     * @param token El token JWT a revocar.
     */
    public void revokeToken(String token) {
        RevokedToken revokedToken = RevokedToken.builder()
                .token(token)
                .revoked(true)
                .revokedAt(Instant.now())  // Se marca como revocado en el momento actual
                .build();
        revokedTokenRepository.save(revokedToken);
    }

    /**
     * Verifica si un token ha sido revocado.
     * @param token El token JWT a verificar.
     * @return `true` si el token ha sido revocado, `false` si no.
     */
    public boolean isTokenRevoked(String token) {
        return revokedTokenRepository.findByToken(token).isPresent();
    }
}