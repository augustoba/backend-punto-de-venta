package com.pdv.parties;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "pos_supplier")
public class Supplier {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 160) private String name;
    @Column(length = 20) private String cuit = "";
    @Column(length = 80) private String trade = "";
    @Column(length = 120) private String contact = "";
    @Column(length = 40) private String phone = "";
    @Column(length = 160) private String email = "";
    @Column(length = 200) private String address = "";
    @Column(length = 80) private String city = "";
    /** Porcentaje de remarcación sugerido sobre el costo. */
    @Column(precision = 6, scale = 2) private BigDecimal markup = BigDecimal.ZERO;
    @Column(length = 500) private String notes = "";
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getCuit() { return cuit; }
    public void setCuit(String v) { cuit = nz(v); }
    public String getTrade() { return trade; }
    public void setTrade(String v) { trade = nz(v); }
    public String getContact() { return contact; }
    public void setContact(String v) { contact = nz(v); }
    public String getPhone() { return phone; }
    public void setPhone(String v) { phone = nz(v); }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = nz(v); }
    public String getAddress() { return address; }
    public void setAddress(String v) { address = nz(v); }
    public String getCity() { return city; }
    public void setCity(String v) { city = nz(v); }
    public BigDecimal getMarkup() { return markup; }
    public void setMarkup(BigDecimal v) { markup = v == null ? BigDecimal.ZERO : v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { notes = nz(v); }
    public Instant getCreatedAt() { return createdAt; }
    private static String nz(String s) { return s == null ? "" : s; }
}
