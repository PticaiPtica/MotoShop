package ru.academy.homework.motoshop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.academy.homework.motoshop.model.Category;
import ru.academy.homework.motoshop.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    long count();

    // Базовые методы поиска
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
    List<Product> findByCategory(Category category);
    List<Product> findByAvailableTrue();

    // Методы для низкого запаса
    List<Product> findByStockQuantityLessThanEqualAndAvailableTrue(Integer threshold);
    List<Product> findByStockQuantityBetweenAndAvailableTrue(Integer min, Integer max);

    // Методы подсчета для статистики
    long countByAvailableTrue();
    long countByStockQuantityLessThanEqualOrAvailableFalse(Integer threshold);
    long countByStockQuantityBetweenAndAvailableTrue(Integer min, Integer max);

    // Методы с пагинацией
    Page<Product> findByAvailableTrue(Pageable pageable);
    Page<Product> findByCategory(Category category, Pageable pageable);
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Сложные запросы
    @Query("SELECT p FROM Product p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:available IS NULL OR p.available = :available)")
    Page<Product> findWithFilters(@Param("name") String name,
                                  @Param("minPrice") Double minPrice,
                                  @Param("maxPrice") Double maxPrice,
                                  @Param("categoryId") Long categoryId,
                                  @Param("available") Boolean available,
                                  Pageable pageable);

    // Поиск продуктов, которые никогда не заказывались
    @Query("SELECT p FROM Product p WHERE p.orderItems IS EMPTY")
    List<Product> findProductsNeverOrdered();

    // Самые популярные продукты (по количеству заказов)
    @Query("SELECT p, COUNT(oi) as orderCount FROM Product p LEFT JOIN p.orderItems oi " +
            "GROUP BY p ORDER BY orderCount DESC")
    Page<Object[]> findMostPopularProducts(Pageable pageable);

    boolean existsByCategoryId(Long id);

    long countByCategoryId(Long categoryId);

}