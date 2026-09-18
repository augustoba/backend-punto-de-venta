package com.pdv.treasury;

import jakarta.persistence.*;

/** Cuenta de dinero: una caja física o una cuenta bancaria. El saldo NO se guarda: es la suma de sus movimientos. */
@Entity
@Table(name = "pos_account")
public class Account {
    public enum Type { CAJA, BANCO }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10)
    private Type type = Type.CAJA;
    @Column(length = 12)
    private String color = "#8fc24a";
    @Column(length = 500)
    private String notes = "";

    protected Account() {}
    public Account(String name, Type type, String color, String notes) {
        this.name = name; this.type = type; if (color != null) this.color = color; if (notes != null) this.notes = notes;
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
