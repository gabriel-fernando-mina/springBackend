package com.springbackend.webbackend.service;

import com.springbackend.webbackend.model.RevokedToken;
import com.springbackend.webbackend.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RevokedTokenService {

    private final RevokedTokenRepository revokedTokenRepository;

    /**
     * Guarda un token en la base de datos para marcarlo como revocado.
     * @param token Token JWT a revocar.
     */
    public void revokeToken(String token) {
        RevokedToken revokedToken = RevokedToken.builder()
                .token(token)
                .revokedAt(Instant.now()) // Se revoca en el momento actual
                .build();
        revokedTokenRepository.save(revokedToken);
    }

    /**
     * Verifica si un token ha sido revocado.
     * @param token Token JWT a verificar.
     * @return `true` si el token está revocado, `false` en caso contrario.
     */
    public boolean isTokenRevoked(String token) {
        return revokedTokenRepository.findByToken(token).isPresent();
    }
}
