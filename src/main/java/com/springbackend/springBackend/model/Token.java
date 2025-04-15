package com.springbackend.springBackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Token {
    @Id
    private String tokenId;
    private String username;
    private boolean revoked;
    private boolean refreshToken;
    private Instant createdAt;
    private Instant expiresAt;
}