package ru.academy.homework.motoshop.services;

import ru.academy.homework.motoshop.model.Product;
import ru.academy.homework.motoshop.model.ProductAttribute;
import ru.academy.homework.motoshop.model.ProductImage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> getAllProducts();
    List<Product> getAllProductsWithDetails();
    Optional<Product> getProductById(Long id);
    Optional<Product> getProductByIdWithDetails(Long id);
    Product saveProduct(Product product);
    Product updateProduct(Long id, Product productDetails);
    Product updateProductWithDetails(Long id, Product productDetails);
    Product updateProductQuantity(Long id, int quantity);
    Product updateProductPrice(Long id, BigDecimal price);
    void deleteProduct(Long id);
    List<Product> searchProductsByName(String name);
    List<Product> findProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> findProductsByCategory(Long categoryId);
    List<Product> findLowStockProducts(int threshold);

    // Методы для работы с изображениями и атрибутами
    List<ProductImage> getProductImages(Long productId);
    List<ProductAttribute> getProductAttributes(Long productId);
    ProductImage addImageToProduct(Long productId, ProductImage image);
    ProductAttribute addAttributeToProduct(Long productId, ProductAttribute attribute);
    void removeImageFromProduct(Long productId, Long imageId);
    void removeAttributeFromProduct(Long productId, Long attributeId);
}