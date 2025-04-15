package com.springbackend.springBackend.repository;

import com.springbackend.springBackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para manejar la entidad User.
 */

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Busca un usuario por nombre de usuario o correo electrónico.
     *
     * @param username El nombre de usuario.
     * @param email    El correo electrónico.
     * @return Un Optional que contiene el usuario si se encuentra.
     */
    Optional<User> findByUsernameOrEmail(String username, String email);
}