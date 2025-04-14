package com.springbackend.springBackend.controller;

import com.springbackend.springBackend.dto.*;
import com.springbackend.springBackend.model.User;
import com.springbackend.springBackend.service.AuthService;
import com.springbackend.springBackend.service.JwtService;
import com.springbackend.springBackend.service.RevokedTokenService;
import com.springbackend.springBackend.util.JwtCookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final RevokedTokenService revokedTokenService;
    private final JwtCookieUtil jwtCookieUtil;
    private final JwtService jwtService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody UserDTO userDTO) {
        try {
            // Registra al usuario
            User user = authService.registerUser(userDTO);

            // Verificar si el usuario tiene MFA habilitado
            boolean mfaEnabled = user.getMfaSecret() != null && !user.getMfaSecret().isEmpty();

            // Generar el token JWT
            String token = jwtService.generateToken(user, mfaEnabled);

            // Devuelve el token en la respuesta
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (ResponseStatusException e) {
            logger.error("Error al registrar usuario: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(new AuthResponse(e.getReason()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        try {
            // Autenticar al usuario y generar el token
            String token = authService.authenticate(authRequest);

            // Responder con el token
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(new AuthResponse(e.getMessage()));
        }
    }

    @PostMapping("/verify-mfa")
    public ResponseEntity<AuthResponse> verifyMfa(@RequestBody MfaRequest mfaRequest) {
        try {
            // Validar el código MFA
            String token = authService.verifyMfaCodeAndGenerateToken(mfaRequest);

            // Responder con el nuevo token
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(new AuthResponse(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(@RequestParam String token, HttpServletResponse response) {
        try {
            if (revokedTokenService.isTokenRevoked(token)) {
                logger.warn("Intento de cerrar sesión con un token ya revocado");
                return ResponseEntity.status(400).body("Token ya revocado");
            }
            revokedTokenService.revokeToken(token);
            jwtCookieUtil.clearCookie(response);
            logger.info("Sesión cerrada con éxito");
            return ResponseEntity.ok("Sesión cerrada con éxito");
        } catch (ResponseStatusException e) {
            logger.error("Error durante el cierre de sesión: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
}