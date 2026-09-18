package com.pdv.parties;

import com.pdv.common.BusinessException;
import com.pdv.parties.PartyEntry.Party;
import com.pdv.treasury.Account;
import com.pdv.treasury.TreasuryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PartyServiceTest {
    @Autowired PartyService service;
    @Autowired TreasuryService treasury;

    private BigDecimal bd(String s) { return new BigDecimal(s); }
    private Customer cliente(String name) { Customer c = new Customer(); c.setName(name); return service.saveCustomer(null, c); }
    private Supplier prov(String name) { Supplier s = new Supplier(); s.setName(name); return service.saveSupplier(null, s); }

    @Test void aumentarDeudaYPagarBajaElSaldoEIngresaALaCuenta() {
        Customer c = cliente("Clara");
        Account banco = treasury.createAccount("Banco t", Account.Type.BANCO, null, null, BigDecimal.ZERO, "t");
        service.customerMovement(c.getId(), "deuda", bd("500"), null, "", "t");
        assertEquals(0, bd("500").compareTo(service.balance(Party.CUSTOMER, c.getId())));
        service.customerMovement(c.getId(), "pago", bd("200"), banco.getId(), "", "t");
        assertEquals(0, bd("300").compareTo(service.balance(Party.CUSTOMER, c.getId())));
        assertEquals(0, bd("200").compareTo(treasury.balance(banco.getId())));
    }

    @Test void elPagoExigeUnaCuentaYElMontoDebeSerPositivo() {
        Customer c = cliente("X");
        assertThrows(BusinessException.class, () -> service.customerMovement(c.getId(), "pago", bd("10"), null, "", "t"));
        assertThrows(BusinessException.class, () -> service.customerMovement(c.getId(), "deuda", bd("0"), null, "", "t"));
    }

    @Test void libroConSaldoCorrido() {
        Customer c = cliente("Y");
        Account caja = treasury.createAccount("Caja t", Account.Type.CAJA, null, null, BigDecimal.ZERO, "t");
        service.customerMovement(c.getId(), "deuda", bd("500"), null, "", "t");
        service.customerMovement(c.getId(), "pago", bd("200"), caja.getId(), "", "t");
        var libro = service.ledger(Party.CUSTOMER, c.getId());
        assertEquals(2, libro.size());
        assertEquals(0, bd("500").compareTo(libro.get(0).balance()));
        assertEquals(0, bd("300").compareTo(libro.get(1).balance()));
    }

    @Test void proveedorCompraSumaDeudaYPagoDescuentaDeLaCuenta() {
        Supplier s = prov("Prov");
        Account caja = treasury.createAccount("Caja p", Account.Type.CAJA, null, null, bd("1000"), "t");
        service.supplierMovement(s.getId(), "compra", bd("600"), null, "", "t");
        assertEquals(0, bd("600").compareTo(service.balance(Party.SUPPLIER, s.getId())));
        service.supplierMovement(s.getId(), "pago", bd("250"), caja.getId(), "", "t");
        assertEquals(0, bd("350").compareTo(service.balance(Party.SUPPLIER, s.getId())));
        assertEquals(0, bd("750").compareTo(treasury.balance(caja.getId())));
    }

    @Test void notaDeCreditoBajaLaDeudaSinMoverDinero() {
        Supplier s = prov("P2");
        service.supplierMovement(s.getId(), "compra", bd("100"), null, "", "t");
        service.supplierMovement(s.getId(), "nota_credito", bd("40"), null, "", "t");
        assertEquals(0, bd("60").compareTo(service.balance(Party.SUPPLIER, s.getId())));
    }

    @Test void totalesSoloCuentanLosSaldosPositivos() {
        BigDecimal antes = service.totalReceivable();
        Customer c = cliente("Z");
        service.customerMovement(c.getId(), "deuda", bd("70"), null, "", "t");
        assertEquals(0, antes.add(bd("70")).compareTo(service.totalReceivable()));
    }
}
