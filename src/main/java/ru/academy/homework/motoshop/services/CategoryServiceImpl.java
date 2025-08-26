package ru.academy.homework.motoshop.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.academy.homework.motoshop.model.Category;
import ru.academy.homework.motoshop.repository.CategoryRepository;
import ru.academy.homework.motoshop.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        // Проверяем родительскую категорию
        if (category.getParentCategory() != null && category.getParentCategory().getId() != null) {
            Category parent = categoryRepository.findById(category.getParentCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            category.setParentCategory(parent);

            // Проверяем циклические зависимости
            if (hasCircularDependency(category, parent)) {
                throw new RuntimeException("Circular dependency detected");
            }
        }

        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Long id, Category categoryDetails) {
        return categoryRepository.findById(id)
                .map(existingCategory -> {
                    existingCategory.setName(categoryDetails.getName());
                    existingCategory.setDescription(categoryDetails.getDescription());

                    // Обновляем родительскую категорию, если предоставлена
                    if (categoryDetails.getParentCategory() != null &&
                            categoryDetails.getParentCategory().getId() != null) {

                        Category newParent = categoryRepository.findById(categoryDetails.getParentCategory().getId())
                                .orElseThrow(() -> new RuntimeException("Parent category not found"));

                        // Проверяем циклические зависимости
                        if (hasCircularDependency(existingCategory, newParent)) {
                            throw new RuntimeException("Circular dependency detected");
                        }

                        existingCategory.setParentCategory(newParent);
                    } else {
                        existingCategory.setParentCategory(null);
                    }

                    return categoryRepository.save(existingCategory);
                })
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Проверяем, есть ли продукты в категории
        if (categoryHasProducts(id)) {
            throw new RuntimeException("Cannot delete category with products");
        }

        // Перемещаем подкатегории на уровень выше
        if (!category.getSubcategories().isEmpty()) {
            for (Category subcategory : new ArrayList<>(category.getSubcategories())) {
                subcategory.setParentCategory(category.getParentCategory());
                categoryRepository.save(subcategory);
            }
        }

        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getRootCategories() {
        return categoryRepository.findByParentCategoryIsNull();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getSubcategories(Long parentId) {
        return categoryRepository.findByParentCategoryId(parentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategoryTree() {
        List<Category> rootCategories = getRootCategories();
        return buildCategoryTree(rootCategories);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategoryPath(Long categoryId) {
        List<Category> path = new ArrayList<>();
        Optional<Category> currentCategory = categoryRepository.findById(categoryId);

        while (currentCategory.isPresent()) {
            path.add(0, currentCategory.get()); // Добавляем в начало
            currentCategory = Optional.ofNullable(currentCategory.get().getParentCategory());
        }

        return path;
    }

    @Override
    @Transactional(readOnly = true)
    public long getProductCount(Long categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalProductCountIncludingSubcategories(Long categoryId) {
        long count = getProductCount(categoryId);
        List<Category> subcategories = getSubcategories(categoryId);

        for (Category subcategory : subcategories) {
            count += getTotalProductCountIncludingSubcategories(subcategory.getId());
        }

        return count;
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
    public boolean isValidParentCategory(Long categoryId, Long potentialParentId) {
        if (categoryId.equals(potentialParentId)) {
            return false; // Не может быть родителем самого себя
        }

        Optional<Category> potentialParent = categoryRepository.findById(potentialParentId);
        if (potentialParent.isEmpty()) {
            return false;
        }

        // Проверяем циклические зависимости
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        return !hasCircularDependency(category, potentialParent.get());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean categoryHasProducts(Long categoryId) {
        return productRepository.existsByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean categoryHasSubcategories(Long categoryId) {
        return categoryRepository.existsByParentCategoryId(categoryId);
    }

    // Вспомогательные методы
    private List<Category> buildCategoryTree(List<Category> categories) {
        for (Category category : categories) {
            List<Category> subcategories = categoryRepository.findByParentCategoryId(category.getId());
            category.setSubcategories(buildCategoryTree(subcategories));
        }
        return categories;
    }

    private boolean hasCircularDependency(Category category, Category potentialParent) {
        // Проверяем, не создаст ли это циклическую зависимость
        Category current = potentialParent;
        while (current != null) {
            if (current.getId().equals(category.getId())) {
                return true;
            }
            current = current.getParentCategory();
        }
        return false;
    }

    // Дополнительные методы для бизнес-логики
    public Category moveCategory(Long categoryId, Long newParentId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (newParentId == null) {
            category.setParentCategory(null);
        } else {
            Category newParent = categoryRepository.findById(newParentId)
                    .orElseThrow(() -> new RuntimeException("New parent category not found"));

            if (hasCircularDependency(category, newParent)) {
                throw new RuntimeException("Circular dependency detected");
            }

            category.setParentCategory(newParent);
        }

        return categoryRepository.save(category);
    }

    public List<Category> getLeafCategories() {
        return categoryRepository.findLeafCategories();
    }

    public Map<Long, Long> getProductCountsForAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        Map<Long, Long> counts = new HashMap<>();

        for (Category category : categories) {
            counts.put(category.getId(), getTotalProductCountIncludingSubcategories(category.getId()));
        }

        return counts;
    }
}