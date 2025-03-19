package com.springbackend.webbackend.service;

import com.springbackend.webbackend.dto.UserDTO;
import com.springbackend.webbackend.mapper.UserMapper;
import com.springbackend.webbackend.model.User;
import com.springbackend.webbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(UserDTO userDTO) {
        userRepository.findByUsername(userDTO.getUsername()).ifPresent(user -> {
            throw new RuntimeException("El usuario ya existe");
        });

        User user = UserMapper.fromDTO(userDTO, passwordEncoder);
        return userRepository.save(user);
    }

    public String authenticate(String emailOrUsername, String password) {
        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(emailOrUsername, password) // Usamos emailOrUsername directamente
            );
        } catch (Exception e) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        User user = userRepository.findByUsername(emailOrUsername)
                .or(() -> userRepository.findByEmail(emailOrUsername))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Claims personalizados
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());

        return jwtService.generateToken(claims, user);
    }

}

