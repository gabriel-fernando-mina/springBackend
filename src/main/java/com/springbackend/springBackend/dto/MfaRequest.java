package com.springbackend.springBackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para solicitud de validación de MFA.
 */
@Data
public class MfaRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio.")
    private String username;

    @NotBlank(message = "El código MFA es obligatorio.")
    private String mfaCode;
}