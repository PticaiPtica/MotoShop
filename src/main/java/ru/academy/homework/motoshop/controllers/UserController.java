package ru.academy.homework.motoshop.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.academy.homework.motoshop.services.ProductService;
import ru.academy.homework.motoshop.services.CategoryService;

@Controller
@RequestMapping("/user")
public class UserController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public UserController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    // Главная страница пользователя (каталог)
    @GetMapping("/catalog")
    public String userCatalog(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Проверяем, аутентифицирован ли пользователь
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/api/auth/login";
        }

        // Добавляем информацию о пользователе в модель
        model.addAttribute("username", authentication.getName());
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", categoryService.getAllCategories());

        return "catalog"; // Используем ваш существующий catalog.html
    }

    // Профиль пользователя
    @GetMapping("/profile")
    public String userProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/api/auth/login";
        }

        model.addAttribute("username", authentication.getName());
        model.addAttribute("roles", authentication.getAuthorities());

        return "profile";
    }

    // Корзина пользователя
    @GetMapping("/cart")
    public String userCart(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/api/auth/login";
        }

        model.addAttribute("username", authentication.getName());

        return "cart";
    }
}