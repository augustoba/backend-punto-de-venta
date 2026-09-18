package com.pdv.sales;

import com.pdv.catalog.CatalogService;
import com.pdv.catalog.CatalogService.ProductData;
import com.pdv.catalog.Product;
import com.pdv.common.BusinessException;
import com.pdv.parties.Customer;
import com.pdv.parties.PartyEntry.Party;
import com.pdv.parties.PartyService;
import com.pdv.sales.Sale.Method;
import com.pdv.sales.SalesService.LineIn;
import com.pdv.sales.SalesService.SaleIn;
import com.pdv.settings.Settings;
import com.pdv.settings.SettingsService;
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
class SalesServiceTest {
    @Autowired SalesService sales;
    @Autowired CatalogService catalog;
    @Autowired TreasuryService treasury;
    @Autowired PartyService parties;
    @Autowired SettingsService settings;

    private BigDecimal bd(String s) { return new BigDecimal(s); }
    private Product prod(int stock) { return catalog.create(new ProductData("P", "", null, null, bd("500"), bd("1000"), BigDecimal.ZERO, 0, 0, bd("21"), null), stock, "t"); }
    private SaleIn venta(Long customer, Long productId, int qty, Method m, boolean paid, BigDecimal pct) {
        return new SaleIn(customer, List.of(new LineIn(productId, qty, null, null)), pct, m, paid, "", false, null, null);
    }
    private Customer cliente() { Customer c = new Customer(); c.setName("Clara"); return parties.saveCustomer(null, c); }
    private BigDecimal cajaTotal() { return treasury.balance(treasury.firstAccount(Account.Type.CAJA).getId()); }

    @Test void ventaEnEfectivoBajaStockEIngresaALaCaja() {
        Product p = prod(10);
        BigDecimal antes = cajaTotal();
        Sale s = sales.register(venta(null, p.getId(), 2, Method.EFECTIVO, true, null), "t");
        assertEquals(0, bd("2000").compareTo(s.getTotal()));
        assertEquals(8, catalog.get(p.getId()).getStock());
        assertEquals(0, antes.add(bd("2000")).compareTo(cajaTotal()));
    }

    @Test void sePuedeVenderSinStock() {
        Product p = prod(0);
        sales.register(venta(null, p.getId(), 1, Method.EFECTIVO, true, null), "t");
        assertEquals(-1, catalog.get(p.getId()).getStock());
    }

    @Test void sinPagarGeneraDeudaYNoMueveDinero() {
        Product p = prod(5); Customer c = cliente();
        BigDecimal antes = cajaTotal();
        Sale s = sales.register(venta(c.getId(), p.getId(), 1, Method.EFECTIVO, false, null), "t");
        assertEquals(Method.CUENTA_CORRIENTE, s.getMethod());
        assertEquals(0, bd("1000").compareTo(parties.balance(Party.CUSTOMER, c.getId())));
        assertEquals(0, antes.compareTo(cajaTotal()));
    }

    @Test void cuentaCorrienteSinClienteEsError() {
        Product p = prod(5);
        assertThrows(BusinessException.class, () -> sales.register(venta(null, p.getId(), 1, Method.CUENTA_CORRIENTE, false, null), "t"));
    }

    @Test void descuentoAutomaticoPorTransferencia() {
        Settings s = settings.get(); s.setTransferDiscount(bd("10"));
        Product p = prod(5);
        Sale sale = sales.register(venta(null, p.getId(), 1, Method.TRANSFERENCIA, true, null), "t");
        assertEquals(0, bd("900").compareTo(sale.getTotal()));
    }

    @Test void descuentoGlobalPorcentaje() {
        Product p = prod(5);
        Sale sale = sales.register(venta(null, p.getId(), 1, Method.EFECTIVO, true, bd("20")), "t");
        assertEquals(0, bd("800").compareTo(sale.getTotal()));
        assertEquals(0, bd("200").compareTo(sale.getDiscountAmount()));
    }

    @Test void anularDevuelveElStockYQuitaLosAsientos() {
        Product p = prod(10);
        BigDecimal antes = cajaTotal();
        Sale s = sales.register(venta(null, p.getId(), 3, Method.EFECTIVO, true, null), "t");
        sales.cancel(s.getId(), "t");
        assertEquals(10, catalog.get(p.getId()).getStock());
        assertEquals(0, antes.compareTo(cajaTotal()));
    }

    @Test void arqueoConCajaCerradaNoDejaVender() {
        settings.get().setArqueo(true);
        Product p = prod(5);
        assertThrows(BusinessException.class, () -> sales.register(venta(null, p.getId(), 1, Method.EFECTIVO, true, null), "t"));
        Account caja = treasury.firstAccount(Account.Type.CAJA);
        sales.open(caja.getId(), treasury.balance(caja.getId()), "", "t");
        assertDoesNotThrow(() -> sales.register(venta(null, p.getId(), 1, Method.EFECTIVO, true, null), "t"));
    }

    @Test void aperturaConDiferenciaYCierreConSobrante() {
        Account caja = treasury.createAccount("Caja arqueo", Account.Type.CAJA, null, null, BigDecimal.ZERO, "t");
        sales.open(caja.getId(), bd("5000"), "", "t");
        assertEquals(0, bd("5000").compareTo(treasury.balance(caja.getId())));
        treasury.addMovement(caja.getId(), bd("-300"), "Otros gastos del local", "Insumos de limpieza", "x", null, "manual", "", "t");
        assertEquals(0, bd("4700").compareTo(sales.expectedClose()));
        CashSession c = sales.close(bd("4800"), "", "t");
        assertEquals(0, bd("100").compareTo(c.getDiff()));
        assertEquals(0, bd("4800").compareTo(treasury.balance(caja.getId())));
        assertEquals(0, bd("4800").compareTo(sales.previousClose(caja.getId())));
    }

    @Test void ventaDeUnComboDescuentaLosComponentes() {
        Product a = prod(10);
        Product combo = catalog.create(new ProductData("Combo", "", null, null, BigDecimal.ZERO, bd("1500"), BigDecimal.ZERO, 0, 0, bd("21"), List.of(new Product.ComboItem(a.getId(), 2))), 0, "t");
        sales.register(venta(null, combo.getId(), 1, Method.EFECTIVO, true, null), "t");
        assertEquals(8, catalog.get(a.getId()).getStock());
    }
}
