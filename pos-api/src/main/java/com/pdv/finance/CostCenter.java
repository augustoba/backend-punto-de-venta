package com.pdv.finance;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Centro de costos: agrupa costos fijos y define cómo se reparten (alimenta la rentabilidad por categoría). */
@Entity
@Table(name = "pos_cost_center")
public class CostCenter {
    public enum Allocation { IGUALES, HORAS, FACTURACION, MANUAL }

    @Embeddable
    public static class FixedCost {
        @Column(length = 160) public String name;
        @Column(precision = 14, scale = 2) public BigDecimal monthly = BigDecimal.ZERO;
        @Column(length = 300) public String notes = "";
        public FixedCost() {}
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 160) private String name;
    @Enumerated(EnumType.STRING) @Column(length = 12) private Allocation allocation = Allocation.HORAS;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pos_fixed_cost", joinColumns = @JoinColumn(name = "center_id"))
    private List<FixedCost> costs = new ArrayList<>();

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public Allocation getAllocation() { return allocation; }
    public void setAllocation(Allocation v) { allocation = v; }
    public List<FixedCost> getCosts() { return costs; }
    public BigDecimal getMonthlyTotal() { return costs.stream().map(c -> c.monthly).reduce(BigDecimal.ZERO, BigDecimal::add); }
}
