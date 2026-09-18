package com.pdv.returns;

import com.pdv.catalog.CatalogService;
import com.pdv.catalog.CatalogService.ProductData;
import com.pdv.catalog.Product;
import com.pdv.common.BusinessException;
import com.pdv.parties.Customer;
import com.pdv.parties.PartyEntry;
import com.pdv.parties.PartyService;
import com.pdv.sales.Sale;
import com.pdv.sales.SalesService;
import com.pdv.treasury.Account;
import com.pdv.treasury.TreasuryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReturnsServiceTest {
    @Autowired ReturnsService service;
    @Autowired SalesService sales;
    @Autowired CatalogService catalog;
    @Autowired TreasuryService treasury;
    @Autowired PartyService parties;

    private BigDecimal bd(String s) { return new BigDecimal(s); }
    private Product prod(int stock) {
        return catalog.create(new ProductData("D" + System.nanoTime(), "", null, null, bd("600"), bd("1000"), BigDecimal.ZERO, 0, 0, bd("21"), null), stock, "t");
    }
    private Sale sell(Product p, int qty, Long customerId, boolean paid) {
        return sales.register(new SalesService.SaleIn(customerId, List.of(new SalesService.LineIn(p.getId(), qty, null, null)), null, Sale.Method.EFECTIVO, paid, "", false, null, null), "t");
    }
    private BigDecimal cajaBalance() { return treasury.balance(treasury.firstAccount(Account.Type.CAJA).getId()); }
    private List<ReturnsService.LineIn> uno(Product p, int qty, boolean restock) { return List.of(new ReturnsService.LineIn(p.getId(), qty, restock)); }

    @Test void devolucionParcialVuelveElStockYElDinero() {
        Product p = prod(10);
        Sale s = sell(p, 3, null, true);
        BigDecimal antes = cajaBalance();
        SaleReturn r = service.register(s.getId(), uno(p, 2, true), "No le gustó", SaleReturn.Refund.EFECTIVO, "t");
        assertEquals(0, bd("2000.00").compareTo(r.getTotal()));
        assertEquals(9, catalog.get(p.getId()).getStock(), "10 − 3 vendidas + 2 devueltas");
        assertEquals(0, antes.subtract(bd("2000")).compareTo(cajaBalance()));
        assertTrue(r.getCreditNote().startsWith("NC-000001-"));
    }

    @Test void noSePuedeDevolverMasDeLoVendidoNiEnDosVeces() {
        Product p = prod(10);
        Sale s = sell(p, 2, null, true);
        service.register(s.getId(), uno(p, 1, true), "", SaleReturn.Refund.EFECTIVO, "t");
        assertEquals(1, service.returnable(s.getId()).get(0).remaining());
        assertThrows(BusinessException.class, () -> service.register(s.getId(), uno(p, 2, true), "", SaleReturn.Refund.EFECTIVO, "t"));
        service.register(s.getId(), uno(p, 1, true), "", SaleReturn.Refund.EFECTIVO, "t");
        assertThrows(BusinessException.class, () -> service.register(s.getId(), uno(p, 1, true), "", SaleReturn.Refund.EFECTIVO, "t"));
    }

    @Test void mermaNoVuelveAlStockPeroSiDevuelveElDinero() {
        Product p = prod(10);
        Sale s = sell(p, 1, null, true);
        service.register(s.getId(), uno(p, 1, false), "Vencido", SaleReturn.Refund.EFECTIVO, "t");
        assertEquals(9, catalog.get(p.getId()).getStock());
    }

    @Test void acreditarEnCuentaCorrienteBajaLaDeudaSinMoverDinero() {
        Customer nuevo = new Customer(); nuevo.setName("Ana"); Customer c = parties.saveCustomer(null, nuevo);
        Product p = prod(10);
        Sale s = sell(p, 2, c.getId(), false);            // a cuenta corriente: debe 2000
        BigDecimal caja = cajaBalance();
        service.register(s.getId(), uno(p, 1, true), "", SaleReturn.Refund.CUENTA_CORRIENTE, "t");
        assertEquals(0, bd("1000.00").compareTo(parties.balance(PartyEntry.Party.CUSTOMER, c.getId())));
        assertEquals(0, caja.compareTo(cajaBalance()), "no mueve dinero");
    }

    @Test void cuentaCorrienteExigeClienteYCambioNoMueveDinero() {
        Product p = prod(5);
        Sale s = sell(p, 1, null, true);
        assertThrows(BusinessException.class, () -> service.register(s.getId(), uno(p, 1, true), "", SaleReturn.Refund.CUENTA_CORRIENTE, "t"));
        BigDecimal caja = cajaBalance();
        service.register(s.getId(), uno(p, 1, true), "Cambio de talle", SaleReturn.Refund.CAMBIO, "t");
        assertEquals(0, caja.compareTo(cajaBalance()));
        assertEquals(5, catalog.get(p.getId()).getStock());
    }

    @Test void descuentoDeLaVentaSeReflejaEnLoDevuelto() {
        Product p = prod(5);
        Sale s = sales.register(new SalesService.SaleIn(null, List.of(new SalesService.LineIn(p.getId(), 1, null, null)), bd("10"), Sale.Method.EFECTIVO, true, "", false, null, null), "t");
        SaleReturn r = service.register(s.getId(), uno(p, 1, true), "", SaleReturn.Refund.EFECTIVO, "t");
        assertEquals(0, bd("900.00").compareTo(r.getTotal()), "se devuelve lo que se pagó, no el precio de lista");
    }

    @Test void validaciones() {
        Product p = prod(5);
        Sale s = sell(p, 1, null, true);
        assertThrows(BusinessException.class, () -> service.register(s.getId(), List.of(), "", SaleReturn.Refund.EFECTIVO, "t"));
        assertThrows(BusinessException.class, () -> service.register(s.getId(), uno(p, 0, true), "", SaleReturn.Refund.EFECTIVO, "t"));
        assertThrows(BusinessException.class, () -> service.register(s.getId(), List.of(new ReturnsService.LineIn(999999L, 1, true)), "", SaleReturn.Refund.EFECTIVO, "t"));
        service.register(s.getId(), uno(p, 1, true), "", SaleReturn.Refund.EFECTIVO, "t");
        assertThrows(BusinessException.class, () -> sales.cancel(s.getId(), "t"), "una venta con devoluciones no se elimina");
    }
}
