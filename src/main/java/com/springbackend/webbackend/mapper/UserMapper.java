package com.springbackend.webbackend.mapper;

import com.springbackend.webbackend.dto.UserDTO;
import com.springbackend.webbackend.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserMapper {

    public static User fromDTO(UserDTO dto, PasswordEncoder passwordEncoder) {
        if (dto.getRole() == null) {
            throw new IllegalArgumentException("El rol es obligatorio (Ejemplo: 'ROLE_USER' o 'ROLE_ADMIN')");
        }

        return User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .email(dto.getEmail())
                .enabled(true)
                .build();
    }
}
