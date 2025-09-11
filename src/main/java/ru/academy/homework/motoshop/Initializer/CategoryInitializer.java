package ru.academy.homework.motoshop.Initializer;

import ru.academy.homework.motoshop.model.Category;
import ru.academy.homework.motoshop.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

@Component
public class CategoryInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CategoryInitializer.class);

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        initializeCategories();
    }

    private void initializeCategories() {
        Map<String, String> categoryImages = Map.of(
                "Шлем", "/images/categories/helmet.jpg",
                "Перчатки", "/images/categories/gloves.jpg",
                "Куртка", "/images/categories/jacket.jpg",
                "Штаны", "/images/categories/pants.jpg",
                "Обувь", "/images/categories/boots.jpg"
        );

        Map<String, String> categoryDescriptions = Map.of(
                "Шлем", "Защитные шлемы для мотоциклистов различных типов и стилей",
                "Перчатки", "Мотоциклетные перчатки для защиты и комфорта",
                "Куртка", "Защитные мотоциклетные куртки с armor-вставками",
                "Штаны", "Мотоциклетные штаны и джинсы с защитой",
                "Обувь", "Мотоциклетные ботинки и обувь для безопасности"
        );

        for (String name : Arrays.asList("Шлем", "Перчатки", "Куртка", "Штаны", "Обувь")) {
            if (!categoryRepository.existsByName(name)) {
                Category category = new Category();
                category.setName(name);
                category.setDescription(categoryDescriptions.get(name));
                category.setImageUrl(categoryImages.get(name));
                category.setActive(true);
                category.setSortOrder(getSortOrder(name));

                categoryRepository.save(category);
                logger.info("Created category: {} with image: {}", name, categoryImages.get(name));
            } else {
                // Обновляем существующие категории, если нужно
                Category existingCategory = categoryRepository.findByName(name);
                if (existingCategory.getImageUrl() == null) {
                    existingCategory.setImageUrl(categoryImages.get(name));
                    existingCategory.setDescription(categoryDescriptions.get(name));
                    existingCategory.setSortOrder(getSortOrder(name));
                    categoryRepository.save(existingCategory);
                    logger.info("Updated category: {} with image: {}", name, categoryImages.get(name));
                }
            }
        }

        logger.info("Category initialization completed");
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