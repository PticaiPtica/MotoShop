package ru.academy.homework.motoshop.repository;

import org.springframework.data.jpa.repository.Query;
import ru.academy.homework.motoshop.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Корневые категории
    @Query("SELECT c FROM Category c WHERE c.parentCategory IS NULL")
    List<Category> findRootCategories();

    // Конечные категории (листья)
    @Query("SELECT c FROM Category c WHERE c.subcategories IS EMPTY")
    List<Category> findLeafCategories();

    // Категории с продуктами
    @Query("SELECT c FROM Category c WHERE c.products IS NOT EMPTY")
    List<Category> findCategoriesWithProducts();

    // Категории по родительскому ID
    @Query("SELECT c FROM Category c WHERE c.parentCategory.id = :parentId")
    List<Category> findByParentId(Long parentId);

    // Поиск по имени
    List<Category> findByNameContainingIgnoreCase(String name);

    boolean existsByParentCategoryId(Long categoryId);

    List<Category> findByParentCategoryIsNull();

    List<Category> findByParentCategoryId(Long parentId);

    boolean existsByName(String name);


    List<Category> findAllByOrderByName();

    Category findByName(String name);
}
