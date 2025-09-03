package ru.academy.homework.motoshop.Initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.academy.homework.motoshop.model.Category;
import ru.academy.homework.motoshop.repository.CategoryRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        Map<String, String> categoryImages = Map.of(
                "Шлем", "/images/categories/helmet.jpg",
                "Перчатки", "/images/categories/gloves.jpg",
                "Куртка", "/images/categories/jacket.jpg",
                "Штаны", "/images/categories/pants.jpg",
                "Обувь", "/images/categories/boots.jpg"
        );

        for (String name : Arrays.asList("Шлем", "Перчатки", "Куртка", "Штаны", "Обувь")) {
            if (!categoryRepository.existsByName(name)) {
                Category category = new Category();
                category.setName(name);
                category.setDescription("Описание " + name);
                category.setImageUrl(categoryImages.get(name));
                category.setActive(true);
                categoryRepository.save(category);
            }
        }
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