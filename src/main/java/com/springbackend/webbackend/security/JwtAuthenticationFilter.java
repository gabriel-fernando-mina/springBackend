package com.springbackend.webbackend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springbackend.webbackend.service.JwtService;
import com.springbackend.webbackend.service.RevokedTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private final RevokedTokenService revokedTokenService;

    @Autowired
    @Lazy
    private final JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final List<String> excludedPaths = List.of("/api/auth/register", "/api/auth/login", "/api/auth/verify-mfa");

    public JwtAuthenticationFilter(JwtService jwtService, RevokedTokenService revokedTokenService) {
        this.jwtService = jwtService;
        this.revokedTokenService = revokedTokenService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getServletPath();
        if (excludedPaths.contains(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");
        String token = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }

        try {
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (revokedTokenService.isTokenRevoked(token)) {
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token has been revoked");
                    return;
                }

                String username = jwtService.extractUsername(token);
                String role = jwtService.extractRole(token);

                if (jwtService.validateToken(token, username)) {
                    User userDetails = new User(username, "", List.of(() -> role));
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to authenticate user with token", e);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Failed to authenticate token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(Map.of("error", message)));
    }
}