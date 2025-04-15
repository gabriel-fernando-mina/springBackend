package com.springbackend.springBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para encapsular la respuesta de autenticación con tokens.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;  // Nuevo token de acceso (JWT)
    private String refreshToken; // Refresh token actual
    private String message;      // Mensaje adicional (por ejemplo, "MFA requerido")

    /**
     * Constructor para solo el token de acceso.
     */
    public AuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}

