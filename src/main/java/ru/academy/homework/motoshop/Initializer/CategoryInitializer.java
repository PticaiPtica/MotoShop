package ru.academy.homework.motoshop.Initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.academy.homework.motoshop.model.Category;
import ru.academy.homework.motoshop.repository.CategoryRepository;


import java.util.Arrays;
import java.util.List;

@Component
public class CategoryInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CategoryInitializer.class);

    private final CategoryRepository categoryRepository;

    public CategoryInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        initializeCategories();
    }

    private void initializeCategories() {
        List<String> categoryNames = Arrays.asList(
                "Шлем", "Перчатки", "Куртка", "Штаны", "Обувь"
        );

        int createdCount = 0;
        int existingCount = 0;

        for (String categoryName : categoryNames) {
            if (!categoryRepository.existsByName(categoryName)) {
                Category category = new Category();
                category.setName(categoryName);
                category.setDescription("Категория товаров: " + categoryName);
                category.setActive(true);
                category.setSortOrder(getSortOrder(categoryName));

                categoryRepository.save(category);
                createdCount++;

                logger.info("Создана категория: {}", categoryName);
            } else {
                existingCount++;
                logger.debug("Категория уже существует: {}", categoryName);
            }
        }

        logger.info("Инициализация категорий завершена. Создано: {}, Существовало: {}",
                createdCount, existingCount);
    }

    private Integer getSortOrder(String categoryName) {
        return switch (categoryName) {
            case "Шлем" -> 10;
            case "Перчатки" -> 20;
            case "Куртка" -> 30;
            case "Штаны" -> 40;
            case "Обувь" -> 50;
            default -> 100;
        };
    }
}