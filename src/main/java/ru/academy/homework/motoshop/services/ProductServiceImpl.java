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

/**
 * Реализация сервиса для работы с товарами
 * Обеспечивает полный цикл CRUD операций и бизнес-логику управления товарами
 */
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    // Репозиторий для работы с товарами
    private final ProductRepository productRepository;
    // Репозиторий для работы с изображениями товаров
    private final ProductImageRepository productImageRepository;
    // Репозиторий для работы с атрибутами товаров
    private final ProductAttributeRepository productAttributeRepository;
    // Сервис для работы с категориями
    private final CategoryService categoryService;

    /**
     * Конструктор с внедрением зависимостей
     */
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

    // ========== ОСНОВНЫЕ CRUD ОПЕРАЦИИ ==========

    /**
     * Получить все товары (без деталей)
     * @return список всех товаров без изображений и атрибутов
     */
    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Получить все товары с деталями (изображения и атрибуты)
     * @return список всех товаров с полной информацией
     */
    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProductsWithDetails() {
        return productRepository.findAllWithDetails();
    }

    /**
     * Найти товар по ID (без деталей)
     * @param id идентификатор товара
     * @return Optional с товаром, если найден
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Найти товар по ID с деталями (изображения и атрибуты)
     * @param id идентификатор товара
     * @return Optional с товаром и полной информацией, если найден
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getProductByIdWithDetails(Long id) {
        return productRepository.findByIdWithDetails(id);
    }

    /**
     * Сохранить новый товар.
     * Устанавливает обратные ссылки для изображений и атрибутов
     * @param product товар для сохранения
     * @return сохраненный товар
     */
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

    /**
     * Обновить основную информацию о товаре
     * @param id идентификатор товара
     * @param productDetails новые данные товара
     * @return обновленный товар
     */
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

    /**
     * Полное обновление товара с изображениями и атрибутами.
     * Заменяет все изображения и атрибуты новыми
     * @param id идентификатор товара
     * @param productDetails новые данные товара
     * @return полностью обновленный товар
     */
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

    /**
     * Обновить количество товара на складе
     * @param id идентификатор товара
     * @param quantity новое количество
     * @return товар с обновленным количеством
     */
    @Override
    public Product updateProductQuantity(Long id, int quantity) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setQuantity(quantity);
                    return productRepository.save(existingProduct);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    /**
     * Обновить цену товара
     * @param id идентификатор товара
     * @param price новая цена
     * @return товар с обновленной ценой
     */
    @Override
    public Product updateProductPrice(Long id, BigDecimal price) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setPrice(price);
                    return productRepository.save(existingProduct);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    /**
     * Удалить товар по ID.
     * Удаляет связанные изображения и атрибуты перед удалением товара
     * @param id идентификатор товара для удаления
     */
    @Override
    public void deleteProduct(Long id) {
        // Сначала удаляем связанные изображения и атрибуты
        productImageRepository.deleteByProductId(id);
        productAttributeRepository.deleteByProductId(id);

        // Затем удаляем сам продукт
        productRepository.deleteById(id);
    }

    // ========== ПОИСК И ФИЛЬТРАЦИЯ ==========

    /**
     * Поиск товаров по названию (без учета регистра)
     * @param name часть названия для поиска
     * @return список товаров, содержащих указанную строку в названии
     */
    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Поиск товаров в ценовом диапазоне
     * @param minPrice минимальная цена
     * @param maxPrice максимальная цена
     * @return список товаров в указанном ценовом диапазоне
     */
    @Override
    @Transactional(readOnly = true)
    public List<Product> findProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    /**
     * Поиск товаров по категории
     * @param categoryId идентификатор категории
     * @return список товаров в указанной категории
     */
    @Override
    @Transactional(readOnly = true)
    public List<Product> findProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    /**
     * Поиск товаров с низким запасом
     * @param threshold пороговое значение количества
     * @return список товаров, количество которых меньше порога
     */
    @Override
    @Transactional(readOnly = true)
    public List<Product> findLowStockProducts(int threshold) {
        return productRepository.findByQuantityLessThan(threshold);
    }

    /**
     * Поиск доступных товаров (в наличии)
     * @return список товаров с количеством > 0
     */
    @Override
    @Transactional(readOnly = true)
    public List<Product> findAvailableProducts() {
        return productRepository.findByQuantityGreaterThan(0);
    }

    /**
     * Поиск товаров по категории и ценовому диапазону
     * @param categoryId идентификатор категории
     * @param minPrice минимальная цена
     * @param maxPrice максимальная цена
     * @return список товаров, удовлетворяющих обоим критериям
     */
    @Override
    @Transactional(readOnly = true)
    public List<Product> findProductsByCategoryAndPriceRange(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByCategoryIdAndPriceBetween(categoryId, minPrice, maxPrice);
    }

    // ========== РАБОТА С ИЗОБРАЖЕНИЯМИ ==========

    /**
     * Получить все изображения товара
     * @param productId идентификатор товара
     * @return список изображений товара
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductImage> getProductImages(Long productId) {
        return productImageRepository.findByProductId(productId);
    }

    /**
     * Добавить изображение к товару
     * @param productId идентификатор товара
     * @param image изображение для добавления
     * @return сохраненное изображение
     */
    @Override
    public ProductImage addImageToProduct(Long productId, ProductImage image) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        image.setProduct(product);
        return productImageRepository.save(image);
    }

    /**
     * Удалить изображение у товара
     * Проверяет, что изображение принадлежит указанному товару
     * @param productId идентификатор товара
     * @param imageId идентификатор изображения
     */
    @Override
    @Transactional
    public void removeImageFromProduct(Long productId, Long imageId) {
        // Проверяем существование и принадлежность в одном запросе
        boolean exists = productImageRepository.existsByIdAndProductId(imageId, productId);

        if (!exists) {
            throw new RuntimeException("Image not found or does not belong to the specified product");
        }

        productImageRepository.deleteById(imageId);
    }
    // ========== РАБОТА С АТРИБУТАМИ ==========

    /**
     * Получить все атрибуты товара
     * @param productId идентификатор товара
     * @return список атрибутов товара
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductAttribute> getProductAttributes(Long productId) {
        return productAttributeRepository.findByProductId(productId);
    }

    /**
     * Добавить атрибут к товару
     * @param productId идентификатор товара
     * @param attribute атрибут для добавления
     * @return сохраненный атрибут
     */
    @Override
    public ProductAttribute addAttributeToProduct(Long productId, ProductAttribute attribute) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        attribute.setProduct(product);
        return productAttributeRepository.save(attribute);
    }

    /**
     * Удалить атрибут у товара
     * Проверяет, что атрибут принадлежит указанному товару
     * @param productId идентификатор товара
     * @param attributeId идентификатор атрибута
     */
    @Override
    @Transactional
    public void removeAttributeFromProduct(Long productId, Long attributeId) {
        // Проверяем существование и принадлежность в одном запросе
        boolean exists = productAttributeRepository.existsByIdAndProductId(attributeId, productId);

        if (!exists) {
            throw new RuntimeException("Attribute not found or does not belong to the specified product");
        }

        productAttributeRepository.deleteById(attributeId);
    }

    // ========== УПРАВЛЕНИЕ ЗАПАСАМИ ==========

    /**
     * Увеличить количество товара на указанное значение
     * @param productId идентификатор товара
     * @param amount количество для увеличения
     */
    @Override
    public void increaseProductQuantity(Long productId, int amount) {
        productRepository.findById(productId)
                .ifPresent(product -> {
                    product.setQuantity(product.getQuantity() + amount);
                    productRepository.save(product);
                });
    }

    /**
     * Уменьшить количество товара на указанное значение
     * Проверяет, что достаточно товара для уменьшения
     * @param productId идентификатор товара
     * @param amount количество для уменьшения
     * @throws RuntimeException если недостаточно товара на складе
     */
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