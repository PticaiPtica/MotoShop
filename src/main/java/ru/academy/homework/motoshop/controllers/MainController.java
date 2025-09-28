package ru.academy.homework.motoshop.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.academy.homework.motoshop.model.Category;
import ru.academy.homework.motoshop.services.CategoryService;
import ru.academy.homework.motoshop.services.ProductService;

import java.util.List;

@Controller
public class MainController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @Autowired
    public MainController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        // Добавляем информацию об аутентификации
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
            model.addAttribute("isAdmin", authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        }

        // Получаем все категории для отображения на главной странице
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        return "index";
    }

    @GetMapping("/catalog")
    public String getCatalogPage(
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            Model model) {

        try {
            if (categoryId != null) {
                // Продукты по категории
                model.addAttribute("products", productService.findProductsByCategory(categoryId));
                model.addAttribute("selectedCategory", categoryService.getCategoryById(categoryId).orElse(null));
            } else {
                // Все продукты
                model.addAttribute("products", productService.getAllProducts());
            }

            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("categoryId", categoryId);

            return "products";

        } catch (Exception e) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("error", "Ошибка загрузки данных: " + e.getMessage());
            return "products";
        }
    }
}

