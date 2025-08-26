package ru.academy.homework.motoshop.repository;

import ru.academy.homework.motoshop.model.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {
    void deleteByProductId(Long id);

    List<ProductAttribute> findByProductId(Long productId);
}
