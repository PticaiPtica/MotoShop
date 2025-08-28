package ru.academy.homework.motoshop.repository;

import ru.academy.homework.motoshop.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    void deleteByProductId(Long id);

    List<ProductImage> findByProductId(Long productId);

    boolean existsByIdAndProductId(Long imageId, Long productId);
}
