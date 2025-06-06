package com.springbackend.springBackend.repository;

import com.springbackend.springBackend.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByTokenId(String tokenId); // Devuelve un Optional
}
