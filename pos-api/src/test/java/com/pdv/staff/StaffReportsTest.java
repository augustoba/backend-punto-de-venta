package com.pdv.staff;

import com.pdv.catalog.CatalogService;
import com.pdv.catalog.CatalogService.ProductData;
import com.pdv.catalog.Product;
import com.pdv.common.BusinessException;
import com.pdv.reports.ReportsService;
import com.pdv.sales.Sale;
import com.pdv.sales.SalesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class StaffReportsTest {
    @Autowired StaffService staff;
    @Autowired ReportsService reports;
    @Autowired SalesService sales;
    @Autowired CatalogService catalog;

    private BigDecimal bd(String s) { return new BigDecimal(s); }
    private Employee emp() { Employee e = new Employee(); e.setName("Ana"); return staff.save(null, e); }

    @Test void lasHorasSeSumanPorEmpleado() {
        Employee e = emp();
        staff.clock(e.getId(), LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(13, 0), "");
        staff.clock(e.getId(), LocalDate.now(), LocalTime.of(15, 0), LocalTime.of(17, 30), "");
        assertEquals(6.5, staff.hoursOf(e.getId()), 0.001);
    }

    @Test void laSalidaDebeSerPosteriorALaEntrada() {
        Employee e = emp();
        assertThrows(BusinessException.class, () -> staff.clock(e.getId(), LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(1, 0), ""));
        assertThrows(BusinessException.class, () -> staff.clock(e.getId(), LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(9, 0), ""));
    }

    @Test void rankingConGananciaSobreElCostoCongelado() {
        Product p = catalog.create(new ProductData("Rank", "", null, null, bd("600"), bd("1000"), BigDecimal.ZERO, 0, 0, bd("21"), null), 10, "t");
        sales.register(new SalesService.SaleIn(null, List.of(new SalesService.LineIn(p.getId(), 2, null, null)), null, Sale.Method.EFECTIVO, true, "", false, null, null), "t");
        catalog.setCost(catalog.get(p.getId()), bd("900"), "t");   // un cambio posterior de costo no altera lo ya vendido
        var row = reports.productRanking(null, null).stream().filter(r -> r.productId().equals(p.getId())).findFirst().orElseThrow();
        assertEquals(2, row.qty());
        assertEquals(0, bd("2000").compareTo(row.revenue()));
        assertEquals(0, bd("1200").compareTo(row.cost()));
        assertEquals(0, bd("800").compareTo(row.profit()));
    }

    @Test void ventasPorMedioDePago() {
        Product p = catalog.create(new ProductData("M", "", null, null, bd("1"), bd("100"), BigDecimal.ZERO, 0, 0, bd("21"), null), 5, "t");
        sales.register(new SalesService.SaleIn(null, List.of(new SalesService.LineIn(p.getId(), 1, null, null)), null, Sale.Method.EFECTIVO, true, "", false, null, null), "t");
        assertTrue(reports.salesByMethod(null, null, false).stream().anyMatch(m -> m.method() == Sale.Method.EFECTIVO && m.count() >= 1));
    }
}
