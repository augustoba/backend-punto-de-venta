package com.pdv.staff;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "pos_employee")
public class Employee {
    public enum Role { ADMIN, VENDEDOR }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String name;
    @Column(length = 120) private String lastName = "";
    @Column(length = 160) private String email = "";
    @Enumerated(EnumType.STRING) @Column(length = 10) private Role role = Role.VENDEDOR;
    private LocalDate hired = LocalDate.now();

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { lastName = v == null ? "" : v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = v == null ? "" : v; }
    public Role getRole() { return role; }
    public void setRole(Role v) { role = v == null ? Role.VENDEDOR : v; }
    public LocalDate getHired() { return hired; }
    public void setHired(LocalDate v) { hired = v == null ? LocalDate.now() : v; }
}
