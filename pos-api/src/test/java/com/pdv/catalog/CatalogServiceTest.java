package com.pdv.catalog;

import com.pdv.catalog.CatalogService.ProductData;
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
class CatalogServiceTest {
    @Autowired CatalogService service;

    private BigDecimal bd(String s) { return new BigDecimal(s); }
    private ProductData data(String name, String price, String cost, List<Product.ComboItem> combo) {
        return new ProductData(name, "", null, null, bd(cost), bd(price), BigDecimal.ZERO, 0, 0, bd("21"), combo);
    }
    private Product prod(String name, int stock) { return service.create(data(name, "1000", "500", null), stock, "t"); }

    @Test void crearConStockDejaUnaFilaEnElLibro() {
        Product p = prod("Coca", 10);
        List<StockMove> ms = service.stockMoves(p.getId());
        assertEquals(1, ms.size());
        assertEquals(CatalogService.R_CREATE, ms.get(0).getReason());
        assertEquals(10, ms.get(0).getResult());
    }

    @Test void seVendeSinStockYQuedaNegativo() {
        Product p = prod("Sin stock", 0);
        service.consume(p.getId(), 1, CatalogService.R_SALE, "Venta #1", "t");
        assertEquals(-1, service.get(p.getId()).getStock());
    }

    @Test void elStockDeUnComboSeDerivaYSeDescuentaDeLosComponentes() {
        Product a = prod("A", 10);
        Product combo = service.create(data("Combo", "1500", "0", List.of(new Product.ComboItem(a.getId(), 2))), 0, "t");
        assertEquals(5, service.stockOf(combo));
        service.consume(combo.getId(), 1, CatalogService.R_SALE, "Venta #2", "t");
        assertEquals(8, service.get(a.getId()).getStock());
        assertThrows(BusinessException.class, () -> service.setStock(combo.getId(), 3, "t"));
    }

    @Test void cambiarPrecioOCostoQuedaEnElHistorial() {
        Product p = prod("P", 0);
        service.update(p.getId(), data("P", "1200", "500", null), "t");
        assertEquals(1, service.priceChanges().size());
        assertEquals("price", service.priceChanges().get(0).getField());
    }

    @Test void edicionManualDeStockRegistraElDelta() {
        Product p = prod("P", 9);
        service.setStock(p.getId(), 15, "t");
        StockMove m = service.stockMoves(p.getId()).get(0);
        assertEquals(6, m.getDelta());
        assertEquals(CatalogService.R_MANUAL, m.getReason());
    }

    @Test void actualizacionMasivaPorPorcentajeConRedondeo() {
        Product p = prod("P", 0);
        service.bulkPrice(List.of(p.getId()), false, true, bd("10"), true, "t");
        assertEquals(0, bd("1100").compareTo(service.get(p.getId()).getPrice()));
    }

    @Test void fotoDelProducto() {
        String foto = "data:image/jpeg;base64,/9j/4AAQ";
        Product p = service.create(new ProductData("Con foto", "", null, null, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO, 0, 0, new BigDecimal("21"), null, foto), 0, "t");
        assertEquals(foto, p.getImage());
        service.update(p.getId(), new ProductData("Con foto", "", null, null, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO, 0, 0, new BigDecimal("21"), null), "t");
        assertEquals(foto, service.get(p.getId()).getImage(), "sin imagen en la actualización no se pierde la foto");
        service.update(p.getId(), new ProductData("Con foto", "", null, null, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO, 0, 0, new BigDecimal("21"), null, ""), "t");
        assertEquals("", service.get(p.getId()).getImage(), "vacío la quita");
        assertThrows(BusinessException.class, () -> service.create(new ProductData("X", "", null, null, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO, 0, 0, new BigDecimal("21"), null, "http://x/y.png"), 0, "t"));
    }
}
