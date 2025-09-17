package ru.academy.homework.motoshop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.academy.homework.motoshop.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Стандартные методы поиска
    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByQuantityLessThan(int quantity);

    List<Product> findByQuantityGreaterThan(int quantity);

    List<Product> findByCategoryIdAndPriceBetween(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice);

    List<Product> findByCategoryIdIn(List<Long> categoryIds);

    // Методы сортировки
    List<Product> findAllByOrderByPriceDesc();

    List<Product> findAllByOrderByNameAsc();

    List<Product> findAllByOrderByPriceAsc();


    // Методы для проверки существования и подсчета
    boolean existsByCategoryId(Long categoryId);

    long countByCategoryId(Long categoryId);

    // Методы для агрегаций и поиска экстремальных значений
    Optional<BigDecimal> getAveragePriceByCategoryId(Long categoryId);

    Optional<Product> findTopByCategoryIdOrderByPriceDesc(Long categoryId);

    Optional<Product> findTopByCategoryIdOrderByPriceAsc(Long categoryId);

    // Кастомные запросы для загрузки связанных сущностей с FETCH
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.images LEFT JOIN FETCH p.attributes")
    List<Product> findAllWithDetails();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.images LEFT JOIN FETCH p.attributes WHERE p.id = :id")
    Optional<Product> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.category.id = :categoryId")
    List<Product> findByCategoryIdWithDetails(@Param("categoryId") Long categoryId);

    // Методы пагинации
    @Query(value = "SELECT * FROM products ORDER BY id OFFSET :offset LIMIT :limit",
            nativeQuery = true)
    List<Product> findProductsWithPagination(@Param("offset") int offset,
                                             @Param("limit") int limit);

    // Стандартные методы пагинации Spring Data (рекомендуется)
    Page<Product> findAll(Pageable pageable);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    // Дополнительный метод пагинации с сортировкой по цене
    @Query("SELECT p FROM Product p ORDER BY p.price DESC")
    Page<Product> findAllOrderByPriceDesc(Pageable pageable);

    // Дополнительный метод пагинации с фильтром по цене
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceBetweenWithPagination(@Param("minPrice") BigDecimal minPrice,
                                                   @Param("maxPrice") BigDecimal maxPrice,
                                                   Pageable pageable);
}