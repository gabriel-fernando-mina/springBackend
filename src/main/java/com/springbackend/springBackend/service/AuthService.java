package com.springbackend.springBackend.service;

import com.springbackend.springBackend.dto.*;
import com.springbackend.springBackend.exception.InvalidMfaCodeException;
import com.springbackend.springBackend.model.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final TokenService tokenService;
    private final MfaService mfaService; // Integración de MfaService
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /**
     * Autentica al usuario y genera tokens JWT de acceso y de refresco.
     *
     * @param authRequest Datos de autenticación proporcionados por el usuario.
     * @return Respuesta con los tokens generados y un mensaje de éxito.
     */
    public AuthResponse authenticateUserAndGenerateTokens(AuthRequest authRequest) {
        try {
            // Autenticar al usuario
            User user = userService.authenticate(authRequest.getUsernameOrEmail(), authRequest.getPassword());

            // Verificar si MFA está habilitado
            if (user.getMfaSecret() != null && !user.getMfaSecret().isEmpty()) {
                logger.info("El usuario {} tiene MFA habilitado. Requiere verificación adicional.", user.getUsername());
                return new AuthResponse(null, null, "Se requiere MFA para completar la autenticación.");
            }

            // Generar tokens JWT usando TokenService
            String accessToken = tokenService.generateToken(user.getUsername(), user.getRole(), user.getMfaSecret() != null);
            String refreshToken = tokenService.createRefreshToken(user);

            logger.info("Autenticación exitosa para el usuario: {}", user.getUsername());
            return new AuthResponse(accessToken, refreshToken, "Inicio de sesión exitoso");
        } catch (ResponseStatusException e) {
            logger.warn("Intento de inicio de sesión fallido para: {}", authRequest.getUsernameOrEmail());
            throw e; // Re-lanzar la excepción para que el controlador la maneje.
        } catch (Exception e) {
            logger.error("Error inesperado durante el inicio de sesión: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error inesperado durante el inicio de sesión");
        }
    }

    /**
     * Verifica el código MFA y genera tokens JWT de acceso y de refresco.
     *
     * @param mfaRequest Datos de la solicitud de verificación MFA.
     * @return Respuesta con los tokens generados y un mensaje de éxito.
     * @throws InvalidMfaCodeException Si el código MFA no es válido.
     */
    public AuthResponse verifyMfaAndGenerateTokens(MfaRequest mfaRequest) {
        // Verificar el código MFA
        boolean isValid = mfaService.verifyCode(mfaRequest.getUsername(), mfaRequest.getMfaCode());
        if (!isValid) {
            logger.warn("Código MFA inválido para el usuario: {}", mfaRequest.getUsername());
            throw new InvalidMfaCodeException("Código MFA inválido.");
        }

        // Obtener el usuario mediante UserService
        User user = userService.findByUsernameOrEmail(mfaRequest.getUsername());

        // Generar tokens
        String accessToken = tokenService.generateToken(user.getUsername(), user.getRole(), user.getMfaSecret() != null);
        String refreshToken = tokenService.createRefreshToken(user);

        logger.info("Autenticación MFA exitosa para el usuario: {}", mfaRequest.getUsername());

        return new AuthResponse(accessToken, refreshToken, "Autenticación MFA exitosa");
    }
}