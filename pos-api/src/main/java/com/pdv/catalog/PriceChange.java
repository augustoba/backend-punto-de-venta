package com.pdv.catalog;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/** Historial de precios: cada cambio de precio, costo u oferta de un producto. */
@Entity
@Table(name = "pos_price_change")
public class PriceChange {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long productId;
    private Instant occurredAt = Instant.now();
    @Column(length = 10) private String field;
    @Column(precision = 14, scale = 2) private BigDecimal prev;
    @Column(precision = 14, scale = 2) private BigDecimal next;
    @Column(length = 80) private String username = "";

    protected PriceChange() {}
    public PriceChange(Long productId, String field, BigDecimal prev, BigDecimal next, String username) {
        this.productId = productId; this.field = field; this.prev = prev; this.next = next; this.username = username == null ? "" : username;
    }
    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getField() { return field; }
    public BigDecimal getPrev() { return prev; }
    public BigDecimal getNext() { return next; }
    public String getUsername() { return username; }
}
