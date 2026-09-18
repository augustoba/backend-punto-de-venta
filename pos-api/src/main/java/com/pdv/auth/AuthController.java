package com.pdv.auth;

import com.pdv.common.BusinessException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService service;
    private final TokenService tokens;

    public AuthController(AuthService service, TokenService tokens) { this.service = service; this.tokens = tokens; }

    public record LoginIn(String username, String password) {}
    public record PasswordIn(String current, String next) {}
    public record UserIn(String username, String password, AppUser.Role role) {}
    public record ActiveIn(boolean active) {}

    @PostMapping("/auth/login") public AuthService.Session login(@RequestBody LoginIn in) { return service.login(in.username(), in.password()); }

    @PutMapping("/auth/password")
    public void password(@RequestHeader("Authorization") String auth, @RequestBody PasswordIn in) {
        service.changePassword(claims(auth).userId(), in.current(), in.next());
    }

    @GetMapping("/users") public List<AppUser> users() { adminOnly(); return service.list(); }
    @PostMapping("/users") public AppUser create(@RequestBody UserIn in) { adminOnly(); return service.create(in.username(), in.password(), in.role()); }
    @PutMapping("/users/{id}/active") public AppUser active(@PathVariable Long id, @RequestBody ActiveIn in) { adminOnly(); return service.setActive(id, in.active()); }

    private TokenService.Claims claims(String auth) {
        return tokens.verify(auth.replaceFirst("^Bearer ", "")).orElseThrow(() -> new BusinessException("Iniciá sesión"));
    }
    private void adminOnly() { if (!CurrentUser.isAdmin()) throw new BusinessException("Sólo un administrador puede gestionar usuarios"); }
}
