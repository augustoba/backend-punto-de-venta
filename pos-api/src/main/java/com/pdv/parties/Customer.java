package com.pdv.parties;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "pos_customer")
public class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String name;
    @Column(length = 120) private String lastName = "";
    @Column(length = 40) private String phone = "";
    @Column(length = 160) private String email = "";
    @Column(length = 20) private String dni = "";
    @Column(length = 20) private String cuit = "";
    @Column(length = 500) private String notes = "";
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { lastName = nz(v); }
    public String getPhone() { return phone; }
    public void setPhone(String v) { phone = nz(v); }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = nz(v); }
    public String getDni() { return dni; }
    public void setDni(String v) { dni = nz(v); }
    public String getCuit() { return cuit; }
    public void setCuit(String v) { cuit = nz(v); }
    public String getNotes() { return notes; }
    public void setNotes(String v) { notes = nz(v); }
    public Instant getCreatedAt() { return createdAt; }
    private static String nz(String s) { return s == null ? "" : s; }
}
