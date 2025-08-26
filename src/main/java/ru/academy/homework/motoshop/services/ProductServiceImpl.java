package ru.academy.homework.motoshop.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.academy.homework.motoshop.model.Category;
import ru.academy.homework.motoshop.model.Product;
import ru.academy.homework.motoshop.model.ProductAttribute;
import ru.academy.homework.motoshop.model.ProductImage;
import ru.academy.homework.motoshop.repository.ProductAttributeRepository;
import ru.academy.homework.motoshop.repository.ProductImageRepository;
import ru.academy.homework.motoshop.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final CategoryService categoryService;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              ProductImageRepository productImageRepository,
                              ProductAttributeRepository productAttributeRepository,
                              CategoryService categoryService) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.productAttributeRepository = productAttributeRepository;
        this.categoryService = categoryService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProductsWithDetails() {
        return productRepository.findAllWithDetails();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getProductByIdWithDetails(Long id) {
        return productRepository.findByIdWithDetails(id);
    }

    @Override
    public Product saveProduct(Product product) {
        // Проверяем и устанавливаем категорию
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            Category category = categoryService.getCategoryById(product.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + product.getCategory().getId()));
            product.setCategory(category);
        }

        // Устанавливаем обратные ссылки для изображений
        if (product.getImages() != null) {
            for (ProductImage image : product.getImages()) {
                image.setProduct(product);
            }
        }

        // Устанавливаем обратные ссылки для атрибутов
        if (product.getAttributes() != null) {
            for (ProductAttribute attribute : product.getAttributes()) {
                attribute.setProduct(product);
            }
        }

        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, Product productDetails) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setName(productDetails.getName());
                    existingProduct.setDescription(productDetails.getDescription());
                    existingProduct.setPrice(productDetails.getPrice());
                    existingProduct.setQuantity(productDetails.getQuantity());

                    // Обновляем категорию, если предоставлена
                    if (productDetails.getCategory() != null && productDetails.getCategory().getId() != null) {
                        Category category = categoryService.getCategoryById(productDetails.getCategory().getId())
                                .orElseThrow(() -> new RuntimeException("Category not found"));
                        existingProduct.setCategory(category);
                    }

                    return productRepository.save(existingProduct);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    public Product updateProductWithDetails(Long id, Product productDetails) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setName(productDetails.getName());
                    existingProduct.setDescription(productDetails.getDescription());
                    existingProduct.setPrice(productDetails.getPrice());
                    existingProduct.setQuantity(productDetails.getQuantity());

                    // Обновление категории
                    if (productDetails.getCategory() != null && productDetails.getCategory().getId() != null) {
                        Category category = categoryService.getCategoryById(productDetails.getCategory().getId())
                                .orElseThrow(() -> new RuntimeException("Category not found"));
                        existingProduct.setCategory(category);
                    }

                    // Обновление изображений (удаляем старые, добавляем новые)
                    if (productDetails.getImages() != null) {
                        // Удаляем существующие изображения
                        productImageRepository.deleteByProductId(id);

                        // Добавляем новые изображения
                        for (ProductImage image : productDetails.getImages()) {
                            image.setProduct(existingProduct);
                            productImageRepository.save(image);
                        }
                    }

                    // Обновление атрибутов (удаляем старые, добавляем новые)
                    if (productDetails.getAttributes() != null) {
                        // Удаляем существующие атрибуты
                        productAttributeRepository.deleteByProductId(id);

                        // Добавляем новые атрибуты
                        for (ProductAttribute attribute : productDetails.getAttributes()) {
                            attribute.setProduct(existingProduct);
                            productAttributeRepository.save(attribute);
                        }
                    }

                    return productRepository.save(existingProduct);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    public Product updateProductQuantity(Long id, int quantity) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setQuantity(quantity);
                    return productRepository.save(existingProduct);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    public Product updateProductPrice(Long id, BigDecimal price) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setPrice(price);
                    return productRepository.save(existingProduct);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    public void deleteProduct(Long id) {
        // Сначала удаляем связанные изображения и атрибуты
        productImageRepository.deleteByProductId(id);
        productAttributeRepository.deleteByProductId(id);

        // Затем удаляем сам продукт
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findLowStockProducts(int threshold) {
        return productRepository.findByQuantityLessThan(threshold);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImage> getProductImages(Long productId) {
        return productImageRepository.findByProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductAttribute> getProductAttributes(Long productId) {
        return productAttributeRepository.findByProductId(productId);
    }

    @Override
    public ProductImage addImageToProduct(Long productId, ProductImage image) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        image.setProduct(product);
        return productImageRepository.save(image);
    }

    @Override
    public ProductAttribute addAttributeToProduct(Long productId, ProductAttribute attribute) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        attribute.setProduct(product);
        return productAttributeRepository.save(attribute);
    }

    @Override
    public void removeImageFromProduct(Long productId, Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));

        if (!image.getProduct().getId().equals(productId)) {
            throw new RuntimeException("Image does not belong to the specified product");
        }

        productImageRepository.deleteById(imageId);
    }

    @Override
    public void removeAttributeFromProduct(Long productId, Long attributeId) {
        ProductAttribute attribute = productAttributeRepository.findById(attributeId)
                .orElseThrow(() -> new RuntimeException("Attribute not found with id: " + attributeId));

        if (!attribute.getProduct().getId().equals(productId)) {
            throw new RuntimeException("Attribute does not belong to the specified product");
        }

        productAttributeRepository.deleteById(attributeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAvailableProducts() {
        return productRepository.findByQuantityGreaterThan(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findProductsByCategoryAndPriceRange(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByCategoryIdAndPriceBetween(categoryId, minPrice, maxPrice);
    }

    @Override
    public void increaseProductQuantity(Long productId, int amount) {
        productRepository.findById(productId)
                .ifPresent(product -> {
                    product.setQuantity(product.getQuantity() + amount);
                    productRepository.save(product);
                });
    }

    @Override
    public void decreaseProductQuantity(Long productId, int amount) {
        productRepository.findById(productId)
                .ifPresent(product -> {
                    if (product.getQuantity() >= amount) {
                        product.setQuantity(product.getQuantity() - amount);
                        productRepository.save(product);
                    } else {
                        throw new RuntimeException("Insufficient quantity available");
                    }
                });
    }
}