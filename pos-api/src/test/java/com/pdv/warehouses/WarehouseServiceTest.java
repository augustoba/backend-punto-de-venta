package com.pdv.warehouses;

import com.pdv.catalog.CatalogService;
import com.pdv.catalog.CatalogService.ProductData;
import com.pdv.catalog.Product;
import com.pdv.common.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class WarehouseServiceTest {
    @Autowired WarehouseService service;
    @Autowired CatalogService catalog;

    private Product prod(int stock) {
        return catalog.create(new ProductData("P" + System.nanoTime(), "", null, null, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO, 0, 0, new BigDecimal("21"), null), stock, "t");
    }
    private Warehouse principal() { return service.list().stream().filter(Warehouse::isMain).findFirst().orElseThrow(); }

    @Test void existeElDepositoPrincipalYSuStockEsElTotal() {
        Product p = prod(10);
        assertEquals(10, service.available(p, principal()));
    }

    @Test void transferirMueveElStockSinCambiarElTotal() {
        Product p = prod(10);
        Warehouse local = service.create("Local 2");
        service.transfer(principal().getId(), local.getId(), List.of(new WarehouseService.LineIn(p.getId(), 4)), "", "t");
        assertEquals(6, service.available(p, principal()));
        assertEquals(4, service.available(p, local));
        assertEquals(10, catalog.get(p.getId()).getStock(), "el total no cambia");
    }

    @Test void unaVentaDescuentaDelPrincipal() {
        Product p = prod(10);
        Warehouse local = service.create("Local 3");
        service.transfer(principal().getId(), local.getId(), List.of(new WarehouseService.LineIn(p.getId(), 4)), "", "t");
        catalog.consume(p.getId(), 2, "venta", "x", "t");
        assertEquals(4, service.available(catalog.get(p.getId()), principal()));
        assertEquals(4, service.available(catalog.get(p.getId()), local));
    }

    @Test void deUnDepositoSecundarioAOtroYDevolverAlPrincipal() {
        Product p = prod(10);
        Warehouse a = service.create("A"), b = service.create("B");
        service.transfer(principal().getId(), a.getId(), List.of(new WarehouseService.LineIn(p.getId(), 5)), "", "t");
        service.transfer(a.getId(), b.getId(), List.of(new WarehouseService.LineIn(p.getId(), 3)), "", "t");
        service.transfer(b.getId(), principal().getId(), List.of(new WarehouseService.LineIn(p.getId(), 1)), "", "t");
        Product q = catalog.get(p.getId());
        assertEquals(2, service.available(q, a));
        assertEquals(2, service.available(q, b));
        assertEquals(6, service.available(q, principal()));
    }

    @Test void validaOrigenDestinoStockYCantidades() {
        Product p = prod(3);
        Warehouse a = service.create("Vacío");
        Long main = principal().getId();
        assertThrows(BusinessException.class, () -> service.transfer(main, main, List.of(new WarehouseService.LineIn(p.getId(), 1)), "", "t"));
        assertThrows(BusinessException.class, () -> service.transfer(a.getId(), main, List.of(new WarehouseService.LineIn(p.getId(), 1)), "", "t"), "no alcanza en el origen");
        assertThrows(BusinessException.class, () -> service.transfer(main, a.getId(), List.of(new WarehouseService.LineIn(p.getId(), 5)), "", "t"));
        assertThrows(BusinessException.class, () -> service.transfer(main, a.getId(), List.of(new WarehouseService.LineIn(p.getId(), 0)), "", "t"));
        assertThrows(BusinessException.class, () -> service.transfer(main, a.getId(), List.of(), "", "t"));
        assertThrows(BusinessException.class, () -> service.create(" "));
    }

    @Test void elListadoMuestraElStockPorDeposito() {
        Product p = prod(8);
        Warehouse a = service.create("Listado");
        service.transfer(principal().getId(), a.getId(), List.of(new WarehouseService.LineIn(p.getId(), 3)), "n", "t");
        var row = service.stock().stream().filter(r -> r.productId().equals(p.getId())).findFirst().orElseThrow();
        assertEquals(8, row.total());
        assertEquals(3, row.byWarehouse().get(a.getId()));
        assertEquals(1, service.transfers().stream().filter(t -> t.getToId().equals(a.getId())).count());
    }
}
