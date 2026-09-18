package com.pdv.sales;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Venta. Las líneas congelan precio, descuento y costo del momento (los reportes no cambian si luego cambia el producto). */
@Entity
@Table(name = "pos_sale")
public class Sale {
    public enum Method { EFECTIVO, TRANSFERENCIA, TARJETA, CUENTA_CORRIENTE }

    @Embeddable
    public static class Line {
        public Long productId;
        @Column(length = 200) public String name;
        public int qty;
        @Column(precision = 14, scale = 2) public BigDecimal price;
        @Column(precision = 14, scale = 2) public BigDecimal discountUnit = BigDecimal.ZERO;
        @Column(precision = 14, scale = 2) public BigDecimal cost = BigDecimal.ZERO;
        public Long categoryId;
        public Line() {}
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Instant occurredAt = Instant.now();
    @Column(length = 80) private String seller = "";
    private Long customerId;
    @Column(precision = 14, scale = 2) private BigDecimal subtotal = BigDecimal.ZERO;
    @Column(precision = 5, scale = 2) private BigDecimal discountPct = BigDecimal.ZERO;
    @Column(precision = 14, scale = 2) private BigDecimal discountAmount = BigDecimal.ZERO;
    @Column(precision = 14, scale = 2) private BigDecimal total = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING) @Column(length = 20) private Method method = Method.EFECTIVO;
    private boolean paid = true;
    private Long accountId;
    @Column(length = 500) private String notes = "";
    private boolean invoiced;
    private Long budgetId;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pos_sale_line", joinColumns = @JoinColumn(name = "sale_id"))
    private List<Line> lines = new ArrayList<>();

    public Long getId() { return id; }
    /** El número visible de la venta es su id. */
    public long getNumber() { return id == null ? 0 : id; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getSeller() { return seller; }
    public void setSeller(String v) { seller = v; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long v) { customerId = v; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal v) { subtotal = v; }
    public BigDecimal getDiscountPct() { return discountPct; }
    public void setDiscountPct(BigDecimal v) { discountPct = v; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal v) { discountAmount = v; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal v) { total = v; }
    public Method getMethod() { return method; }
    public void setMethod(Method v) { method = v; }
    public boolean isPaid() { return paid; }
    public void setPaid(boolean v) { paid = v; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long v) { accountId = v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { notes = v == null ? "" : v; }
    public boolean isInvoiced() { return invoiced; }
    public void setInvoiced(boolean v) { invoiced = v; }
    public Long getBudgetId() { return budgetId; }
    public void setBudgetId(Long v) { budgetId = v; }
    public List<Line> getLines() { return lines; }
}
