package ru.academy.homework.motoshop.services;


import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import ru.academy.homework.motoshop.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    // Основные CRUD операции
    List<Category> getAllCategories();

    Optional<Category> getCategoryById(Long id);

    Category createCategory(Category category);

    Category updateCategory(Long id, Category categoryDetails);

    void deleteCategory(Long id);

    // Специфичные методы для категорий
    List<Category> getRootCategories();

    List<Category> getSubcategories(Long parentId);

    List<Category> getCategoryTree();

    List<Category> getCategoryPath(Long categoryId);

    // Методы для работы с продуктами в категориях
    long getProductCount(Long categoryId);

    long getTotalProductCountIncludingSubcategories(Long categoryId);

    // Поиск и фильтрация
    List<Category> searchCategoriesByName(String name);

    List<Category> getCategoriesWithProducts();

    // Валидация
    boolean isValidParentCategory(Long categoryId, Long potentialParentId);

    boolean categoryHasProducts(Long categoryId);

    boolean categoryHasSubcategories(Long categoryId);
}