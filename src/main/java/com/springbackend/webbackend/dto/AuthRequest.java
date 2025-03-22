package com.springbackend.webbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    private String emailOrUsername;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @Size(min = 6, max = 6, message = "El código MFA debe tener 6 caracteres")
    private String mfaCode;  // Agregado para almacenar el código MFA
}

