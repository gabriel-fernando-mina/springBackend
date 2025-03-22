package com.springbackend.webbackend.mapper;

import com.springbackend.webbackend.dto.UserDTO;
import com.springbackend.webbackend.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserMapper {

    public static User fromDTO(UserDTO userDto, PasswordEncoder passwordEncoder) {
        if (userDto.getRole() == null) {
            throw new IllegalArgumentException("El rol es obligatorio (Ejemplo: 'USER' o 'ADMIN')");
        }

        return User.builder()
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .role(userDto.getRole())
                .email(userDto.getEmail())
                .enabled(true)
                .build();
    }
}