package ru.academy.homework.motoshop.controlles;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.academy.homework.motoshop.config.JwtUtils;
import ru.academy.homework.motoshop.entity.User;
import ru.academy.homework.motoshop.payload.JwtResponse;
import ru.academy.homework.motoshop.payload.MessageResponse;
import ru.academy.homework.motoshop.payload.request.LoginRequest;
import ru.academy.homework.motoshop.services.UserDetailsImpl;
import ru.academy.homework.motoshop.services.UserService;


@Controller
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "registered", required = false) String registered,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Неверное имя пользователя или пароль");
        }
        if (registered != null) {
            model.addAttribute("message", "Регистрация успешна! Теперь вы можете войти.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        // Проверка на существование пользователя с таким же username или email
        if (userService.existsByUsername(registrationDto.getUsername())) {
            model.addAttribute("error", "Пользователь с таким именем уже существует");
            return "register";
        }

        if (userService.existsByEmail(registrationDto.getEmail())) {
            model.addAttribute("error", "Пользователь с таким email уже существует");
            return "register";
        }

        try {
            userService.registerUser(
                    registrationDto.getUsername(),
                    registrationDto.getEmail(),
                    registrationDto.getPassword()
            );
            return "redirect:/api/auth/login?registered=true";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Ошибка при регистрации: " + e.getMessage());
            return "register";
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());

            return ResponseEntity.ok(new JwtResponse(jwt, user));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Неверное имя пользователя или пароль"));
        }
    }

    // DTO для регистрации
    public static class UserRegistrationDto {
        @NotBlank(message = "Имя пользователя обязательно")
        @Size(min = 3, max = 20, message = "Имя пользователя должно содержать от 3 до 20 символов")
        private String username;

        @NotBlank(message = "Пароль обязателен")
        @Size(min = 6, message = "Пароль должен содержать не менее 6 символов")
        private String password;

        @NotBlank(message = "Email обязателен")
        @Email(message = "Некорректный email")
        private String email;

        // Геттеры и сеттеры
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}