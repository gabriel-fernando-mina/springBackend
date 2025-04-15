package com.springbackend.springBackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para credenciales de inicio de sesión.
 */
@Data
public class AuthRequest {

    @NotBlank(message = "El nombre de usuario o correo electrónico es obligatorio.")
    private String usernameOrEmail;

    @NotBlank(message = "La contraseña es obligatoria.")
    private String password;
}