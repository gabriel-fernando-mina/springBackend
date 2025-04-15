package com.springbackend.springBackend.exception;

/**
 * Excepción personalizada para códigos MFA inválidos.
 */
public class InvalidMfaCodeException extends RuntimeException {
    public InvalidMfaCodeException(String message) {
        super(message);
    }
}
