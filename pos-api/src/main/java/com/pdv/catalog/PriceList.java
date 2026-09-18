package com.pdv.catalog;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** Lista de precios: ajuste porcentual sobre el precio base del producto (negativo = descuento, p. ej. Mayorista −15). */
@Entity
@Table(name = "pos_price_list")
public class PriceList {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 80) private String name;
    @Column(name = "adjust_pct", precision = 6, scale = 2) private BigDecimal percent = BigDecimal.ZERO;
    private boolean main;

    protected PriceList() {}
    public PriceList(String name, BigDecimal percent, boolean main) { this.name = name; this.percent = percent; this.main = main; }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public BigDecimal getPercent() { return percent; }
    public void setPercent(BigDecimal p) { this.percent = p; }
    public boolean isMain() { return main; }
}
