package com.springbackend.springBackend.util;

import io.jsonwebtoken.security.Keys;
import java.util.Base64;

public class JwtKeyGenerator {
    public static void main(String[] args) {
        // Generar una clave segura para HS512
        byte[] keyBytes = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512).getEncoded();
        String base64Key = Base64.getEncoder().encodeToString(keyBytes);

        // Imprimir la clave generada
        System.out.println("Clave secreta segura (Base64): " + base64Key);
    }
}
