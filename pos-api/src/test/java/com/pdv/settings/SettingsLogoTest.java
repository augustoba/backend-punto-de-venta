package com.pdv.settings;

import com.pdv.common.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SettingsLogoTest {
    @Autowired SettingsService service;

    private Settings withLogo(String logo) { Settings s = service.get(); s.setLogo(logo); return s; }

    @Test void guardaYDevuelveElLogo() {
        String png = "data:image/png;base64,iVBORw0KGgo=";
        assertEquals(png, service.update(withLogo(png)).getLogo());
        assertEquals("", service.update(withLogo("")).getLogo(), "vacío quita el logo");
    }

    @Test void rechazaLoQueNoEsImagenOEsMuyPesado() {
        assertThrows(BusinessException.class, () -> service.update(withLogo("http://sitio/logo.png")));
        assertThrows(BusinessException.class, () -> service.update(withLogo("data:text/html;base64,PHNjcmlwdD4=")));
        assertThrows(BusinessException.class, () -> service.update(withLogo("data:image/png;base64," + "A".repeat(600_001))));
    }
}
