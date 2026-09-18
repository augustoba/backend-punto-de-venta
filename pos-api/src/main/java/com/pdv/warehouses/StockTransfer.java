package com.pdv.warehouses;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Traslado de mercadería entre depósitos: no cambia el stock total, sólo dónde está. */
@Entity
@Table(name = "pos_stock_transfer")
public class StockTransfer {
    @Embeddable
    public static class Line {
        public Long productId;
        @Column(length = 200) public String name;
        public int qty;
        public Line() {}
        public Line(Long productId, String name, int qty) { this.productId = productId; this.name = name; this.qty = qty; }
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Instant occurredAt = Instant.now();
    @Column(nullable = false) private Long fromId;
    @Column(nullable = false) private Long toId;
    @Column(length = 80) private String username = "";
    @Column(length = 300) private String notes = "";
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pos_stock_transfer_line", joinColumns = @JoinColumn(name = "transfer_id"))
    private List<Line> lines = new ArrayList<>();

    protected StockTransfer() {}
    public StockTransfer(Long fromId, Long toId, String username, String notes, List<Line> lines) {
        this.fromId = fromId; this.toId = toId; this.username = username; this.notes = notes == null ? "" : notes; this.lines = lines;
    }

    public Long getId() { return id; }
    public long getNumber() { return id == null ? 0 : id; }
    public Instant getOccurredAt() { return occurredAt; }
    public Long getFromId() { return fromId; }
    public Long getToId() { return toId; }
    public String getUsername() { return username; }
    public String getNotes() { return notes; }
    public List<Line> getLines() { return lines; }
}
