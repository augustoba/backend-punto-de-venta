package com.pdv.billing;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Presupuesto: se convierte en venta desde la caja (al cobrar pasa a VENDIDO). Los precios incluyen IVA. */
@Entity
@Table(name = "pos_budget")
public class Budget {
    public enum Status { ACTIVO, VENDIDO, RECHAZADO, ARCHIVADO }

    @Embeddable
    public static class Line {
        public Long productId;
        @Column(length = 200) public String name;
        public int qty;
        @Column(precision = 14, scale = 2) public BigDecimal price;
        public Line() {}
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long customerId;
    private Instant createdAt = Instant.now();
    private LocalDate expires;
    @Column(precision = 14, scale = 2) private BigDecimal total = BigDecimal.ZERO;
    @Column(length = 500) private String notes = "";
    @Enumerated(EnumType.STRING) @Column(length = 12) private Status status = Status.ACTIVO;
    @Column(length = 80) private String username = "";
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pos_budget_line", joinColumns = @JoinColumn(name = "budget_id"))
    private List<Line> lines = new ArrayList<>();

    public Long getId() { return id; }
    public long getNumber() { return id == null ? 0 : id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long v) { customerId = v; }
    public Instant getCreatedAt() { return createdAt; }
    public LocalDate getExpires() { return expires; }
    public void setExpires(LocalDate v) { expires = v; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal v) { total = v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { notes = v == null ? "" : v; }
    public Status getStatus() { return status; }
    public void setStatus(Status v) { status = v; }
    public String getUsername() { return username; }
    public void setUsername(String v) { username = v; }
    public List<Line> getLines() { return lines; }
    public boolean isExpired() { return status == Status.ACTIVO && expires != null && expires.isBefore(LocalDate.now()); }
}
