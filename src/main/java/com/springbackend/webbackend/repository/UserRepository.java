package com.springbackend.webbackend.repository;

import com.springbackend.webbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username); // Busca un usuario por su nombre de usuario
    Optional<User> findByEmail(String email);
}
