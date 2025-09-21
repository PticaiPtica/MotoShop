package ru.academy.homework.motoshop.services;

import org.springframework.transaction.annotation.Transactional;
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


    @Transactional(readOnly = true)
    long getProductCount(Long categoryId);

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

    long count();

    void deleteAll();

    void delete(Category category);

    List<Category> saveAll(Iterable<Category> categories);
}