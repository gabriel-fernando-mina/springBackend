package com.springbackend.webbackend.mapper;

import com.springbackend.webbackend.dto.UserDTO;
import com.springbackend.webbackend.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserMapper {

    /**
     * Convierte un UserDTO a una entidad User.
     *
     * @param userDto          El objeto UserDTO con la información del usuario.
     * @param passwordEncoder  El codificador de contraseñas para proteger el password.
     * @return                 La entidad User mapeada desde el UserDTO.
     * @throws IllegalArgumentException Si el rol es nulo o vacío.
     */
    public static User fromDTO(UserDTO userDto, PasswordEncoder passwordEncoder) {
        if (userDto.getRole() == null) {
            throw new IllegalArgumentException("El rol es obligatorio (Ejemplo: 'USER' o 'ADMIN')");
        }

        // Validación adicional para roles no reconocidos
        if (!isValidRole(userDto.getRole().toString())) {
            throw new IllegalArgumentException("Rol no reconocido: " + userDto.getRole());
        }

        return User.builder()
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .role(userDto.getRole())
                .email(userDto.getEmail())
                .enabled(true)
                .build();
    }

    /**
     * Valida si un rol es válido.
     *
     * @param role El rol a validar.
     * @return     true si el rol es válido, de lo contrario false.
     */
    private static boolean isValidRole(String role) {
        return role.equalsIgnoreCase("USER") || role.equalsIgnoreCase("ADMIN");
    }
}