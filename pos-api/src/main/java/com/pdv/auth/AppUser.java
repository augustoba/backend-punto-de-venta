package com.pdv.auth;

import jakarta.persistence.*;

/** Usuario que inicia sesión en el POS. La contraseña se guarda sólo como hash PBKDF2 con sal. */
@Entity
@Table(name = "pos_user")
public class AppUser {
    public enum Role { ADMIN, VENDEDOR }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 60) private String username;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(nullable = false, length = 200) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10) private Role role = Role.VENDEDOR;
    private boolean active = true;

    protected AppUser() {}
    public AppUser(String username, String passwordHash, Role role) { this.username = username; this.passwordHash = passwordHash; this.role = role; }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String h) { this.passwordHash = h; }
    public Role getRole() { return role; }
    public void setRole(Role r) { this.role = r; }
    public boolean isActive() { return active; }
    public void setActive(boolean a) { this.active = a; }
}
