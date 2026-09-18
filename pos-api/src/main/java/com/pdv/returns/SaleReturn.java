package com.pdv.returns;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Devolución (total o parcial) de una venta. Genera una nota de crédito de prueba (sin validez fiscal). */
@Entity
@Table(name = "pos_sale_return")
public class SaleReturn {
    /** Cómo se compensa al cliente. CAMBIO = sólo vuelve la mercadería; lo nuevo se cobra en otra venta. */
    public enum Refund { EFECTIVO, TRANSFERENCIA, TARJETA, CUENTA_CORRIENTE, CAMBIO }

    @Embeddable
    public static class Line {
        public Long productId;
        @Column(length = 200) public String name;
        public int qty;
        @Column(precision = 14, scale = 2) public BigDecimal unitPrice;   // neto de descuentos y recargos de la venta
        @Column(precision = 14, scale = 2) public BigDecimal unitCost;
        /** true = la mercadería vuelve al stock; false = se descarta (merma). */
        public boolean restock;
        public Line() {}
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long saleId;
    private Instant occurredAt = Instant.now();
    @Column(length = 80) private String username = "";
    @Column(length = 300) private String reason = "";
    @Enumerated(EnumType.STRING) @Column(length = 20) private Refund refund;
    @Column(precision = 14, scale = 2) private BigDecimal total = BigDecimal.ZERO;
    @Column(length = 24) private String creditNote = "";
    private Long customerId;
    private Long accountId;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pos_sale_return_line", joinColumns = @JoinColumn(name = "return_id"))
    private List<Line> lines = new ArrayList<>();

    protected SaleReturn() {}
    public SaleReturn(Long saleId, String username, String reason, Refund refund, Long customerId) {
        this.saleId = saleId; this.username = username; this.reason = reason == null ? "" : reason; this.refund = refund; this.customerId = customerId;
    }

    public Long getId() { return id; }
    public long getNumber() { return id == null ? 0 : id; }
    public Long getSaleId() { return saleId; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getUsername() { return username; }
    public String getReason() { return reason; }
    public Refund getRefund() { return refund; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal t) { total = t; }
    public String getCreditNote() { return creditNote; }
    public void setCreditNote(String n) { creditNote = n; }
    public Long getCustomerId() { return customerId; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long a) { accountId = a; }
    public List<Line> getLines() { return lines; }
}
