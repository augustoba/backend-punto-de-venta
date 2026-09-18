package com.pdv.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Exige `Authorization: Bearer <token>` en /api/** (menos el login y las consultas previas CORS). */
@Component
public class AuthFilter extends OncePerRequestFilter {
    private final TokenService tokens;
    private final boolean enabled;

    public AuthFilter(TokenService tokens, @Value("${pos.auth.enabled:true}") boolean enabled) { this.tokens = tokens; this.enabled = enabled; }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String path = req.getRequestURI();
        return !enabled || "OPTIONS".equalsIgnoreCase(req.getMethod()) || !path.startsWith("/api/") || path.equals("/api/auth/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String h = req.getHeader("Authorization");
        var claims = h != null && h.startsWith("Bearer ") ? tokens.verify(h.substring(7)) : java.util.Optional.<TokenService.Claims>empty();
        if (claims.isEmpty()) {
            res.setStatus(401);
            res.setContentType("application/json;charset=UTF-8");
            res.getOutputStream().write("{\"error\":\"Iniciá sesión\"}".getBytes(StandardCharsets.UTF_8));
            return;
        }
        CurrentUser.set(claims.get());
        try { chain.doFilter(req, res); } finally { CurrentUser.clear(); }
    }
}
