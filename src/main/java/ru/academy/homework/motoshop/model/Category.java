package ru.academy.homework.motoshop.model;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Сущность категории товаров.
 * Поддерживает древовидную структуру с возможностью вложенности
 */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "name", nullable = false, length = 100)
    private String name;


    @Column(name = "description", length = 500)
    private String description;

    // Родительская категория (для построения дерева)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    // Подкатегории (дочерние категории)
    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Category> subcategories = new ArrayList<>();

    // Товары в этой категории
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    // Служебные поля
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "image_url")
    private String imageUrl;

    // ========== КОНСТРУКТОРЫ ==========

    public Category() {
        // Конструктор по умолчанию для JPA
    }

    public Category(String name) {
        this.name = name;
    }

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Category(String name, String description, Category parentCategory) {
        this.name = name;
        this.description = description;
        this.parentCategory = parentCategory;
    }

    // ========== БИЗНЕС-МЕТОДЫ ==========

    /**
     * Добавить подкатегорию
     * @param subcategory дочерняя категория
     */
    public void addSubcategory(Category subcategory) {
        subcategories.add(subcategory);
        subcategory.setParentCategory(this);
    }

    /**
     * Удалить подкатегорию
     * @param subcategory дочерняя категория
     */
    public void removeSubcategory(Category subcategory) {
        subcategories.remove(subcategory);
        subcategory.setParentCategory(null);
    }

    /**
     * Проверить, является ли категория корневой
     * @return true если у категории нет родителя
     */
    public boolean isRoot() {
        return parentCategory == null;
    }

    /**
     * Проверить, является ли категория конечной (листом)
     * @return true если у категории нет подкатегорий
     */
    public boolean isLeaf() {
        return subcategories.isEmpty();
    }

    /**
     * Получить уровень вложенности категории
     * @return уровень вложенности (0 для корневых)
     */
    public int getLevel() {
        int level = 0;
        Category current = this.parentCategory;
        while (current != null) {
            level++;
            current = current.getParentCategory();
        }
        return level;
    }

    // ========== GETTERS и SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(Category parentCategory) {
        this.parentCategory = parentCategory;
    }

    public List<Category> getSubcategories() {
        return subcategories;
    }

    public void setSubcategories(List<Category> subcategories) {
        this.subcategories = subcategories;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // ========== EQUALS и HASHCODE ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // ========== TO STRING ==========

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", parentCategory=" + (parentCategory != null ? parentCategory.getName() : "null") +
                ", active=" + active +
                ", subcategoriesCount=" + subcategories.size() +
                ", productsCount=" + products.size() +
                '}';
    }
}