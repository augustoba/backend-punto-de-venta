package com.pdv.billing;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/** Factura de prueba (Factura C, sin validez fiscal). La emisión fiscal real (ARCA) es una integración aparte. */
@Entity
@Table(name = "pos_invoice")
public class Invoice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(length = 20) private String number;
    private Instant occurredAt = Instant.now();
    private Long customerId;
    @Column(precision = 14, scale = 2) private BigDecimal total;
    @Column(precision = 14, scale = 2) private BigDecimal net;
    @Column(precision = 14, scale = 2) private BigDecimal iva;
    @Column(length = 500) private String items = "";
    @Column(length = 80) private String username = "";

    protected Invoice() {}
    public Invoice(String number, Long customerId, BigDecimal total, BigDecimal net, BigDecimal iva, String items) {
        this.number = number; this.customerId = customerId; this.total = total; this.net = net; this.iva = iva; this.items = items == null ? "" : items;
    }
    public Long getId() { return id; }
    public String getNumber() { return number; }
    public Instant getOccurredAt() { return occurredAt; }
    public Long getCustomerId() { return customerId; }
    public BigDecimal getTotal() { return total; }
    public BigDecimal getNet() { return net; }
    public BigDecimal getIva() { return iva; }
    public String getItems() { return items; }
    public String getUsername() { return username; }
    public void setUsername(String u) { this.username = u == null ? "" : u; }
}
