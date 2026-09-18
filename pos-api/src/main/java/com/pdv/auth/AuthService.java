package com.pdv.auth;

import com.pdv.common.BusinessException;
import com.pdv.common.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Usuarios, inicio de sesión y cambio de contraseña. Al primer arranque crea el usuario `admin`. */
@Service
@Transactional
public class AuthService implements ApplicationRunner {
    private final UserRepository users;
    private final TokenService tokens;
    private final String initialPassword;

    public AuthService(UserRepository users, TokenService tokens, @Value("${pos.auth.initial-password:admin}") String initialPassword) {
        this.users = users; this.tokens = tokens; this.initialPassword = initialPassword;
    }

    /** Sólo si no hay ningún usuario: crea `admin` con la contraseña inicial (hay que cambiarla). */
    @Override
    public void run(ApplicationArguments args) {
        if (users.count() == 0) users.save(new AppUser("admin", PasswordHasher.hash(initialPassword), AppUser.Role.ADMIN));
    }

    public record Session(String token, AppUser user) {}

    public Session login(String username, String password) {
        AppUser u = users.findByUsernameIgnoreCase(username == null ? "" : username.trim()).orElse(null);
        if (u == null || !u.isActive() || password == null || !PasswordHasher.matches(password, u.getPasswordHash())) throw new BusinessException("Usuario o contraseña incorrectos");
        return new Session(tokens.issue(u), u);
    }

    public List<AppUser> list() { return users.findAll(); }

    public AppUser create(String username, String password, AppUser.Role role) {
        if (username == null || username.isBlank()) throw new BusinessException("El usuario necesita un nombre");
        validPassword(password);
        if (users.findByUsernameIgnoreCase(username.trim()).isPresent()) throw new BusinessException("Ya existe un usuario con ese nombre");
        return users.save(new AppUser(username.trim(), PasswordHasher.hash(password), role == null ? AppUser.Role.VENDEDOR : role));
    }

    public void changePassword(Long userId, String current, String next) {
        AppUser u = users.findById(userId).orElseThrow(() -> new NotFoundException("Usuario", userId));
        if (current == null || !PasswordHasher.matches(current, u.getPasswordHash())) throw new BusinessException("La contraseña actual no es correcta");
        validPassword(next);
        u.setPasswordHash(PasswordHasher.hash(next));
    }

    public AppUser setActive(Long id, boolean active) {
        AppUser u = users.findById(id).orElseThrow(() -> new NotFoundException("Usuario", id));
        if (!active && u.getRole() == AppUser.Role.ADMIN && users.findAll().stream().filter(x -> x.isActive() && x.getRole() == AppUser.Role.ADMIN).count() <= 1)
            throw new BusinessException("Tiene que quedar al menos un administrador activo");
        u.setActive(active);
        return u;
    }

    private void validPassword(String p) { if (p == null || p.length() < 6) throw new BusinessException("La contraseña debe tener al menos 6 caracteres"); }
}
