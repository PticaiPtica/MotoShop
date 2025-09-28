package ru.academy.homework.motoshop.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.academy.homework.motoshop.model.Category;
import ru.academy.homework.motoshop.model.Product;
import ru.academy.homework.motoshop.repository.CategoryRepository;
import ru.academy.homework.motoshop.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    /**
     * @param id
     * @return
     */
    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * @param product
     */
    @Override
    public void save(Product product) {
        productRepository.save(product);

    }

    /**
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        productRepository.deleteById(id);

    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Product saveProduct(Product product) {
        // Валидация продукта перед сохранением
        validateProduct(product);

        // Устанавливаем доступность на основе количества
        if (product.getStockQuantity() != null) {
            product.setAvailable(product.getStockQuantity() > 0);
        }

        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, Product productDetails) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Продукт не найден с id: " + id));

        // Обновляем только не-null поля
        if (productDetails.getName() != null) {
            existingProduct.setName(productDetails.getName());
        }
        if (productDetails.getDescription() != null) {
            existingProduct.setDescription(productDetails.getDescription());
        }
        if (productDetails.getPrice() != null) {
            existingProduct.setPrice(productDetails.getPrice());
        }
        if (productDetails.getStockQuantity() != null) {
            existingProduct.setStockQuantity(productDetails.getStockQuantity());
            existingProduct.setAvailable(productDetails.getStockQuantity() > 0);
        }
        if (productDetails.getCategory() != null && productDetails.getCategory().getId() != null) {
            Category category = categoryRepository.findById(productDetails.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Категория не найдена"));
            existingProduct.setCategory(category);
        }
        if (productDetails.getBrand() != null) {
            existingProduct.setBrand(productDetails.getBrand());
        }
        if (productDetails.getModel() != null) {
            existingProduct.setModel(productDetails.getModel());
        }
        if (productDetails.getImageUrl() != null) {
            existingProduct.setImageUrl(productDetails.getImageUrl());
        }
        if (productDetails.getAvailable() != null) {
            existingProduct.setAvailable(productDetails.getAvailable());
        }

        return productRepository.save(existingProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Продукт не найден с id: " + id));

        // Проверяем, есть ли связанные заказы
        if (!product.getOrderItems().isEmpty()) {
            throw new RuntimeException("Нельзя удалить продукт, так как он связан с заказами");
        }

        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findProductsByPriceRange(Double minPrice, Double maxPrice) {
        if (minPrice == null) minPrice = 0.0;
        if (maxPrice == null) maxPrice = Double.MAX_VALUE;

        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findProductsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Категория не найдена с id: " + categoryId));

        return productRepository.findByCategory(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findActiveProducts() {
        return productRepository.findByAvailableTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findLowStockProducts(int threshold) {
        return productRepository.findByStockQuantityLessThanEqualAndAvailableTrue(threshold);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalProductsCount() {
        return productRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveProductsCount() {
        return productRepository.countByAvailableTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public long getOutOfStockProductsCount() {
        return productRepository.countByStockQuantityLessThanEqualOrAvailableFalse(0);
    }

    @Override
    @Transactional(readOnly = true)
    public long getLowStockProductsCount(int threshold) {
        return productRepository.countByStockQuantityBetweenAndAvailableTrue(1, threshold);
    }

    @Override
    public void decreaseStock(Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть положительным");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Продукт не найден с id: " + productId));

        if (product.getStockQuantity() == null || product.getStockQuantity() < quantity) {
            throw new RuntimeException("Недостаточно товара на складе. Доступно: " +
                    (product.getStockQuantity() != null ? product.getStockQuantity() : 0) +
                    ", требуется: " + quantity);
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        product.setAvailable(product.getStockQuantity() > 0);

        productRepository.save(product);
    }

    @Override
    public void increaseStock(Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть положительным");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Продукт не найден с id: " + productId));

        int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        product.setStockQuantity(currentStock + quantity);
        product.setAvailable(true);

        productRepository.save(product);
    }

    /**
     * @param categoryId
     * @param pageable
     * @return
     */
    @Override
    public Page<Product> findByCategoryId(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    /**
     * @return
     */
    @Override
    public Object findAllProducts() {
        return productRepository.findAll();
    }

    /**
     * @param categoryId
     * @return
     */
    @Override
    public Object findAllProductsById(Long categoryId) {
        return productRepository.findById(categoryId);
    }

    // Вспомогательные методы
    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Название продукта обязательно");
        }
        if (product.getPrice() == null || product.getPrice() <= 0) {
            throw new IllegalArgumentException("Цена должна быть положительной");
        }
        if (product.getStockQuantity() != null && product.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Количество не может быть отрицательным");
        }
    }
}