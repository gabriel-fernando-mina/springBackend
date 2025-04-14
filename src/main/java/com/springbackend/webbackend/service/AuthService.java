package com.springbackend.webbackend.service;

import com.springbackend.webbackend.dto.AuthRequest;
import com.springbackend.webbackend.dto.MfaRequest;
import com.springbackend.webbackend.dto.UserDTO;
import com.springbackend.webbackend.model.User;
import com.springbackend.webbackend.repository.UserRepository;
import com.springbackend.webbackend.util.JwtCookieUtil;
import io.jsonwebtoken.security.InvalidKeyException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtCookieUtil jwtCookieUtil;
    private final RevokedTokenService revokedTokenService;
    private final MfaService mfaService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Autentica a un usuario y devuelve un token JWT.
     */
    public String authenticate(AuthRequest authRequest) {
        // Buscar al usuario por email o username
        User user = userService.findByEmailOrUsername(authRequest.getEmailOrUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar la contraseña
        if (!passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        // Verificar si el usuario tiene MFA habilitado
        boolean mfaEnabled = user.getMfaSecret() != null && !user.getMfaSecret().isEmpty();

        // Generar el token JWT
        return jwtService.generateToken(user, mfaEnabled);
    }

    /*
     * Verifica un código MFA antes de permitir el acceso.
     */
    /**
     * Verifica el código MFA de un usuario.
     *
     * @param username El nombre de usuario.
     * @param mfaCode El código MFA proporcionado.
     * @return true si el código MFA es válido, false de lo contrario.
     */
    public boolean verifyMfaCode(String username, String mfaCode) {
        // Buscar usuario por username
        User user = userService.findUserByUsername(username);

        // Validar el código MFA
        return mfaService.verifyCode(user.getMfaSecret(), mfaCode);
    }

    /**
     * Agrega el token JWT a la respuesta HTTP.
     */
    public void addJwtToResponse(HttpServletResponse response, String token) {
        jwtCookieUtil.createCookie(response, token);
    }

    /**
     * Registra un nuevo usuario en base a un DTO
     */
    public User registerUser(UserDTO userDTO) {
        return userService.registerUser(userDTO);
    }

    public String verifyMfaCodeAndGenerateToken(MfaRequest mfaRequest) {
        // Buscar al usuario por username
        User user = userService.findUserByUsername(mfaRequest.getUsername());

        // Validar el código MFA
        boolean isMfaValid = mfaService.verifyCode(user.getMfaSecret(), mfaRequest.getMfaCode());
        if (!isMfaValid) {
            throw new RuntimeException("Código MFA incorrecto");
        }

        // Generar un nuevo token JWT con MFA validado
        return jwtService.generateToken(user, false); // Ahora MFA está validado
    }
}