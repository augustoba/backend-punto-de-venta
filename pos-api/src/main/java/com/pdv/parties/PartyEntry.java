package com.pdv.parties;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/** Fila del libro de cuenta corriente. delta > 0 aumenta la deuda; delta < 0 la baja. El saldo es la suma. */
@Entity
@Table(name = "pos_party_entry", indexes = @Index(name = "ix_pos_party_entry", columnList = "party,partyId"))
public class PartyEntry {
    public enum Party { CUSTOMER, SUPPLIER }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10) private Party party;
    @Column(nullable = false) private Long partyId;
    private Instant occurredAt = Instant.now();
    /** venta | pago | devolucion | deuda | compra | nota_credito | ajuste */
    @Column(length = 20) private String kind;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal delta;
    private Long accountId;
    @Column(length = 500) private String comment = "";
    @Column(length = 40) private String ref = "";
    @Column(length = 80) private String username = "";

    protected PartyEntry() {}
    public PartyEntry(Party party, Long partyId, String kind, BigDecimal delta, Long accountId, String comment, String ref, String username) {
        this.party = party; this.partyId = partyId; this.kind = kind; this.delta = delta; this.accountId = accountId;
        this.comment = comment == null ? "" : comment; this.ref = ref == null ? "" : ref; this.username = username == null ? "" : username;
    }
    public Long getId() { return id; }
    public Party getParty() { return party; }
    public Long getPartyId() { return partyId; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getKind() { return kind; }
    public BigDecimal getDelta() { return delta; }
    public Long getAccountId() { return accountId; }
    public String getComment() { return comment; }
    public String getRef() { return ref; }
    public String getUsername() { return username; }
}
