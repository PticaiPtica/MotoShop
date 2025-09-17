package ru.academy.homework.motoshop.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.academy.homework.motoshop.model.Category;
import ru.academy.homework.motoshop.repository.CategoryRepository;
import ru.academy.homework.motoshop.repository.ProductRepository;

import java.util.*;

/**
 * Реализация сервиса для работы с категориями товаров.
 * Обеспечивает управление древовидной структурой категорий и связанной бизнес-логикой
 */
@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    // Репозиторий для работы с категориями
    private final CategoryRepository categoryRepository;
    // Репозиторий для работы с товарами (для проверок связанных товаров)
    private final ProductRepository productRepository;

    /**
     * Конструктор с внедрением зависимостей
     */
    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    // ========== ОСНОВНЫЕ CRUD ОПЕРАЦИИ ==========

    /**
     * Получить все категории
     *
     * @return список всех категорий
     */
    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Найти категорию по ID
     *
     * @param id идентификатор категории
     * @return Optional с категорией, если найдена
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    /**
     * Создать новую категорию.
     * Проверяет родительскую категорию и циклические зависимости
     *
     * @param category категория для создания
     * @return сохраненная категория
     */
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

    /**
     * Обновить информацию о категории
     *
     * @param id              идентификатор категории
     * @param categoryDetails новые данные категории
     * @return обновленная категория
     */
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

    /**
     * Удалить категорию.
     * Проверяет наличие товаров и перемещает подкатегории
     *
     * @param id идентификатор категории для удаления
     * @throws RuntimeException если в категории есть товары
     */
    @Override
    public void deleteById(Long id) {
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

    // ========== ДОБАВЛЕННЫЕ CRUD МЕТОДЫ ==========

    /**
     * Найти все категории.
     * Аналог метода {@link #getAllCategories()}.
     *
     * @return список всех категорий
     */
    @Override
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    /**
     * Найти категорию по идентификатору.
     * Аналог метода {@link #getCategoryById(Long)}.
     *
     * @param id идентификатор категории
     * @return категория в виде {@link Optional}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    /**
     * Сохранить категорию (создать или обновить существующую).
     *
     * @param category категория для сохранения
     * @return сохраненная категория
     */
    @Override
    public Category save(Category category) {
        if (category.getId() == null) {
            // Создание новой категории
            return createCategory(category);
        } else {
            // Обновление существующей категории
            return updateCategory(category.getId(), category);
        }
    }

    // ========== РАБОТА С ДЕРЕВОМ КАТЕГОРИЙ ==========

    /**
     * Получить корневые категории (без родителя)
     *
     * @return список корневых категорий
     */
    @Override
    @Transactional(readOnly = true)
    public List<Category> getRootCategories() {
        return categoryRepository.findByParentCategoryIsNull();
    }

    /**
     * Получить подкатегории указанной категории
     *
     * @param parentId идентификатор родительской категории
     * @return список подкатегорий
     */
    @Override
    @Transactional(readOnly = true)
    public List<Category> getSubcategories(Long parentId) {
        return categoryRepository.findByParentCategoryId(parentId);
    }

    /**
     * Получить полное дерево категорий с вложенностью
     *
     * @return дерево категорий с подкатегориями
     */


    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategoryTree() {
        List<Category> rootCategories = getRootCategories();
        return buildCategoryTree(rootCategories);
    }

    /**
     * Получить путь категории от корня до указанной категории
     *
     * @param categoryId идентификатор целевой категории
     * @return список категорий в порядке от корня к целевой
     */
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

    // ========== СТАТИСТИКА И АНАЛИТИКА ==========

    /**
     * Получить количество товаров в конкретной категории
     *
     * @param categoryId идентификатор категории
     * @return количество товаров
     */
    @Override
    @Transactional(readOnly = true)
    public long getProductCount(Long categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }

    /**
     * Получить общее количество товаров в категории включая все подкатегории
     *
     * @param categoryId идентификатор категории
     * @return общее количество товаров
     */
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

    // ========== ПОИСК И ФИЛЬТРАЦИЯ ==========

    /**
     * Поиск категорий по названию
     *
     * @param name часть названия для поиска
     * @return список категорий, содержащих указанную строку в названии
     */
    @Override
    @Transactional(readOnly = true)
    public List<Category> searchCategoriesByName(String name) {
        return categoryRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Получить категории, в которых есть товары
     *
     * @return список категорий с товарами
     */
    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategoriesWithProducts() {
        return categoryRepository.findCategoriesWithProducts();
    }

    // ========== ПРОВЕРКИ И ВАЛИДАЦИЯ ==========

    /**
     * Проверить, может ли категория быть родителем для другой категории
     *
     * @param categoryId        идентификатор категории
     * @param potentialParentId идентификатор потенциального родителя
     * @return true если может быть родителем
     */
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

    /**
     * Проверить, есть ли товары в категории
     *
     * @param categoryId идентификатор категории
     * @return true если есть товары
     */
    @Override
    @Transactional(readOnly = true)
    public boolean categoryHasProducts(Long categoryId) {
        return productRepository.existsByCategoryId(categoryId);
    }

    /**
     * Проверить, есть ли подкатегории у категории
     *
     * @param categoryId идентификатор категории
     * @return true если есть подкатегории
     */
    @Override
    @Transactional(readOnly = true)
    public boolean categoryHasSubcategories(Long categoryId) {
        return categoryRepository.existsByParentCategoryId(categoryId);
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    /**
     * Построить дерево категорий с вложенностью
     *
     * @param categories список категорий для построения дерева
     * @return дерево категорий
     */
    private List<Category> buildCategoryTree(List<Category> categories) {
        for (Category category : categories) {
            List<Category> subcategories = categoryRepository.findByParentCategoryId(category.getId());
            category.setSubcategories(buildCategoryTree(subcategories));
        }
        return categories;
    }

    /**
     * Проверить наличие циклических зависимостей в дереве категорий
     *
     * @param category        проверяемая категория
     * @param potentialParent потенциальный родитель
     * @return true если обнаружена циклическая зависимость
     */
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

    // ========== ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ ДЛЯ БИЗНЕС-ЛОГИКИ ==========

    /**
     * Переместить категорию к новому родителю
     *
     * @param categoryId  идентификатор перемещаемой категории
     * @param newParentId идентификатор нового родителя (null для корня)
     * @return обновленная категория
     */
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

    /**
     * Получить конечные категории (без подкатегорий)
     *
     * @return список конечных категорий
     */
    public List<Category> getLeafCategories() {
        return categoryRepository.findLeafCategories();
    }

    /**
     * Получить количество товаров для всех категорий
     *
     * @return Map, где ключ - ID категории, значение - количество товаров
     */
    public Map<Long, Long> getProductCountsForAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        Map<Long, Long> counts = new HashMap<>();

        for (Category category : categories) {
            counts.put(category.getId(), getTotalProductCountIncludingSubcategories(category.getId()));
        }

        return counts;
    }

    /**
     * Найти категории по списку идентификаторов
     *
     * @param ids список идентификаторов категорий
     * @return список найденных категорий
     */
    public List<Category> findAllById(Iterable<Long> ids) {
        return categoryRepository.findAllById(ids);
    }

    /**
     * Проверить существование категории по идентификатору
     *
     * @param id идентификатор категории
     * @return true если категория существует
     */
    public boolean existsById(Long id) {
        return categoryRepository.existsById(id);
    }

    /**
     * Получить общее количество категорий
     *
     * @return количество категорий
     */
    public long count() {
        return categoryRepository.count();
    }

    /**
     * Удалить все категории (использовать с осторожностью)
     */
    public void deleteAll() {
        categoryRepository.deleteAll();
    }

    /**
     * Удалить категорию по объекту
     *
     * @param category категория для удаления
     */
    public void delete(Category category) {
        categoryRepository.delete(category);
    }

    /**
     * Удалить категории по списку
     *
     * @param categories список категорий для удаления
     */
    public void deleteAll(Iterable<? extends Category> categories) {
        categoryRepository.deleteAll(categories);
    }

    /**
     * Сохранить список категорий
     *
     * @param categories список категорий для сохранения
     * @return список сохраненных категорий
     */
    public List<Category> saveAll(Iterable<Category> categories) {
        return categoryRepository.saveAll(categories);
    }
}