package com.springbackend.springBackend.mapper;

import com.springbackend.springBackend.dto.UserDTO;
import com.springbackend.springBackend.model.User;

public class UserMapper {

    /**
     * Convierte un objeto UserDTO a un objeto User.
     *
     * @param userDTO El objeto UserDTO.
     * @return El objeto User.
     */
    public static User toEntity(UserDTO userDTO) {
        return User.builder()
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .password(userDTO.getPassword()) // La contraseña será codificada en el servicio
                .enabled(true) // Por defecto, el usuario estará habilitado
                .build();
    }
}