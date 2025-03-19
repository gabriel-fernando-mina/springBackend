package com.springbackend.webbackend.controller;

import com.springbackend.webbackend.dto.AuthRequest;
import com.springbackend.webbackend.dto.AuthResponse;
import com.springbackend.webbackend.dto.UserDTO;
import com.springbackend.webbackend.model.RevokedToken;
import com.springbackend.webbackend.repository.RevokedTokenRepository;
import com.springbackend.webbackend.security.JwtCookieUtil;
import com.springbackend.webbackend.service.AuthService;
import com.springbackend.webbackend.service.RevokedTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtCookieUtil jwtCookieUtil;
    private final RevokedTokenService revokedTokenService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDTO userDTO) {
        authService.registerUser(userDTO);
        return ResponseEntity.ok("Usuario registrado con éxito");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest, HttpServletResponse response) {
        String token = authService.authenticate(authRequest.getEmailOrUsername(), authRequest.getPassword());
        jwtCookieUtil.createJwtCookie(response, token);
        return ResponseEntity.ok(new AuthResponse(token)); // ✅ Enviar el token en la respuesta
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("{\"error\": \"Token inválido\"}");
        }

        String token = authorizationHeader.substring(7);
        revokedTokenService.revokeToken(token); // Revocar el token en la base de datos

        return ResponseEntity.ok("{\"message\": \"Cierre de sesión exitoso\"}");
    }
}
