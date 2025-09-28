package ru.academy.homework.motoshop.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.academy.homework.motoshop.model.Product;
import ru.academy.homework.motoshop.services.ProductService;

import java.util.Optional;

@Controller
public class ProductViewController {

    @Autowired
    private ProductService productService;

    @GetMapping("/product")
    public String getProductPage(@RequestParam Long id, Model model) {
        try {
            Optional<Product> product = productService.getProductById(id);
            if (product.isPresent()) {
                model.addAttribute("product", product.get());
            } else {
                model.addAttribute("error", "Продукт не найден");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка загрузки продукта: " + e.getMessage());
        }
        return "product";
    }
}