package com.pdv.catalog;

import com.pdv.catalog.CatalogService.ProductData;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/** API del catálogo: productos, combos, edición de stock, actualización masiva y libros de stock/precios. */
@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CatalogService service;
    public CatalogController(CatalogService s) { this.service = s; }

    public record ProductOut(Long id, String name, String barcode, Long categoryId, Long supplierId, BigDecimal cost, BigDecimal price,
                             BigDecimal offer, int stock, int lowStock, int idealStock, BigDecimal iva, boolean archived,
                             boolean combo, List<Product.ComboItem> components, String image) {}
    public record CreateIn(ProductData data, Integer initialStock) {}
    public record StockIn(int value) {}
    public record IdsIn(List<Long> ids, boolean flag) {}
    public record BulkPriceIn(List<Long> ids, boolean offer, boolean percent, BigDecimal value, boolean round) {}

    private ProductOut out(Product p) {
        return new ProductOut(p.getId(), p.getName(), p.getBarcode(), p.getCategoryId(), p.getSupplierId(), p.getCost(), p.getPrice(), p.getOffer(),
                service.stockOf(p), p.getLowStock(), p.getIdealStock(), p.getIva(), p.isArchived(), p.isCombo(), p.getCombo(), p.getImage());
    }

    @GetMapping("/products") public List<ProductOut> list(@RequestParam(defaultValue = "false") boolean archived) { return service.list(archived).stream().map(this::out).toList(); }
    @GetMapping("/products/{id}") public ProductOut get(@PathVariable Long id) { return out(service.get(id)); }
    @PostMapping("/products") public ProductOut create(@RequestBody CreateIn in) { return out(service.create(in.data(), in.initialStock() == null ? 0 : in.initialStock(), com.pdv.auth.CurrentUser.name())); }
    @PutMapping("/products/{id}") public ProductOut update(@PathVariable Long id, @RequestBody ProductData d) { return out(service.update(id, d, com.pdv.auth.CurrentUser.name())); }
    @PutMapping("/products/{id}/stock") public ProductOut setStock(@PathVariable Long id, @RequestBody StockIn in) { service.setStock(id, in.value(), com.pdv.auth.CurrentUser.name()); return out(service.get(id)); }
    @PostMapping("/products/archive") public void archive(@RequestBody IdsIn in) { service.archive(in.ids(), in.flag()); }
    @PostMapping("/products/delete") public void delete(@RequestBody IdsIn in) { service.delete(in.ids()); }
    @PostMapping("/products/bulk-price") public void bulk(@RequestBody BulkPriceIn in) { service.bulkPrice(in.ids(), in.offer(), in.percent(), in.value(), in.round(), com.pdv.auth.CurrentUser.name()); }

    @GetMapping("/stock-moves") public List<StockMove> stockMoves(@RequestParam(required = false) Long productId) { return service.stockMoves(productId); }
    @GetMapping("/price-changes") public List<PriceChange> priceChanges() { return service.priceChanges(); }
}
