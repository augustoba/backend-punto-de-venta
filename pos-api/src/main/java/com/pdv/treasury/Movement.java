package com.pdv.treasury;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/** Fila del libro de movimientos de dinero. amount: positivo = ingreso, negativo = egreso. */
@Entity
@Table(name = "pos_movement", indexes = @Index(name = "ix_pos_movement_account", columnList = "accountId"))
public class Movement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long accountId;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false)
    private Instant occurredAt;
    @Column(length = 120) private String category = "";
    @Column(length = 120) private String subcategory = "";
    @Column(length = 500) private String description = "";
    /** Qué lo originó: manual, venta, apertura, cierre, transferencia, cc-cliente, cc-proveedor, compra, saldo. */
    @Column(length = 30) private String sourceType = "manual";
    @Column(length = 40) private String sourceId = "";
    @Column(length = 80) private String username = "";

    protected Movement() {}
    public Movement(Long accountId, BigDecimal amount, Instant at, String category, String subcategory,
                    String description, String sourceType, String sourceId, String username) {
        this.accountId = accountId; this.amount = amount; this.occurredAt = at;
        this.category = nz(category); this.subcategory = nz(subcategory); this.description = nz(description);
        this.sourceType = nz(sourceType); this.sourceId = nz(sourceId); this.username = nz(username);
    }
    private static String nz(String s) { return s == null ? "" : s; }
    public Long getId() { return id; }
    public Long getAccountId() { return accountId; }
    public BigDecimal getAmount() { return amount; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getCategory() { return category; }
    public String getSubcategory() { return subcategory; }
    public String getDescription() { return description; }
    public String getSourceType() { return sourceType; }
    public String getSourceId() { return sourceId; }
    public String getUsername() { return username; }
}
