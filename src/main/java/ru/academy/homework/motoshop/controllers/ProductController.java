package ru.academy.homework.motoshop.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import ru.academy.homework.motoshop.model.Category;
import ru.academy.homework.motoshop.model.Product;
import ru.academy.homework.motoshop.services.CategoryService;
import ru.academy.homework.motoshop.services.ProductService;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @Autowired
    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    // GET - Получить все продукты с пагинацией
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        Page<Product> productsPage = productService.getAllProducts(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("products", productsPage.getContent());
        response.put("currentPage", productsPage.getNumber());
        response.put("totalItems", productsPage.getTotalElements());
        response.put("totalPages", productsPage.getTotalPages());

        return ResponseEntity.ok(response);
    }


    // GET - Получить продукт по ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST - Создать новый продукт
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        try {
            // Валидация обязательных полей
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Название продукта обязательно");
            }
            if (product.getPrice() == null || product.getPrice() <= 0) {
                return ResponseEntity.badRequest().body("Цена должна быть положительной");
            }

            // Проверяем и устанавливаем категорию
            if (product.getCategory() != null && product.getCategory().getId() != null) {
                Category category = categoryService.getCategoryById(product.getCategory().getId())
                        .orElseThrow(() -> new RuntimeException("Категория не найдена"));
                product.setCategory(category);
            }

            Product savedProduct = productService.saveProduct(product);
            return ResponseEntity.ok(savedProduct);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при создании продукта: " + e.getMessage());
        }
    }

    // PUT - Обновить существующий продукт
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        try {
            // Проверяем существование продукта
            Product existingProduct = productService.getProductById(id)
                    .orElseThrow(() -> new RuntimeException("Продукт не найден"));

            // Проверяем и устанавливаем категорию, если она указана
            if (productDetails.getCategory() != null && productDetails.getCategory().getId() != null) {
                Category category = categoryService.getCategoryById(productDetails.getCategory().getId())
                        .orElseThrow(() -> new RuntimeException("Категория не найдена"));
                productDetails.setCategory(category);
            }

            // Обновляем поля
            if (productDetails.getName() != null) {
                existingProduct.setName(productDetails.getName());
            }
            if (productDetails.getDescription() != null) {
                existingProduct.setDescription(productDetails.getDescription());
            }
            if (productDetails.getPrice() != null) {
                existingProduct.setPrice(productDetails.getPrice());
            }
            if (productDetails.getStockQuantity() != null) {
                existingProduct.setStockQuantity(productDetails.getStockQuantity());
            }
            if (productDetails.getCategory() != null) {
                existingProduct.setCategory(productDetails.getCategory());
            }
            if (productDetails.getBrand() != null) {
                existingProduct.setBrand(productDetails.getBrand());
            }
            if (productDetails.getModel() != null) {
                existingProduct.setModel(productDetails.getModel());
            }
            if (productDetails.getImageUrl() != null) {
                existingProduct.setImageUrl(productDetails.getImageUrl());
            }
            if (productDetails.getAvailable() != null) {
                existingProduct.setAvailable(productDetails.getAvailable());
            }

            Product updatedProduct = productService.saveProduct(existingProduct);
            return ResponseEntity.ok(updatedProduct);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при обновлении продукта: " + e.getMessage());
        }
    }

    // PATCH - Частичное обновление продукта
    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdateProduct(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        try {
            Product product = productService.getProductById(id)
                    .orElseThrow(() -> new RuntimeException("Продукт не найден"));

            // Обрабатываем каждое поле для обновления
            updates.forEach((key, value) -> {
                switch (key) {
                    case "name":
                        product.setName((String) value);
                        break;
                    case "description":
                        product.setDescription((String) value);
                        break;
                    case "price":
                        product.setPrice(((Number) value).doubleValue());
                        break;
                    case "stockQuantity":
                        product.setStockQuantity(((Number) value).intValue());
                        break;
                    case "category":
                        if (value instanceof Map) {
                            Map<?, ?> categoryMap = (Map<?, ?>) value;
                            if (categoryMap.get("id") != null) {
                                Long categoryId = ((Number) categoryMap.get("id")).longValue();
                                Category category = categoryService.getCategoryById(categoryId)
                                        .orElseThrow(() -> new RuntimeException("Категория не найдена"));
                                product.setCategory(category);
                            }
                        }
                        break;
                    case "brand":
                        product.setBrand((String) value);
                        break;
                    case "model":
                        product.setModel((String) value);
                        break;
                    case "imageUrl":
                        product.setImageUrl((String) value);
                        break;
                    case "available":
                        product.setAvailable((Boolean) value);
                        break;
                }
            });

            Product updatedProduct = productService.saveProduct(product);
            return ResponseEntity.ok(updatedProduct);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при обновлении продукта: " + e.getMessage());
        }
    }

    // DELETE - Удалить продукт
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok().body("Продукт успешно удален");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при удалении продукта: " + e.getMessage());
        }
    }

    // GET - Поиск продуктов по названию
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String name) {
        List<Product> products = productService.searchProductsByName(name);
        return ResponseEntity.ok(products);
    }

    // GET - Фильтрация продуктов по цене
    @GetMapping("/filter/price")
    public ResponseEntity<List<Product>> filterByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {
        List<Product> products = productService.findProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }
    @GetMapping("/catalog")
    public String getCatalogPage(
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            Model model) {

        try {
            if (categoryId != null) {
                // Продукты по категории
                model.addAttribute("products", productService.findAllProductsById(categoryId));
                model.addAttribute("selectedCategory", categoryService.findById(categoryId));
            } else {
                // Все продукты
                model.addAttribute("products", productService.findAllProducts());
            }

            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("categoryId", categoryId);

            return "products";

        } catch (Exception e) {
            // Если есть ошибка, все равно показываем страницу с пустыми данными
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("error", "Ошибка загрузки данных: " + e.getMessage());
            return "products";
        }
    }

    // GET - Продукты по категории
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productService.findProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    // GET - Продукты с низким количеством
    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts(@RequestParam(defaultValue = "10") int threshold) {
        List<Product> products = productService.findLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }

    // GET - Активные продукты
    @GetMapping("/active")
    public ResponseEntity<List<Product>> getActiveProducts() {
        List<Product> products = productService.findActiveProducts();
        return ResponseEntity.ok(products);
    }


    // Дополнительные методы для API, если нужно
    @GetMapping("/api/products")
    @ResponseBody
    public Page<Product> getProductsApi(
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);

        if (categoryId != null) {
            return productService.findByCategoryId(categoryId, pageable);
        } else {
            return productService.findAll(pageable);
        }
    }

    // GET - Статистика продуктов
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getProductStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", productService.getTotalProductsCount());
        stats.put("activeProducts", productService.getActiveProductsCount());
        stats.put("outOfStockProducts", productService.getOutOfStockProductsCount());
        stats.put("lowStockProducts", productService.getLowStockProductsCount(10));

        return ResponseEntity.ok(stats);
    }

    // POST - Обновить изображение продукта
    @PostMapping("/{id}/image")
    public ResponseEntity<?> updateProductImage(@PathVariable Long id, @RequestParam String imageUrl) {
        try {
            Product product = productService.getProductById(id)
                    .orElseThrow(() -> new RuntimeException("Продукт не найден"));
            product.setImageUrl(imageUrl);
            Product updatedProduct = productService.saveProduct(product);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при обновлении изображения: " + e.getMessage());
        }
    }

    // PATCH - Обновить статус доступности
    @PatchMapping("/{id}/availability")
    public ResponseEntity<?> updateProductAvailability(@PathVariable Long id, @RequestParam boolean available) {
        try {
            Product product = productService.getProductById(id)
                    .orElseThrow(() -> new RuntimeException("Продукт не найден"));
            product.setAvailable(available);
            Product updatedProduct = productService.saveProduct(product);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при обновлении статуса: " + e.getMessage());
        }
    }
}