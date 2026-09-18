package com.pdv.auth;

import com.pdv.common.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthTest {
    @Autowired AuthService auth;
    @Autowired TokenService tokens;

    @Test void laContrasenaSeGuardaConHashYSeVerifica() {
        String h = PasswordHasher.hash("secreto1");
        assertNotEquals("secreto1", h);
        assertTrue(PasswordHasher.matches("secreto1", h));
        assertFalse(PasswordHasher.matches("otra", h));
        assertNotEquals(h, PasswordHasher.hash("secreto1"), "cada hash lleva su propia sal");
    }

    @Test void alPrimerArranqueExisteElAdmin() {
        var s = auth.login("admin", "admin1234");
        assertEquals(AppUser.Role.ADMIN, s.user().getRole());
        assertTrue(tokens.verify(s.token()).isPresent());
    }

    @Test void loginConDatosIncorrectosFalla() {
        assertThrows(BusinessException.class, () -> auth.login("admin", "mala"));
        assertThrows(BusinessException.class, () -> auth.login("nadie", "admin1234"));
    }

    @Test void tokenAlteradoOVencidoNoVale() {
        AppUser u = auth.login("admin", "admin1234").user();
        String t = tokens.issue(u);
        assertTrue(tokens.verify(t).isPresent());
        assertTrue(tokens.verify(t + "x").isEmpty());
        assertTrue(tokens.verify("basura").isEmpty());
        assertTrue(tokens.verify(tokens.issue(u, 1)).isEmpty(), "vencido");
    }

    @Test void crearUsuarioValidaNombreRepetidoYContrasena() {
        AppUser v = auth.create("ana", "clave123", AppUser.Role.VENDEDOR);
        assertEquals(AppUser.Role.VENDEDOR, v.getRole());
        assertThrows(BusinessException.class, () -> auth.create("ANA", "clave123", null));
        assertThrows(BusinessException.class, () -> auth.create("beto", "123", null));
        assertEquals("ana", auth.login("Ana", "clave123").user().getUsername());
    }

    @Test void cambiarContrasenaYUltimoAdmin() {
        AppUser admin = auth.login("admin", "admin1234").user();
        auth.changePassword(admin.getId(), "admin1234", "nueva-clave");
        assertThrows(BusinessException.class, () -> auth.login("admin", "admin1234"));
        assertNotNull(auth.login("admin", "nueva-clave"));
        assertThrows(BusinessException.class, () -> auth.changePassword(admin.getId(), "incorrecta", "otra-clave"));
        assertThrows(BusinessException.class, () -> auth.setActive(admin.getId(), false), "no se puede desactivar al único admin");
    }

    @Test void usuarioDesactivadoNoEntra() {
        AppUser v = auth.create("luis", "clave123", AppUser.Role.VENDEDOR);
        auth.setActive(v.getId(), false);
        assertThrows(BusinessException.class, () -> auth.login("luis", "clave123"));
    }
}
