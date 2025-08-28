package ru.academy.homework.motoshop.model;

import jakarta.persistence.*;

@Entity
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String imageUrl;
    private boolean isMain;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public ProductImage(long id, String imageUrl, boolean isMain, Product product) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.isMain = isMain;
        this.product = product;
    }
    public ProductImage() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) {
        isMain = main;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public String toString() {
        return "ProductImage{" +
                "id=" + id +
                ", imageUrl='" + imageUrl + '\'' +
                ", isMain=" + isMain +
                ", product=" + product +
                '}';
    }
}
