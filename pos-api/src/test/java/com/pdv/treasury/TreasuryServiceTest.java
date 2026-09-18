package com.pdv.treasury;

import com.pdv.common.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TreasuryServiceTest {
    @Autowired TreasuryService service;
    @Autowired CategoryRepository categoryRepo;

    private Account acc(String name, Account.Type t, String initial) {
        return service.createAccount(name, t, null, null, new BigDecimal(initial), "test");
    }
    private BigDecimal bd(String s) { return new BigDecimal(s); }

    @Test void editarCuentaYCategoria() {
        Account a = service.createAccount("Vieja", Account.Type.CAJA, "#111111", "", null, "t");
        service.updateAccount(a.getId(), "Nueva", Account.Type.BANCO, "#222222", "nota");
        assertEquals("Nueva", service.accounts().stream().filter(x -> x.getId().equals(a.getId())).findFirst().orElseThrow().getName());
        Category c = service.updateCategory(categoryRepo.findAll().get(0).getId(), "Renombrada", "X", "#333333", java.util.List.of("Sub A"));
        assertEquals("Renombrada", c.getName());
        assertEquals(java.util.List.of("Sub A"), c.getSubcategories());
        assertThrows(com.pdv.common.BusinessException.class, () -> service.updateAccount(a.getId(), " ", null, null, null));
    }

    @Test void saldoInicialEsUnMovimientoYElSaldoEsLaSuma() {
        Account a = acc("Caja test", Account.Type.CAJA, "1000");
        assertEquals(0, bd("1000").compareTo(service.balance(a.getId())));
        service.addMovement(a.getId(), bd("-300"), "Otros gastos del local", "Insumos de limpieza", "x", null, "manual", "", "test");
        assertEquals(0, bd("700").compareTo(service.balance(a.getId())));
    }

    @Test void transferenciaEsNeutraEnElTotal() {
        Account a = acc("A", Account.Type.CAJA, "1000"), b = acc("B", Account.Type.BANCO, "0");
        service.transfer(a.getId(), b.getId(), bd("300"), null, "test");
        assertEquals(0, bd("700").compareTo(service.balance(a.getId())));
        assertEquals(0, bd("300").compareTo(service.balance(b.getId())));
        assertEquals(0, bd("1000").compareTo(service.balance(a.getId()).add(service.balance(b.getId()))));
    }

    @Test void noSePuedeTransferirALaMismaCuentaNiUnImporteInvalido() {
        Account a = acc("A", Account.Type.CAJA, "100");
        assertThrows(BusinessException.class, () -> service.transfer(a.getId(), a.getId(), bd("10"), null, "t"));
        Account b = acc("B", Account.Type.CAJA, "0");
        assertThrows(BusinessException.class, () -> service.transfer(a.getId(), b.getId(), bd("0"), null, "t"));
    }

    @Test void laSubcategoriaEsObligatoriaSiLaCategoriaLaTiene() {
        Account a = acc("A", Account.Type.CAJA, "0");
        assertThrows(BusinessException.class, () -> service.addMovement(a.getId(), bd("-5"), "Servicios", "", "luz", Instant.now(), "manual", "", "t"));
        assertDoesNotThrow(() -> service.addMovement(a.getId(), bd("-5"), "Servicios", "Electricidad", "luz", Instant.now(), "manual", "", "t"));
    }

    @Test void importeCeroEsInvalido() {
        Account a = acc("A", Account.Type.CAJA, "0");
        assertThrows(BusinessException.class, () -> service.addMovement(a.getId(), BigDecimal.ZERO, "Saldo inicial", "", "", null, "manual", "", "t"));
    }
}
