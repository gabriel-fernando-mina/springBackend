package com.springbackend.webbackend.service;

import com.springbackend.webbackend.model.Token;
import com.springbackend.webbackend.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    /**
     * Guarda un nuevo token en la base de datos.
     * @param token El token JWT a guardar.
     * @param username El nombre de usuario asociado con el token.
     * @param isRefreshToken Indica si el token es un token de actualización.
     */
    public void saveToken(String token, String username, boolean isRefreshToken) {
        if (token == null || username == null) {
            throw new IllegalArgumentException("Token and username must not be null");
        }

        Token newToken = Token.builder()
                .token(token)
                .username(username)
                .revoked(false)
                .refreshToken(isRefreshToken)
                .createdAt(Instant.now())
                .build();

        try {
            tokenRepository.save(newToken);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save token", e);
        }
    }

    /**
     * Genera un nuevo token y lo guarda en la base de datos.
     * @param username El nombre de usuario asociado con el token.
     * @param isRefreshToken Indica si el token es un token de actualización.
     * @return El token JWT generado.
     */
    public String generateAndSaveToken(String username, boolean isRefreshToken) {
        if (username == null) {
            throw new IllegalArgumentException("Username must not be null");
        }

        // Generar un nuevo token (puedes usar tu lógica de generación de JWT aquí)
        String token = UUID.randomUUID().toString();

        // Guardar el token en la base de datos
        saveToken(token, username, isRefreshToken);

        return token;
    }
}