package ru.academy.homework.motoshop.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.academy.homework.motoshop.model.Category;
import ru.academy.homework.motoshop.repository.CategoryRepository;
import ru.academy.homework.motoshop.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса для работы с категориями товаров.
 * Упрощенная версия без древовидной структуры
 */
@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    // ========== ОСНОВНЫЕ CRUD ОПЕРАЦИИ ==========

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Category createCategory(Category category) {
        // Проверяем уникальность имени
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Категория с таким именем уже существует");
        }

        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Long id, Category categoryDetails) {
        return categoryRepository.findById(id)
                .map(existingCategory -> {
                    // Проверяем, что новое имя не совпадает с именем другой категории
                    if (!existingCategory.getName().equals(categoryDetails.getName()) &&
                            categoryRepository.existsByName(categoryDetails.getName())) {
                        throw new IllegalArgumentException("Категория с таким именем уже существует");
                    }

                    existingCategory.setName(categoryDetails.getName());
                    existingCategory.setDescription(categoryDetails.getDescription());

                    return categoryRepository.save(existingCategory);
                })
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена с id: " + id));
    }

    @Override
    public void deleteById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена"));

        // Проверяем, есть ли продукты в категории
        if (productRepository.existsByCategoryId(id)) {
            throw new IllegalArgumentException("Нельзя удалить категорию с товарами");
        }

        categoryRepository.deleteById(id);
    }

    // ========== ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ ==========

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Category save(Category category) {
        if (category.getId() == null) {
            return createCategory(category);
        } else {
            return updateCategory(category.getId(), category);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public long getProductCount(Long categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> searchCategoriesByName(String name) {
        return categoryRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategoriesWithProducts() {
        return categoryRepository.findCategoriesWithProducts();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean categoryHasProducts(Long categoryId) {
        return productRepository.existsByCategoryId(categoryId);
    }

    // ========== ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ ДЛЯ БИЗНЕС-ЛОГИКИ ==========

    @Override
    public boolean existsById(Long id) {
        return categoryRepository.existsById(id);
    }

    @Override
    public long count() {
        return categoryRepository.count();
    }

    @Override
    public void deleteAll() {
        // Проверяем, что ни в одной категории нет товаров
        if (productRepository.count() > 0) {
            throw new IllegalArgumentException("Нельзя удалить все категории, так как существуют товары");
        }

        categoryRepository.deleteAll();
    }

    @Override
    public void delete(Category category) {
        deleteById(category.getId());
    }

    @Override
    public List<Category> saveAll(Iterable<Category> categories) {
        return categoryRepository.saveAll(categories);
    }
}