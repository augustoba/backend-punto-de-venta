package com.pdv.finance;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Cheque a cobrar (recibido) o a pagar (entregado). */
@Entity
@Table(name = "pos_cheque")
public class Cheque {
    public enum Kind { COBRAR, PAGAR }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING) @Column(length = 10, nullable = false) private Kind kind = Kind.COBRAR;
    @Column(precision = 14, scale = 2, nullable = false) private BigDecimal amount;
    @Column(nullable = false) private LocalDate due;
    @Column(length = 20) private String number = "";
    private boolean collected;
    private Long customerId;
    @Column(length = 300) private String description = "";

    public Long getId() { return id; }
    public Kind getKind() { return kind; }
    public void setKind(Kind v) { kind = v; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal v) { amount = v; }
    public LocalDate getDue() { return due; }
    public void setDue(LocalDate v) { due = v; }
    public String getNumber() { return number; }
    public void setNumber(String v) { number = v; }
    public boolean isCollected() { return collected; }
    public void setCollected(boolean v) { collected = v; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long v) { customerId = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v == null ? "" : v; }
}
