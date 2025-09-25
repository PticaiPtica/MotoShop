package ru.academy.homework.motoshop.controlles;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.academy.homework.motoshop.services.UserDetailsImpl;

@Controller
public class ProfileController {

    @GetMapping("/profile")
    public String profilePage(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            model.addAttribute("user", userDetails.getUser());
            return "profile";
        }
        return "redirect:/auth/login";
    }
}