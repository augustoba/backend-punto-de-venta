package com.pdv.sales;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/** Turno de caja (arqueo): apertura con saldo declarado y cierre comparando lo esperado contra lo contado. */
@Entity
@Table(name = "pos_cash_session")
public class CashSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long accountId;
    @Column(length = 80) private String username = "";
    private Instant openedAt = Instant.now();
    @Column(precision = 14, scale = 2) private BigDecimal openingBalance = BigDecimal.ZERO;
    @Column(precision = 14, scale = 2) private BigDecimal previousClose = BigDecimal.ZERO;
    @Column(length = 500) private String notesOpen = "";
    private Instant closedAt;
    @Column(precision = 14, scale = 2) private BigDecimal expected;
    @Column(precision = 14, scale = 2) private BigDecimal real;
    @Column(precision = 14, scale = 2) private BigDecimal diff;
    @Column(length = 500) private String notesClose = "";
    private boolean verified;
    @Column(length = 500) private String note = "";

    protected CashSession() {}
    public CashSession(Long accountId, String username, BigDecimal opening, BigDecimal previousClose, String notes) {
        this.accountId = accountId; this.username = username; this.openingBalance = opening; this.previousClose = previousClose;
        this.notesOpen = notes == null ? "" : notes;
    }
    public void close(BigDecimal expected, BigDecimal real, String notes) {
        this.closedAt = Instant.now(); this.expected = expected; this.real = real; this.diff = real.subtract(expected);
        this.notesClose = notes == null ? "" : notes;
    }
    public Long getId() { return id; }
    public Long getAccountId() { return accountId; }
    public String getUsername() { return username; }
    public Instant getOpenedAt() { return openedAt; }
    public BigDecimal getOpeningBalance() { return openingBalance; }
    public BigDecimal getPreviousClose() { return previousClose; }
    public String getNotesOpen() { return notesOpen; }
    public Instant getClosedAt() { return closedAt; }
    public BigDecimal getExpected() { return expected; }
    public BigDecimal getReal() { return real; }
    public BigDecimal getDiff() { return diff; }
    public String getNotesClose() { return notesClose; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean v) { verified = v; }
    public String getNote() { return note; }
    public void setNote(String v) { note = v == null ? "" : v; }
}
