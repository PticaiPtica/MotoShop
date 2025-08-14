package model;

import jakarta.persistence.*;

@Entity
public class ProdyctImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String imageUrl;
    private boolean isMain;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
