package ru.academy.homework.motoshop.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.academy.homework.motoshop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Стандартные методы
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByQuantityLessThan(int quantity);

    // Кастомные запросы для загрузки связанных сущностей
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.images LEFT JOIN FETCH p.attributes")
    List<Product> findAllWithDetails();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.images LEFT JOIN FETCH p.attributes WHERE p.id = :id")
    Optional<Product> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.category.id = :categoryId")
    List<Product> findByCategoryIdWithDetails(@Param("categoryId") Long categoryId);

    long countByCategoryId(Long categoryId);

    List<Product> findByCategoryIdAndPriceBetween(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice);

    List<Product> findByQuantityGreaterThan(int i);

    boolean existsByCategoryId(Long categoryId);
}