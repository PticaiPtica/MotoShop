package ru.academy.homework.motoshop.model;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import java.util.List;

@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> children;

    public Category(long id, String name, Category parent, List<Category> children) {
        this.id = id;
        this.name = name;
        this.parent = parent;
        this.children = children;
    }
    public Category() {}

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Category getParentCategory() { return parentCategory; }
    public void setParentCategory(Category parentCategory) { this.parentCategory = parentCategory; }

    public List<Category> getSubcategories() { return subcategories; }
    public void setSubcategories(List<Category> subcategories) { this.subcategories = subcategories; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    // Вспомогательные методы
    public void addSubcategory(Category subcategory) {
        subcategories.add(subcategory);
        subcategory.setParentCategory(this);
    }

    public void removeSubcategory(Category subcategory) {
        subcategories.remove(subcategory);
        subcategory.setParentCategory(null);
    }

    public void addProduct(Product product) {
        products.add(product);
        product.setCategory(this);
    }

    public void removeProduct(Product product) {
        products.remove(product);
        product.setCategory(null);
    }
}