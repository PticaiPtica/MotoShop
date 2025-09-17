package ru.academy.homework.motoshop.services;

import ru.academy.homework.motoshop.model.Product;
import ru.academy.homework.motoshop.model.ProductAttribute;
import ru.academy.homework.motoshop.model.ProductImage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления товарами.
 * Предоставляет методы для CRUD операций, работы с изображениями, атрибутами и управления запасами.
 */
public interface ProductService {

    // ========== ОСНОВНЫЕ CRUD ОПЕРАЦИИ ==========

    /**
     * Получить все товары (без деталей)
     * @return список всех товаров без изображений и атрибутов
     */
    List<Product> getAllProducts();

    /**
     * Получить все товары с деталями (изображения и атрибуты)
     * @return список всех товаров с полной информацией
     */
    List<Product> getAllProductsWithDetails();

    /**
     * Найти товар по ID (без деталей)
     * @param id идентификатор товара
     * @return Optional с товаром, если найден
     */
    Optional<Product> getProductById(Long id);

    /**
     * Найти товар по ID с деталями (изображения и атрибуты)
     * @param id идентификатор товара
     * @return Optional с товаром и полной информацией, если найден
     */
    Optional<Product> getProductByIdWithDetails(Long id);

    /**
     * Проверить существование товара по идентификатору
     * @param id идентификатор товара
     * @return true если товар существует
     */
    boolean existsById(Long id);

    /**
     * Сохранить новый товар
     * @param product товар для сохранения
     * @return сохраненный товар
     */
    Product saveProduct(Product product);

    /**
     * Обновить основную информацию о товаре
     * @param id идентификатор товара
     * @param productDetails новые данные товара
     * @return обновленный товар
     */
    Product updateProduct(Long id, Product productDetails);

    /**
     * Полное обновление товара с изображениями и атрибутами
     * @param id идентификатор товара
     * @param productDetails новые данные товара
     * @return полностью обновленный товар
     */
    Product updateProductWithDetails(Long id, Product productDetails);

    /**
     * Обновить количество товара на складе
     * @param id идентификатор товара
     * @param quantity новое количество
     * @return товар с обновленным количеством
     */
    Product updateProductQuantity(Long id, int quantity);

    /**
     * Обновить цену товара
     * @param id идентификатор товара
     * @param price новая цена
     * @return товар с обновленной ценой
     */
    Product updateProductPrice(Long id, BigDecimal price);

    /**
     * Удалить товар по ID
     * @param id идентификатор товара для удаления
     */
    void deleteProduct(Long id);

    // ========== ПОИСК И ФИЛЬТРАЦИЯ ==========

    /**
     * Поиск товаров по названию (без учета регистра)
     * @param name часть названия для поиска
     * @return список товаров, содержащих указанную строку в названии
     */
    List<Product> searchProductsByName(String name);

    /**
     * Поиск товаров в ценовом диапазоне
     * @param minPrice минимальная цена
     * @param maxPrice максимальная цена
     * @return список товаров в указанном ценовом диапазоне
     */
    List<Product> findProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Поиск товаров по категории
     * @param categoryId идентификатор категории
     * @return список товаров в указанной категории
     */
    List<Product> findProductsByCategory(Long categoryId);

    /**
     * Поиск товаров с низким запасом
     * @param threshold пороговое значение количества
     * @return список товаров, количество которых меньше порога
     */
    List<Product> findLowStockProducts(int threshold);

    /**
     * Поиск доступных товаров (в наличии)
     * @return список товаров с количеством > 0
     */
    List<Product> findAvailableProducts();

    /**
     * Поиск товаров по категории и ценовому диапазону
     * @param categoryId идентификатор категории
     * @param minPrice минимальная цена
     * @param maxPrice максимальная цена
     * @return список товаров, удовлетворяющих обоим критериям
     */
    List<Product> findProductsByCategoryAndPriceRange(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice);

    // ========== РАБОТА С ИЗОБРАЖЕНИЯМИ ==========

    /**
     * Получить все изображения товара
     * @param productId идентификатор товара
     * @return список изображений товара
     */
    List<ProductImage> getProductImages(Long productId);

    /**
     * Добавить изображение к товару
     * @param productId идентификатор товара
     * @param image изображение для добавления
     * @return сохраненное изображение
     */
    ProductImage addImageToProduct(Long productId, ProductImage image);

    /**
     * Удалить изображение у товара
     * @param productId идентификатор товара
     * @param imageId идентификатор изображения
     */
    void removeImageFromProduct(Long productId, Long imageId);

    // ========== РАБОТА С АТРИБУТАМИ ==========

    /**
     * Получить все атрибуты товара
     * @param productId идентификатор товара
     * @return список атрибутов товара
     */
    List<ProductAttribute> getProductAttributes(Long productId);

    /**
     * Добавить атрибут к товару
     * @param productId идентификатор товара
     * @param attribute атрибут для добавления
     * @return сохраненный атрибут
     */
    ProductAttribute addAttributeToProduct(Long productId, ProductAttribute attribute);

    /**
     * Удалить атрибут у товара
     * @param productId идентификатор товара
     * @param attributeId идентификатор атрибута
     */
    void removeAttributeFromProduct(Long productId, Long attributeId);

    // ========== УПРАВЛЕНИЕ ЗАПАСАМИ ==========

    /**
     * Увеличить количество товара на указанное значение
     * @param productId идентификатор товара
     * @param amount количество для увеличения
     */
    void increaseProductQuantity(Long productId, int amount);

    /**
     * Уменьшить количество товара на указанное значение
     * @param productId идентификатор товара
     * @param amount количество для уменьшения
     */
    void decreaseProductQuantity(Long productId, int amount);

    // ========== ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ ==========

    /**
     * Получить общее количество товаров
     * @return общее количество товаров в системе
     */
    long getTotalProductsCount();

    /**
     * Получить количество товаров в категории
     * @param categoryId идентификатор категории
     * @return количество товаров в категории
     */
    long getProductsCountByCategory(Long categoryId);

    /**
     * Получить товары с сортировкой по цене (по возрастанию)
     * @return отсортированный список товаров
     */
    List<Product> getProductsSortedByPriceAsc();

    /**
     * Получить товары с сортировкой по цене (по убыванию)
     * @return отсортированный список товаров
     */
    List<Product> getProductsSortedByPriceDesc();

    /**
     * Получить товары с сортировкой по названию
     * @return отсортированный список товаров
     */
    List<Product> getProductsSortedByName();

    /**
     * Найти все товары по списку идентификаторов
     * @param ids список идентификаторов товаров
     * @return список найденных товаров
     */
    List<Product> findAllById(Iterable<Long> ids);

    /**
     * Сохранить список товаров
     * @param products список товаров для сохранения
     * @return список сохраненных товаров
     */
    List<Product> saveAll(Iterable<Product> products);

    /**
     * Получить среднюю цену товаров в категории
     * @param categoryId идентификатор категории
     * @return средняя цена товаров
     */
    BigDecimal getAveragePriceByCategory(Long categoryId);

    /**
     * Получить самый дорогой товар в категории
     * @param categoryId идентификатор категории
     * @return самый дорогой товар
     */
    Optional<Product> getMostExpensiveProductInCategory(Long categoryId);

    /**
     * Получить самый дешевый товар в категории
     * @param categoryId идентификатор категории
     * @return самый дешевый товар
     */
    Optional<Product> getCheapestProductInCategory(Long categoryId);
}