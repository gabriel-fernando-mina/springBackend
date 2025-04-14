package com.springbackend.springBackend.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.stereotype.Service;

@Service
public class MfaService {

    private final GoogleAuthenticator googleAuthenticator;

    public MfaService() {
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setTimeStepSizeInMillis(30000) // Tiempo de expiración del código
                .setWindowSize(5) // Número de intentos permitidos
                .build();
        this.googleAuthenticator = new GoogleAuthenticator(config);
    }

    // Generar la clave secreta para un usuario
    public String generateSecret() {
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        return key.getKey();
    }

    // Verificar el código TOTP
    public boolean verifyCode(String secret, String code) {
        return googleAuthenticator.authorize(secret, Integer.parseInt(code)); // Asumiendo que la API de googleAuthenticator usa un int
    }

    /**
     * Genera una URL OTP para configurar el código QR en una aplicación de autenticación.
     * @param username El nombre de usuario para el que se genera el código QR.
     * @param secret La clave secreta generada para el usuario.
     * @return La URL OTP que puede ser utilizada para generar un código QR.
     */
    public String generateQRUrl(String username, String secret) {
        // La URL QR sigue este formato:
        // "otpauth://totp/{issuer}:{username}?secret={secret}&issuer={issuer}"
        String issuer = "MyApp";  // Nombre de tu aplicación o servicio
        return "otpauth://totp/" + issuer + ":" + username + "?secret=" + secret + "&issuer=" + issuer;
    }
}
