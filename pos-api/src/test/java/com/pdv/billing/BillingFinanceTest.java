package com.pdv.billing;

import com.pdv.catalog.CatalogService;
import com.pdv.catalog.CatalogService.ProductData;
import com.pdv.catalog.Product;
import com.pdv.common.BusinessException;
import com.pdv.finance.Cheque;
import com.pdv.finance.CostCenter;
import com.pdv.finance.FinanceService;
import com.pdv.sales.Sale;
import com.pdv.sales.SalesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BillingFinanceTest {
    @Autowired BillingService billing;
    @Autowired FinanceService finance;
    @Autowired SalesService sales;
    @Autowired CatalogService catalog;

    private BigDecimal bd(String s) { return new BigDecimal(s); }
    private Product prod() { return catalog.create(new ProductData("P", "", null, null, bd("500"), bd("1000"), BigDecimal.ZERO, 0, 0, bd("21"), null), 10, "t"); }

    @Test void elPresupuestoSumaConIvaIncluidoYPasaAVendidoAlConvertirse() {
        Product p = prod();
        Budget b = billing.saveBudget(null, null, null, List.of(new BillingService.BudgetLineIn(p.getId(), "P", 2, bd("1000"))), "", "t");
        assertEquals(0, bd("2000").compareTo(b.getTotal()));
        assertEquals(Budget.Status.ACTIVO, b.getStatus());
        sales.register(new SalesService.SaleIn(null, List.of(new SalesService.LineIn(p.getId(), 2, null, null)), null, Sale.Method.EFECTIVO, true, "", false, b.getId(), null), "t");
        assertEquals(Budget.Status.VENDIDO, billing.budget(b.getId()).getStatus());
    }

    @Test void presupuestoVencidoSeDetecta() {
        Product p = prod();
        Budget b = billing.saveBudget(null, null, LocalDate.now().minusDays(1), List.of(new BillingService.BudgetLineIn(p.getId(), "P", 1, bd("10"))), "", "t");
        assertTrue(b.isExpired());
    }

    @Test void facturaSeparaNetoEIva() {
        Invoice f = billing.issue(null, "x", bd("999"), null);
        assertEquals(0, bd("825.62").compareTo(f.getNet()));
        assertEquals(0, bd("173.38").compareTo(f.getIva()));
    }

    @Test void ventaConFacturaEmiteElComprobante() {
        Product p = prod();
        int antes = billing.invoices().size();
        sales.register(new SalesService.SaleIn(null, List.of(new SalesService.LineIn(p.getId(), 1, null, null)), null, Sale.Method.EFECTIVO, true, "", true, null, null), "t");
        assertEquals(antes + 1, billing.invoices().size());
    }

    @Test void chequeExigeValorYVencimientoYSeNumeraSolo() {
        assertThrows(BusinessException.class, () -> finance.saveCheque(null, Cheque.Kind.COBRAR, bd("5000"), null, null, ""));
        Cheque c = finance.saveCheque(null, Cheque.Kind.COBRAR, bd("5000"), LocalDate.now().plusDays(10), null, "test");
        assertTrue(c.getNumber().startsWith("#"));
        BigDecimal antes = finance.pending(Cheque.Kind.COBRAR);
        finance.setCollected(c.getId(), true);
        assertEquals(0, antes.subtract(bd("5000")).compareTo(finance.pending(Cheque.Kind.COBRAR)));
    }

    @Test void centroDeCostosSumaSusCostosFijos() {
        CostCenter c = finance.createCenter("Gastos del local", CostCenter.Allocation.FACTURACION);
        finance.addFixedCost(c.getId(), "Alquiler", bd("40000"), "");
        finance.addFixedCost(c.getId(), "Luz", bd("5000"), "");
        assertEquals(0, bd("45000").compareTo(finance.center(c.getId()).getMonthlyTotal()));
    }
}
