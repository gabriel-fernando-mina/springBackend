package com.springbackend.springBackend.security;

import com.springbackend.springBackend.service.TokenService;
import com.springbackend.springBackend.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filtro personalizado que verifica el token JWT en cookies o encabezados.
 * Si el token es válido, autentica al usuario en el contexto de seguridad de Spring.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserService userService;

    private static final String TOKEN_COOKIE_NAME = "JWT_TOKEN";

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/verify-mfa"
    );

    public JwtAuthenticationFilter(TokenService tokenService, UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestPath = request.getServletPath();
        // Excluir todos los endpoints públicos
        return PUBLIC_ENDPOINTS.stream().anyMatch(requestPath::equals);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractTokenFromCookies(request);
        if (token == null) {
            token = extractTokenFromRequest(request);
        }

        if (token != null) {
            try {
                // Validar el token utilizando el servicio centralizado TokenService
                if (!tokenService.isTokenValid(token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Unauthorized: Invalid or Expired Token");
                    return;
                }

                // Extraer el nombre de usuario del token
                String username = tokenService.getUsernameFromToken(token);

                // Si el usuario no está autenticado, configurar el contexto de seguridad
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Usar el UserService personalizado para cargar los detalles del usuario
                    UserDetails userDetails = userService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // En caso de error (token inválido, expirado, etc.), limpiar el contexto de seguridad y responder con 401
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized: Invalid or Expired Token");
                return;
            }
        }

        // Continuar con la siguiente cadena de filtros
        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> TOKEN_COOKIE_NAME.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Extraer el token sin el prefijo "Bearer "
        }
        return null;
    }
}