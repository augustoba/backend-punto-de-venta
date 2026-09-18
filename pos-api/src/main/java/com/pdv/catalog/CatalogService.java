package com.pdv.catalog;

import com.pdv.common.BusinessException;
import com.pdv.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Reglas del catálogo (ver referencia-envi/ANALISIS_stock.md): libro de stock, combos, historial de precios. */
@Service
@Transactional
public class CatalogService {
    public static final String R_CREATE = "Creación de producto";
    public static final String R_SALE = "Venta en caja";
    public static final String R_PURCHASE = "Compra recibida";
    public static final String R_MANUAL = "Actualización manual";

    private final ProductRepository products;
    private final StockMoveRepository moves;
    private final PriceChangeRepository prices;

    public CatalogService(ProductRepository p, StockMoveRepository m, PriceChangeRepository c) { this.products = p; this.moves = m; this.prices = c; }

    public record ProductData(String name, String barcode, Long categoryId, Long supplierId, BigDecimal cost, BigDecimal price,
                              BigDecimal offer, Integer lowStock, Integer idealStock, BigDecimal iva, List<Product.ComboItem> combo, String image) {
        /** Sin foto: en la actualización deja la que ya tenía. */
        public ProductData(String name, String barcode, Long categoryId, Long supplierId, BigDecimal cost, BigDecimal price,
                           BigDecimal offer, Integer lowStock, Integer idealStock, BigDecimal iva, List<Product.ComboItem> combo) {
            this(name, barcode, categoryId, supplierId, cost, price, offer, lowStock, idealStock, iva, combo, null);
        }
    }

    /** Sólo imágenes en data URL (PNG/JPG/WebP) y de tamaño acotado; el front las reduce antes de subirlas. */
    static String validImage(String image) {
        if (image == null || image.isBlank()) return "";
        if (!image.startsWith("data:image/png;base64,") && !image.startsWith("data:image/jpeg;base64,") && !image.startsWith("data:image/webp;base64,"))
            throw new BusinessException("La foto debe ser una imagen PNG, JPG o WebP");
        if (image.length() > 250_000) throw new BusinessException("La foto es demasiado pesada (máximo ~180 KB)");
        return image;
    }

    public Product get(Long id) { return products.findById(id).orElseThrow(() -> new NotFoundException("Producto", id)); }
    public List<Product> list(boolean archived) { return products.findByArchivedOrderByIdDesc(archived); }

    /** Stock efectivo: los combos lo derivan del componente más escaso. */
    public int stockOf(Product p) {
        if (!p.isCombo()) return p.getStock();
        int min = Integer.MAX_VALUE;
        for (Product.ComboItem c : p.getCombo()) min = Math.min(min, stockOf(get(c.productId)) / Math.max(1, c.qty));
        return min == Integer.MAX_VALUE ? 0 : min;
    }

    public Product create(ProductData d, int initialStock, String user) {
        if (d.name() == null || d.name().isBlank()) throw new BusinessException("El producto necesita un nombre");
        Product p = new Product();
        apply(p, d);
        p = products.save(p);
        if (initialStock != 0 && !p.isCombo()) move(p, initialStock, R_CREATE, "", user);
        return p;
    }

    public Product update(Long id, ProductData d, String user) {
        Product p = get(id);
        logPrice(p, "price", p.getPrice(), d.price(), user);
        logPrice(p, "cost", p.getCost(), d.cost(), user);
        logPrice(p, "offer", p.getOffer(), d.offer(), user);
        apply(p, d);
        return p;
    }

    private void apply(Product p, ProductData d) {
        p.setName(d.name().trim());
        p.setBarcode(d.barcode());
        p.setCategoryId(d.categoryId());
        p.setSupplierId(d.supplierId());
        if (d.cost() != null) p.setCost(d.cost());
        if (d.price() != null) p.setPrice(d.price());
        if (d.offer() != null) p.setOffer(d.offer());
        if (d.lowStock() != null) p.setLowStock(d.lowStock());
        if (d.idealStock() != null) p.setIdealStock(d.idealStock());
        if (d.iva() != null) p.setIva(d.iva());
        if (d.combo() != null) { p.getCombo().clear(); p.getCombo().addAll(d.combo()); }
        if (d.image() != null) p.setImage(validImage(d.image()));   // null = no tocar; "" = quitar
    }

    private void logPrice(Product p, String field, BigDecimal prev, BigDecimal next, String user) {
        if (next != null && prev.compareTo(next) != 0) prices.save(new PriceChange(p.getId(), field, prev, next, user));
    }

    /** Único punto por el que cambia el stock: deja siempre una fila en el libro. */
    public void move(Product p, int delta, String reason, String ref, String user) {
        if (delta == 0) return;
        moves.save(new StockMove(p.getId(), p.getStock(), delta, reason, ref, user));
        p.setStock(p.getStock() + delta);
    }

    /** Cambia el costo (por ejemplo, al recibir una compra) y deja rastro en el historial de precios. */
    public void setCost(Product p, BigDecimal cost, String user) {
        logPrice(p, "cost", p.getCost(), cost, user);
        p.setCost(cost);
    }

    /** Baja de stock por una venta; en un combo descuenta cada componente. */
    public void consume(Long productId, int qty, String reason, String ref, String user) {
        Product p = get(productId);
        if (p.isCombo()) p.getCombo().forEach(c -> consume(c.productId, c.qty * qty, reason, ref, user));
        else move(p, -qty, reason, ref, user);
    }

    /** Edición manual (edición en línea del listado): el combo no se edita a mano. */
    public void setStock(Long id, int value, String user) {
        Product p = get(id);
        if (p.isCombo()) throw new BusinessException("El stock de un combo se deriva de sus componentes");
        move(p, value - p.getStock(), R_MANUAL, "", user);
    }

    public void archive(List<Long> ids, boolean archived) { ids.forEach(i -> get(i).setArchived(archived)); }
    public void delete(List<Long> ids) { products.deleteAllById(ids); }

    /** Actualización masiva de precios (por porcentaje o monto fijo; opcionalmente redondeada al peso). */
    public void bulkPrice(List<Long> ids, boolean offer, boolean percent, BigDecimal value, boolean round, String user) {
        for (Long id : ids) {
            Product p = get(id);
            BigDecimal prev = offer ? p.getOffer() : p.getPrice();
            if (offer && prev.signum() == 0) continue;
            BigDecimal next = percent ? prev.multiply(BigDecimal.ONE.add(value.movePointLeft(2))) : prev.add(value);
            next = round ? next.setScale(0, RoundingMode.HALF_UP) : next.setScale(2, RoundingMode.HALF_UP);
            if (next.signum() < 0) next = BigDecimal.ZERO;
            logPrice(p, offer ? "offer" : "price", prev, next, user);
            if (offer) p.setOffer(next); else p.setPrice(next);
        }
    }

    public List<StockMove> stockMoves(Long productId) {
        return productId == null ? moves.findAllByOrderByOccurredAtDescIdDesc() : moves.findByProductIdOrderByOccurredAtDescIdDesc(productId);
    }
    public List<PriceChange> priceChanges() { return prices.findAllByOrderByOccurredAtDescIdDesc(); }
}
