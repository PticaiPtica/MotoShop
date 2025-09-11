package ru.academy.homework.motoshop.controlles;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.academy.homework.motoshop.model.Category;
import ru.academy.homework.motoshop.repository.CategoryRepository;
import ru.academy.homework.motoshop.services.CategoryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


@Controller

public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private final CategoryService categoryService;

    @Autowired
    public MainController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


    @GetMapping("/")
    public String index(Model model) {
        List<Category> categories = categoryService.getAllCategories();


        model.addAttribute("categories", categories);
        return "index";
    }
}

