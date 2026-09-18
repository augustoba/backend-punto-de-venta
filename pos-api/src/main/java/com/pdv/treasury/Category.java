package com.pdv.treasury;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/** Categoría con subcategorías; la comparten el catálogo y la tesorería. */
@Entity
@Table(name = "pos_category")
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120) private String name;
    @Column(length = 16) private String emoji = "";
    @Column(length = 12) private String color = "#888888";
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pos_subcategory", joinColumns = @JoinColumn(name = "category_id"))
    @Column(name = "name", length = 120)
    private List<String> subcategories = new ArrayList<>();

    protected Category() {}
    public Category(String name, String emoji, String color, List<String> subs) {
        this.name = name; this.emoji = emoji; this.color = color; if (subs != null) this.subcategories = new ArrayList<>(subs);
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String e) { this.emoji = e; }
    public String getColor() { return color; }
    public void setColor(String c) { this.color = c; }
    public List<String> getSubcategories() { return subcategories; }
}
