package ru.academy.homework.motoshop.services;

import ru.academy.homework.motoshop.model.Category;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления категориями товаров.
 * Предоставляет методы для CRUD операций, работы с иерархией категорий и статистики.
 */
public interface CategoryService {

    /**
     * Возвращает список всех категорий.
     * Аналог метода {@link #findAll()}.
     *
     * @return список всех категорий
     */
    List<Category> getAllCategories();

    /**
     * Находит категорию по её идентификатору.
     * Аналог метода {@link #findById(Long)}.
     *
     * @param id идентификатор категории
     * @return категория в виде {@link Optional}
     */
    Optional<Category> getCategoryById(Long id);

    /**
     * Создает новую категорию.
     *
     * @param category объект категории для создания
     * @return созданная категория
     */
    Category createCategory(Category category);

    /**
     * Обновляет существующую категорию.
     *
     * @param id идентификатор обновляемой категории
     * @param categoryDetails объект с обновленными данными
     * @return обновленная категория
     */
    Category updateCategory(Long id, Category categoryDetails);

    /**
     * Удаляет категорию по идентификатору.
     *
     * @param id идентификатор удаляемой категории
     */
    void deleteById(Long id);

    /**
     * Возвращает список корневых категорий (категорий без родителя).
     *
     * @return список корневых категорий
     */
    List<Category> getRootCategories();

    /**
     * Возвращает список подкатегорий для указанной родительской категории.
     *
     * @param parentId идентификатор родительской категории
     * @return список подкатегорий
     */
    List<Category> getSubcategories(Long parentId);

    /**
     * Возвращает полное дерево категорий с вложенностью.
     *
     * @return иерархическое дерево категорий
     */
    List<Category> getCategoryTree();

    /**
     * Возвращает путь от корневой категории до указанной.
     *
     * @param categoryId идентификатор целевой категории
     * @return список категорий от корня до целевой
     */
    List<Category> getCategoryPath(Long categoryId);

    /**
     * Возвращает количество товаров в указанной категории.
     *
     * @param categoryId идентификатор категории
     * @return количество товаров
     */
    long getProductCount(Long categoryId);

    /**
     * Возвращает общее количество товаров в категории и всех её подкатегориях.
     *
     * @param categoryId идентификатор категории
     * @return суммарное количество товаров
     */
    long getTotalProductCountIncludingSubcategories(Long categoryId);

    /**
     * Ищет категории по названию.
     *
     * @param name название для поиска
     * @return список подходящих категорий
     */
    List<Category> searchCategoriesByName(String name);

    /**
     * Возвращает категории, содержащие товары.
     *
     * @return список категорий с товарами
     */
    List<Category> getCategoriesWithProducts();

    /**
     * Проверяет, может ли категория быть родителем для другой категории.
     *
     * @param categoryId идентификатор проверяемой категории
     * @param potentialParentId идентификатор потенциального родителя
     * @return true если валидация прошла успешно
     */
    boolean isValidParentCategory(Long categoryId, Long potentialParentId);

    /**
     * Проверяет, есть ли товары в категории.
     *
     * @param categoryId идентификатор категории
     * @return true если в категории есть товары
     */

    boolean categoryHasProducts(Long categoryId);

    /**
     * Проверить существование категории по идентификатору
     * @param id идентификатор категории
     * @return true если категория существует
     */
    boolean existsById(Long id);

    /**
     * Проверяет, есть ли подкатегории у категории.
     *
     * @param categoryId идентификатор категории
     * @return true если есть подкатегории
     */
    boolean categoryHasSubcategories(Long categoryId);

    // Новые методы

    /**
     * Находит все категории.
     * Аналог метода {@link #getAllCategories()}.
     *
     * @return список всех категорий
     */
    List<Category> findAll();

    /**
     * Находит категорию по идентификатору.
     * Аналог метода {@link #getCategoryById(Long)}.
     *
     * @param id идентификатор категории
     * @return категория в виде {@link Optional}
     */
    Optional<Category> findById(Long id);

    /**
     * Сохраняет категорию (создает или обновляет существующую).
     *
     * @param category категория для сохранения
     * @return сохраненная категория
     */
    Category save(Category category);
}