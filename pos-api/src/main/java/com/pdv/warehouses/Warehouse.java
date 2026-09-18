package com.pdv.warehouses;

import jakarta.persistence.*;

/** Depósito. El «Principal» (main) no guarda filas: su stock es el total del producto menos lo que está en los demás. */
@Entity
@Table(name = "pos_warehouse")
public class Warehouse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String name;
    private boolean main;

    protected Warehouse() {}
    public Warehouse(String name, boolean main) { this.name = name; this.main = main; }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public boolean isMain() { return main; }
}
