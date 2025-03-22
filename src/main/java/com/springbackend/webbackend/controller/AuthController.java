package com.springbackend.webbackend.controller;

import com.springbackend.webbackend.dto.AuthResponse;
import com.springbackend.webbackend.dto.LoginRequest;
import com.springbackend.webbackend.dto.UserDTO;
import com.springbackend.webbackend.model.User;
import com.springbackend.webbackend.service.AuthService;
import com.springbackend.webbackend.service.JwtService;
import com.springbackend.webbackend.service.RevokedTokenService;
import com.springbackend.webbackend.util.JwtCookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RevokedTokenService revokedTokenService;
    private final JwtCookieUtil jwtCookieUtil;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody UserDTO userDTO) {
        try {
            User user = authService.registerUser(userDTO);  // Guarda el usuario en la BD
            String token = jwtService.generateToken(user); // Genera el token JWT
            return ResponseEntity.ok(new AuthResponse(token)); // Devuelve el token en la respuesta
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new AuthResponse(e.getReason()));
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        System.out.println("Iniciando autenticación para: " + loginRequest.getEmailOrUsername());

        try {
            String token = authService.authenticate(loginRequest.getEmailOrUsername(), loginRequest.getPassword());

            System.out.println("Token generado correctamente");

            if (token != null) {
                jwtCookieUtil.createCookie(response, token);
                return ResponseEntity.ok().header("Authorization", "Bearer " + token).body("Autenticación exitosa");
            } else {
                return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body("Credenciales inválidas");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                    .body("Error interno en el servidor");
        }
    }


    @PostMapping("/verify-mfa")
    public ResponseEntity<?> verifyMfaCode(@RequestParam String username, @RequestParam String code) {
        try {
            User user = authService.findUserByUsername(username);
            if (authService.verifyMfaCode(user, code)) {
                return ResponseEntity.ok("Código MFA verificado con éxito");
            } else {
                return ResponseEntity.status(400).body("Código MFA inválido");
            }
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestParam String token, HttpServletResponse response) {
        try {
            if (revokedTokenService.isTokenRevoked(token)) {
                return ResponseEntity.status(400).body("Token ya revocado");
            }
            revokedTokenService.revokeToken(token);
            jwtCookieUtil.clearCookie(response);
            return ResponseEntity.ok("Sesión cerrada con éxito");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
}