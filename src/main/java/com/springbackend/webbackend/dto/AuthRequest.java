package com.springbackend.webbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthRequest {

    @NotBlank(message = "El campo email o username no puede estar vacío")
    private String emailOrUsername;

    @NotBlank(message = "El campo contraseña no puede estar vacío")
    private String password;
}