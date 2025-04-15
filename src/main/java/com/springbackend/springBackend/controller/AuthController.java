package com.springbackend.springBackend.controller;

import com.springbackend.springBackend.dto.*;
import com.springbackend.springBackend.exception.InvalidMfaCodeException;
import com.springbackend.springBackend.model.User;
import com.springbackend.springBackend.service.AuthService;
import com.springbackend.springBackend.service.JwtService;
import com.springbackend.springBackend.service.TokenService;
import com.springbackend.springBackend.service.UserService;
import com.springbackend.springBackend.util.JwtCookieUtil;
import com.springbackend.springBackend.util.PasswordValidator;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;


/**
 * Controlador para manejar operaciones de autenticación.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final JwtCookieUtil jwtCookieUtil;
    private final TokenService tokenService;
    private final JwtService jwtService;
    private final UserService userService;


    /**
     * Endpoint para registrar un nuevo usuario.
     *
     * @param userDTO Objeto con datos del usuario.
     * @return Respuesta con el usuario registrado.
     */
    @PostMapping("/register")
    public ResponseEntity<Object> registerUser(@Valid @RequestBody UserDTO userDTO) {
        System.out.println("Entrando al método registerUser...");
        try {
            System.out.println("Datos recibidos: {}"+ userDTO.toString());

            // Validar que password y confirmPassword coincidan
            if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
                System.out.println(("Las contraseñas no coinciden."));
                return ResponseEntity.badRequest().body("Las contraseñas no coinciden.");
            }

            // Validar la fortaleza de la contraseña
            if (!PasswordValidator.isValid(userDTO.getPassword())) {
                System.out.println("Contraseña débil.");
                return ResponseEntity.badRequest().body(PasswordValidator.getValidationMessage(userDTO.getPassword()));
            }

            // Registrar al usuario
            User registeredUser = userService.registerUser(userDTO);
            System.out.println("Usuario registrado con éxito: {}"+ registeredUser.getUsername());
            return ResponseEntity.ok("Usuario registrado con éxito: " + registeredUser.getUsername());
        } catch (IllegalArgumentException e) {
            System.out.println("Error de validación: {}"+ e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error inesperado: {}"+ e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al registrar el usuario.");
        }
    }

    /**
     * Endpoint para iniciar sesión.
     *
     * @param authRequest Objeto con credenciales del usuario.
     * @return Respuesta con tokens de acceso y refresco.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            // Autenticar al usuario y generar tokens
            AuthResponse authResponse = authService.authenticateUserAndGenerateTokens(authRequest);

            // Responder con los tokens
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            System.out.println(("Intento de inicio de sesión fallido para: {}"+ authRequest.getUsernameOrEmail()));
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                    .body(new AuthResponse(null, null, e.getMessage()));
        }
    }

    /**
     * Endpoint para verificar el código MFA y generar tokens.
     *
     * @param mfaRequest Objeto con datos de la solicitud MFA.
     * @return Respuesta con los tokens generados.
     */
    @PostMapping("/verify-mfa")
    public ResponseEntity<AuthResponse> verifyMfa(@Valid @RequestBody MfaRequest mfaRequest) {
        try {
            // Validar MFA y generar tokens
            AuthResponse authResponse = authService.verifyMfaAndGenerateTokens(mfaRequest);

            // Responder con los nuevos tokens
            return ResponseEntity.ok(authResponse);
        } catch (InvalidMfaCodeException e) {
            System.out.println(("Código MFA inválido para usuario: {}"+mfaRequest.getUsername()));
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                    .body(new AuthResponse(null, null, "Código MFA inválido."));
        } catch (ResponseStatusException e) {
            System.out.println(("Error de autenticación MFA: {}"+ e.getReason()));
            return ResponseEntity.status(e.getStatusCode())
                    .body(new AuthResponse(null, null, e.getReason()));
        }
    }

    /**
     * Endpoint para cerrar sesión.
     *
     * @param token Token JWT del usuario.
     * @param response Objeto HTTPResponse para manejar cookies.
     * @return Mensaje de éxito o error.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(@RequestParam String token, HttpServletResponse response) {
        try {
            // Revocar el token y eliminar la cookie
            tokenService.revokeToken(token);
            jwtCookieUtil.clearCookie(response);
            System.out.println("Sesión cerrada con éxito");
            return ResponseEntity.ok("Sesión cerrada con éxito");
        } catch (ResponseStatusException e) {
            System.out.println("Error durante el cierre de sesión: {}"+ e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @PostMapping("/verify-token")
    public ResponseEntity<AuthResponse> verifyToken(@RequestParam String token) {
        try {
            if (jwtService.isTokenExpired(token)) {
                throw new RuntimeException("El token ha expirado");
            }

            String username = jwtService.getUsernameFromToken(token);
            return ResponseEntity.ok(new AuthResponse("Token válido para: " + username));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(null, null, e.getMessage()));
        }
    }
}