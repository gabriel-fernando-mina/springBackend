package com.springbackend.webbackend.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MFARequest {
    private String username;  // Nombre de usuario (o email) para verificar el MFA
    private String code;         // Código MFA que el usuario ingresa
}
