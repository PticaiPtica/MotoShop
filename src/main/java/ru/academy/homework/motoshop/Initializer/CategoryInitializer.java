package ru.academy.homework.motoshop.Initializer;

import org.springframework.core.annotation.Order;
import ru.academy.homework.motoshop.model.Category;
import ru.academy.homework.motoshop.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Order(1)
public class CategoryInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CategoryInitializer.class);

    private static final String HELMET = "Шлем";
    private static final String GLOVES = "Перчатки";
    private static final String JACKET = "Куртка";
    private static final String PANTS = "Штаны";
    private static final String BOOTS = "Обувь";

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
       // logger.info("Starting category initialization...");
        initializeCategories();
    }

    private void initializeCategories() {
        Map<String, CategoryData> categoriesData = createCategoriesData();

        // Получаем все существующие категории одним запросом
        List<Category> existingCategories = categoryRepository.findAll();
        Map<String, Category> existingCategoriesMap = existingCategories.stream()
                .collect(Collectors.toMap(Category::getName, category -> category));

        int createdCount = 0;
        int updatedCount = 0;

        for (Map.Entry<String, CategoryData> entry : categoriesData.entrySet()) {
            String name = entry.getKey();
            CategoryData data = entry.getValue();

            Category existingCategory = existingCategoriesMap.get(name);

            if (existingCategory == null) {
                // Создаем новую категорию через сеттеры
                Category newCategory = createNewCategory(name, data);
                categoryRepository.save(newCategory);
                createdCount++;
              //  logger.debug("Created category: {}", name);
            } else {
                // Обновляем существующую категорию
                if (updateExistingCategory(existingCategory, data)) {
                    updatedCount++;
                 //   logger.debug("Updated category: {}", name);
                }
            }
        }

      //  logger.info("Category initialization completed: {} created, {} updated",             createdCount, updatedCount);
    }

    private Map<String, CategoryData> createCategoriesData() {
        Map<String, CategoryData> data = new LinkedHashMap<>();
        data.put(HELMET, new CategoryData(
                "/images/categories/helmet.jpg",
                "Защитные шлемы для мотоциклистов различных типов и стилей",
                10
        ));
        data.put(GLOVES, new CategoryData(
                "/images/categories/gloves.jpg",
                "Мотоциклетные перчатки для защиты и комфорта",
                20
        ));
        data.put(JACKET, new CategoryData(
                "/images/categories/jacket.jpg",
                "Защитные мотоциклетные куртки с armor-вставками",
                30
        ));
        data.put(PANTS, new CategoryData(
                "/images/categories/pants.jpg",
                "Мотоциклетные штаны и джинсы с защитой",
                40
        ));
        data.put(BOOTS, new CategoryData(
                "/images/categories/boots.jpg",
                "Мотоциклетные ботинки и обувь для безопасности",
                50
        ));
        return data;
    }

    private Category createNewCategory(String name, CategoryData data) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(data.description());
        category.setImageUrl(data.imageUrl());


        return category;
    }

    private boolean updateExistingCategory(Category category, CategoryData newData) {
        boolean needsUpdate = false;

        if (!Objects.equals(newData.description(), category.getDescription())) {
            category.setDescription(newData.description());
            needsUpdate = true;
        }

        if (!Objects.equals(newData.imageUrl(), category.getImageUrl())) {
            category.setImageUrl(newData.imageUrl());
            needsUpdate = true;
        }


        return needsUpdate;
    }

    // Record для хранения данных категории
    private record CategoryData(String imageUrl, String description, Integer sortOrder) {
    }
}