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

    boolean existsByCategoryId(Long id);

    List<Product> findByNameContainingIgnoreCase(String name);

    long countByCategoryId(Long categoryId);

    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByQuantityLessThan(int threshold);

    List<Product> findByQuantityGreaterThan(int i);

    List<Product> findByCategoryIdAndPriceBetween(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice);

    List<Product> findAllByOrderByPriceAsc();

    List<Product> findAllByOrderByPriceDesc();

    List<Product> findAllByOrderByNameAsc();

    Optional<BigDecimal> getAveragePriceByCategoryId(Long categoryId);

    Optional<Product> findTopByCategoryIdOrderByPriceDesc(Long categoryId);

    Optional<Product> findTopByCategoryIdOrderByPriceAsc(Long categoryId);
}