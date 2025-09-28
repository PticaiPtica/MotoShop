package ru.academy.homework.motoshop.Initializer;


import ru.academy.homework.motoshop.model.Category;
import ru.academy.homework.motoshop.model.Product;
import ru.academy.homework.motoshop.repository.CategoryRepository;
import ru.academy.homework.motoshop.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Order(2)
public class ProductInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ProductInitializer.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ProductInitializer(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Starting product initialization...");
        initializeProducts();
    }

    private void initializeProducts() {
        // Получаем все категории
        List<Category> categories = categoryRepository.findAll();
        Map<String, Category> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getName, category -> category));

        // Проверяем, есть ли уже продукты
        if (productRepository.count() == 0) {
            createProducts(categoryMap);
            logger.info("Product initialization completed: 10 products created");
        } else {
            logger.info("Products already exist, skipping initialization.");
        }
    }

    private void createProducts(Map<String, Category> categoryMap) {
        // Шлемы
        createAndSaveProduct("Шлем AGV K6", "Спортивный шлем AGV K6 с карбоновым корпусом", 45000.0, 5,
                categoryMap.get("Шлем"), "AGV", "K6", "/images/products/agv-k6.jpg");
        createAndSaveProduct("Шлем Shoei X-Spirit 3", "Шлем премиум-класса для спортивной езды", 65000.0, 3,
                categoryMap.get("Шлем"), "Shoei", "X-Spirit 3", "/images/products/shoei-xspirit.jpg");

        // Перчатки
        createAndSaveProduct("Перчатки Alpinestars SP-8", "Кожаные перчатки с защитой пальцев", 12000.0, 8,
                categoryMap.get("Перчатки"), "Alpinestars", "SP-8", "/images/products/a-stars-sp8.jpg");
        createAndSaveProduct("Перчатки Dainese 4 Stroke", "Перчатки с вентиляцией для летней езды", 15000.0, 6,
                categoryMap.get("Перчатки"), "Dainese", "4 Stroke", "/images/products/dainese-4stroke.jpg");

        // Куртки
        createAndSaveProduct("Куртка Rev'it Tornado 3", "Летняя куртка с защитой уровня AA", 25000.0, 4,
                categoryMap.get("Куртка"), "Rev'it", "Tornado 3", "/images/products/revit-tornado.jpg");
        createAndSaveProduct("Куртка Dainese Carve Master 3", "Куртка для приключенческого туризма", 35000.0, 2,
                categoryMap.get("Куртка"), "Dainese", "Carve Master 3", "/images/products/dainese-carve.jpg");

        // Штаны
        createAndSaveProduct("Штаны Rev'it Sand 4", "Джинсы с защитой для городской езды", 18000.0, 7,
                categoryMap.get("Штаны"), "Rev'it", "Sand 4", "/images/products/revit-sand.jpg");
        createAndSaveProduct("Штаны Alpinestars Crank", "Штаны для эндуро с усиленной защитой", 22000.0, 5,
                categoryMap.get("Штаны"), "Alpinestars", "Crank", "/images/products/a-stars-crank.jpg");

        // Обувь
        createAndSaveProduct("Ботинки TCX Blend", "Городские ботинки с защитой щиколотки", 16000.0, 10,
                categoryMap.get("Обувь"), "TCX", "Blend", "/images/products/tcx-blend.jpg");
        createAndSaveProduct("Ботинки Alpinestars SMX-6", "Спортивные ботинки для трек-дней", 28000.0, 4,
                categoryMap.get("Обувь"), "Alpinestars", "SMX-6", "/images/products/a-stars-smx6.jpg");
    }

    private void createAndSaveProduct(String name, String description, Double price, Integer stockQuantity,
                                      Category category, String brand, String model, String imageUrl) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setCategory(category);
        product.setBrand(brand);
        product.setModel(model);
        product.setImageUrl(imageUrl);
        product.setAvailable(stockQuantity > 0);

        productRepository.save(product);
        logger.debug("Created product: {}", name);
    }
}