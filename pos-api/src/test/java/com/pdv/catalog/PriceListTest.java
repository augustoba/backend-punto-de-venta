package com.pdv.catalog;

import com.pdv.catalog.CatalogService.ProductData;
import com.pdv.common.BusinessException;
import com.pdv.sales.Sale;
import com.pdv.sales.SalesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PriceListTest {
    @Autowired PriceListService lists;
    @Autowired CatalogService catalog;
    @Autowired SalesService sales;

    private BigDecimal bd(String s) { return new BigDecimal(s); }
    private Product prod(String price, String offer) {
        return catalog.create(new ProductData("L" + System.nanoTime(), "", null, null, bd("100"), bd(price), bd(offer), 0, 0, bd("21"), null), 20, "t");
    }
    private PriceList principal() { return lists.list().stream().filter(PriceList::isMain).findFirst().orElseThrow(); }

    @Test void laPrincipalExisteYNoCambiaElPrecio() {
        Product p = prod("1000", "0");
        assertEquals(0, bd("1000").compareTo(lists.priceIn(p, principal().getId())));
        assertEquals(0, bd("1000").compareTo(lists.priceIn(p, null)));
    }

    @Test void mayoristaAplicaElPorcentaje() {
        Product p = prod("1000", "0");
        PriceList may = lists.create("Mayorista " + System.nanoTime(), bd("-15"));
        assertEquals(0, bd("850.00").compareTo(lists.priceIn(p, may.getId())));
        PriceList rec = lists.create("Recargo " + System.nanoTime(), bd("10"));
        assertEquals(0, bd("1100.00").compareTo(lists.priceIn(p, rec.getId())));
    }

    @Test void laVentaConListaUsaSuPrecioEIgnoraLaOferta() {
        Product p = prod("1000", "900");
        PriceList may = lists.create("May " + System.nanoTime(), bd("-20"));
        Sale s = sales.register(new SalesService.SaleIn(null, List.of(new SalesService.LineIn(p.getId(), 2, null, null)), null, Sale.Method.EFECTIVO, true, "", false, null, null, may.getId()), "t");
        assertEquals(0, bd("1600.00").compareTo(s.getTotal()));
        assertEquals(may.getId(), s.getPriceListId());
        Sale normal = sales.register(new SalesService.SaleIn(null, List.of(new SalesService.LineIn(p.getId(), 1, null, null)), null, Sale.Method.EFECTIVO, true, "", false, null, null), "t");
        assertEquals(0, bd("900.00").compareTo(normal.getTotal()), "sin lista se usa la oferta");
    }

    @Test void unPrecioExplicitoGanaSobreLaLista() {
        Product p = prod("1000", "0");
        PriceList may = lists.create("Exp " + System.nanoTime(), bd("-50"));
        Sale s = sales.register(new SalesService.SaleIn(null, List.of(new SalesService.LineIn(p.getId(), 1, bd("700"), null)), null, Sale.Method.EFECTIVO, true, "", false, null, null, may.getId()), "t");
        assertEquals(0, bd("700.00").compareTo(s.getTotal()));
    }

    @Test void validaciones() {
        assertThrows(BusinessException.class, () -> lists.create(" ", bd("5")));
        assertThrows(BusinessException.class, () -> lists.create("X", null));
        assertThrows(BusinessException.class, () -> lists.create("Y", bd("-100")));
        assertThrows(BusinessException.class, () -> lists.update(principal().getId(), "Otra", bd("5")));
        assertThrows(BusinessException.class, () -> lists.delete(principal().getId()));
        PriceList l = lists.create("Borrar " + System.nanoTime(), bd("5"));
        lists.delete(l.getId());
        assertThrows(com.pdv.common.NotFoundException.class, () -> lists.get(l.getId()));
    }
}
