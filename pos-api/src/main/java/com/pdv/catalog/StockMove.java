package com.pdv.catalog;

import jakarta.persistence.*;

import java.time.Instant;

/** Fila del libro de stock: todo cambio de stock deja una (creación, venta, compra recibida, edición manual). */
@Entity
@Table(name = "pos_stock_move", indexes = @Index(name = "ix_pos_stock_move_product", columnList = "productId"))
public class StockMove {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long productId;
    private Instant occurredAt = Instant.now();
    private int prev;
    private int delta;
    private int result;
    @Column(length = 40) private String reason;
    @Column(length = 60) private String ref = "";
    @Column(length = 80) private String username = "";

    protected StockMove() {}
    public StockMove(Long productId, int prev, int delta, String reason, String ref, String username) {
        this.productId = productId; this.prev = prev; this.delta = delta; this.result = prev + delta;
        this.reason = reason; this.ref = ref == null ? "" : ref; this.username = username == null ? "" : username;
    }
    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public Instant getOccurredAt() { return occurredAt; }
    public int getPrev() { return prev; }
    public int getDelta() { return delta; }
    public int getResult() { return result; }
    public String getReason() { return reason; }
    public String getRef() { return ref; }
    public String getUsername() { return username; }
}
