package com.springbackend.springBackend.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class JwtCookieUtil {

    private static final String COOKIE_NAME = "JWT_TOKEN";
    private static final int COOKIE_EXPIRE_SECONDS = 3600; // 1 hora

    // Metodo para crear una cookie JWT
    public void createCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    // Metodo para limpiar la cookie JWT
    public void clearCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}