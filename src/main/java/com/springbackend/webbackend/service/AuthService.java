package com.springbackend.webbackend.service;

import com.springbackend.webbackend.dto.UserDTO;
import com.springbackend.webbackend.mapper.UserMapper;
import com.springbackend.webbackend.model.User;
import com.springbackend.webbackend.repository.UserRepository;
import com.springbackend.webbackend.util.JwtCookieUtil;
import com.springbackend.webbackend.util.PasswordValidator;
import io.jsonwebtoken.security.InvalidKeyException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtCookieUtil jwtCookieUtil;
    private final RevokedTokenService revokedTokenService;
    private final MFAService mfaService;
    private final UserService userService; // Usa `UserService` en lugar de `UserRepository`

    /**
     * Autentica a un usuario y devuelve un token JWT.
     */
    public String authenticate(String emailOrUsername, String password) {
        try {
            System.out.println("🔑 Intentando autenticar usuario: " + emailOrUsername);

            // Autenticar usuario con AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(emailOrUsername, password));

            System.out.println("✅ Autenticación exitosa para: " + emailOrUsername);

            // Obtener los detalles del usuario autenticado
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findUserByUsername(userDetails.getUsername());

            System.out.println("✅ Usuario encontrado: " + user.getUsername());

            // Generar token JWT
            System.out.println("🔐 Generando token JWT...");
            String token = jwtService.generateToken(userDetails);

            // Guardar el token en la lista de revocados si es necesario
            revokedTokenService.saveRevokedToken(token, user.getUsername(), false);

            System.out.println("✅ Token generado correctamente para " + user.getUsername());

            return token;
        } catch (BadCredentialsException e) {
            System.out.println("❌ Error de autenticación: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        } catch (Exception e) {
            System.out.println("❌ Error inesperado: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al autenticar usuario");
        }
    }

    /**
     * Verifica un código MFA antes de permitir el acceso.
     */
    public boolean verifyMfaCode(User user, String code) throws InvalidKeyException {
        try {
            return mfaService.verifyCode(user.getMfaSecret(), code);
        } catch (NumberFormatException e) {
            throw new InvalidKeyException("Código MFA inválido");
        }
    }

    /**
     * Agrega el token JWT a la respuesta HTTP.
     */
    public void addJwtToResponse(HttpServletResponse response, String token) {
        jwtCookieUtil.createCookie(response, token);
    }
}
