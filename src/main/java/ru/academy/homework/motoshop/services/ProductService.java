package ru.academy.homework.motoshop.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.academy.homework.motoshop.model.Product;


import java.util.List;
import java.util.Optional;

public interface ProductService {

    // Основные CRUD операции
    List<Product> getAllProducts();
    Page<Product> getAllProducts(Pageable pageable);
    Optional<Product> getProductById(Long id);
    Product saveProduct(Product product);
    Product updateProduct(Long id, Product productDetails);
    void deleteProduct(Long id);
    Page<Product> findAll(Pageable pageable);
    Optional<Product> findById(Long id);
    void save(Product product);
    void deleteById(Long id);

    // Поиск и фильтрация
    List<Product> searchProductsByName(String name);
    List<Product> findProductsByPriceRange(Double minPrice, Double maxPrice);
    List<Product> findProductsByCategory(Long categoryId);
    List<Product> findActiveProducts();
    List<Product> findLowStockProducts(int threshold);

    // Статистика
    long getTotalProductsCount();
    long getActiveProductsCount();
    long getOutOfStockProductsCount();
    long getLowStockProductsCount(int threshold);

    // Бизнес-логика
    void decreaseStock(Long productId, Integer quantity);
    void increaseStock(Long productId, Integer quantity);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    Object findAllProducts();

    Object findAllProductsById(Long categoryId);
}
