package com.pdv.catalog;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Producto. Si tiene componentes es un combo: su stock se deriva de ellos y no se edita. El precio incluye IVA. */
@Entity
@Table(name = "pos_product")
public class Product {
    @Embeddable
    public static class ComboItem {
        @Column(name = "component_id") public Long productId;
        @Column(name = "qty") public int qty = 1;
        public ComboItem() {}
        public ComboItem(Long productId, int qty) { this.productId = productId; this.qty = qty; }
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 200) private String name;
    @Column(length = 60) private String barcode = "";
    private Long categoryId;
    private Long supplierId;
    @Column(precision = 14, scale = 2) private BigDecimal cost = BigDecimal.ZERO;
    @Column(precision = 14, scale = 2) private BigDecimal price = BigDecimal.ZERO;
    @Column(precision = 14, scale = 2) private BigDecimal offer = BigDecimal.ZERO;
    /** Se permite negativo (venta sin stock). */
    private int stock;
    private int lowStock;
    private int idealStock;
    @Column(precision = 5, scale = 2) private BigDecimal iva = new BigDecimal("21");
    private boolean archived;
    private Instant createdAt = Instant.now();
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pos_product_combo", joinColumns = @JoinColumn(name = "product_id"))
    private List<ComboItem> combo = new ArrayList<>();

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String v) { barcode = v == null ? "" : v; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long v) { categoryId = v; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long v) { supplierId = v; }
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal v) { cost = v; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal v) { price = v; }
    public BigDecimal getOffer() { return offer; }
    public void setOffer(BigDecimal v) { offer = v; }
    public int getStock() { return stock; }
    public void setStock(int v) { stock = v; }
    public int getLowStock() { return lowStock; }
    public void setLowStock(int v) { lowStock = v; }
    public int getIdealStock() { return idealStock; }
    public void setIdealStock(int v) { idealStock = v; }
    public BigDecimal getIva() { return iva; }
    public void setIva(BigDecimal v) { iva = v; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean v) { archived = v; }
    public Instant getCreatedAt() { return createdAt; }
    public List<ComboItem> getCombo() { return combo; }
    public boolean isCombo() { return !combo.isEmpty(); }
}
