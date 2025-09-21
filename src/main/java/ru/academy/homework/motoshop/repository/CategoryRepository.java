package ru.academy.homework.motoshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.academy.homework.motoshop.model.Category;

import java.util.List;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    @Query("SELECT c FROM Category c JOIN FETCH c.products")
    List<Category> findCategoriesWithProducts();

    List<Category> findByNameContainingIgnoreCase(String name);
}
