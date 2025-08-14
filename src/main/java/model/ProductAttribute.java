package model;

import jakarta.persistence.*;

@Entity
public class ProductAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
