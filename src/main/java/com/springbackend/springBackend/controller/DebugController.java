package com.springbackend.springBackend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/roles")
    public ResponseEntity<?> debugRoles(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("No estás autenticado.");
        }
        return ResponseEntity.ok(authentication.getAuthorities());
    }
}