package com.pdv.purchasing;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Pedido de compra a un proveedor: Borrador → Pedido → Recibido. El stock y el costo solo cambian al recibir. */
@Entity
@Table(name = "pos_purchase")
public class Purchase {
    public enum Status { BORRADOR, PEDIDO, RECIBIDO }

    @Embeddable
    public static class Line {
        public Long productId;
        @Column(length = 200) public String name;
        public int qty;
        @Column(precision = 14, scale = 2) public BigDecimal cost;
        @Column(precision = 14, scale = 2) public BigDecimal prevCost = BigDecimal.ZERO;
        public int received;
        public Line() {}
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long supplierId;
    @Enumerated(EnumType.STRING) @Column(length = 12) private Status status = Status.BORRADOR;
    @Column(precision = 14, scale = 2) private BigDecimal total = BigDecimal.ZERO;
    private Instant createdAt = Instant.now();
    private Instant receivedAt;
    private boolean paid;
    @Column(length = 160) private String name = "";
    @Column(length = 80) private String username = "";
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pos_purchase_line", joinColumns = @JoinColumn(name = "purchase_id"))
    private List<Line> lines = new ArrayList<>();

    public Long getId() { return id; }
    public long getNumber() { return id == null ? 0 : id; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long v) { supplierId = v; }
    public Status getStatus() { return status; }
    public void setStatus(Status v) { status = v; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal v) { total = v; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant v) { receivedAt = v; }
    public boolean isPaid() { return paid; }
    public void setPaid(boolean v) { paid = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v == null ? "" : v; }
    public String getUsername() { return username; }
    public void setUsername(String v) { username = v; }
    public List<Line> getLines() { return lines; }
}
