package ru.academy.homework.motoshop.controlles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.academy.homework.motoshop.model.Category;
import ru.academy.homework.motoshop.model.Product;
import ru.academy.homework.motoshop.model.ProductAttribute;
import ru.academy.homework.motoshop.model.ProductImage;
import ru.academy.homework.motoshop.services.CategoryService;
import ru.academy.homework.motoshop.services.ProductService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @Autowired
    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    // GET - Получить все продукты с полной информацией
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProductsWithDetails();
        return ResponseEntity.ok(products);
    }

    // GET - Получить продукт по ID с полной информацией
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductByIdWithDetails(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST - Создать новый продукт с обработкой связанных сущностей
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        // Проверяем и устанавливаем категорию
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            Category category = categoryService.getCategoryById(product.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        // Устанавливаем обратные ссылки для изображений
        if (product.getImages() != null) {
            for (ProductImage image : product.getImages()) {
                image.setProduct(product);
            }
        }

        // Устанавливаем обратные ссылки для атрибутов
        if (product.getAttributes() != null) {
            for (ProductAttribute attribute : product.getAttributes()) {
                attribute.setProduct(product);
            }
        }

        Product savedProduct = productService.saveProduct(product);
        return ResponseEntity.ok(savedProduct);
    }

    // PUT - Обновить существующий продукт
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        try {
            Product updatedProduct = productService.updateProductWithDetails(id, productDetails);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PATCH - Частичное обновление (например, только количество)
    @PatchMapping("/{id}/quantity")
    public ResponseEntity<Product> updateProductQuantity(@PathVariable Long id, @RequestParam int quantity) {
        try {
            Product updatedProduct = productService.updateProductQuantity(id, quantity);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PATCH - Обновление цены
    @PatchMapping("/{id}/price")
    public ResponseEntity<Product> updateProductPrice(@PathVariable Long id, @RequestParam BigDecimal price) {
        try {
            Product updatedProduct = productService.updateProductPrice(id, price);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE - Удалить продукт
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
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
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<Product> products = productService.findProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
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

    // GET - Изображения продукта
    @GetMapping("/{id}/images")
    public ResponseEntity<List<ProductImage>> getProductImages(@PathVariable Long id) {
        List<ProductImage> images = productService.getProductImages(id);
        return ResponseEntity.ok(images);
    }

    // GET - Атрибуты продукта
    @GetMapping("/{id}/attributes")
    public ResponseEntity<List<ProductAttribute>> getProductAttributes(@PathVariable Long id) {
        List<ProductAttribute> attributes = productService.getProductAttributes(id);
        return ResponseEntity.ok(attributes);
    }

    // POST - Добавить изображение к продукту
    @PostMapping("/{id}/images")
    public ResponseEntity<ProductImage> addImageToProduct(@PathVariable Long id, @RequestBody ProductImage image) {
        ProductImage savedImage = productService.addImageToProduct(id, image);
        return ResponseEntity.ok(savedImage);
    }

    // POST - Добавить атрибут к продукту
    @PostMapping("/{id}/attributes")
    public ResponseEntity<ProductAttribute> addAttributeToProduct(@PathVariable Long id, @RequestBody ProductAttribute attribute) {
        ProductAttribute savedAttribute = productService.addAttributeToProduct(id, attribute);
        return ResponseEntity.ok(savedAttribute);
    }
}