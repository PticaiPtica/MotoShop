package ru.academy.homework.motoshop.controlles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.academy.homework.motoshop.model.Category;
import ru.academy.homework.motoshop.services.CategoryService;

import java.util.List;

@Controller
public class MainController {

    private final CategoryService categoryService;

    @Autowired
    public MainController(CategoryService categoryService) {
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

}

