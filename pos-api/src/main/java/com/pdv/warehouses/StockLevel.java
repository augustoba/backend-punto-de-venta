package com.pdv.warehouses;

import jakarta.persistence.*;

/** Cantidad de un producto en un depósito que no es el principal. */
@Entity
@Table(name = "pos_stock_level", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "warehouse_id"}))
public class StockLevel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "warehouse_id", nullable = false) private Long warehouseId;
    private int qty;

    protected StockLevel() {}
    public StockLevel(Long productId, Long warehouseId, int qty) { this.productId = productId; this.warehouseId = warehouseId; this.qty = qty; }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public Long getWarehouseId() { return warehouseId; }
    public int getQty() { return qty; }
    public void setQty(int q) { this.qty = q; }
}
