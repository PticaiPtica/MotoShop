package ru.academy.homework.motoshop.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.academy.homework.motoshop.config.JwtUtils;
import ru.academy.homework.motoshop.entity.User;
import ru.academy.homework.motoshop.services.UserDetailsImpl;
import ru.academy.homework.motoshop.services.UserService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);


    private final boolean isSecureCookie = true;
    private final int jwtExpirationMs = 24 * 60 * 60 * 1000; // 24 часа

    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Страница входа с проверкой уже аутентифицированных пользователей
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "registered", required = false) String registered,
                            @RequestParam(value = "redirect", required = false) String redirectUrl,
                            HttpServletRequest request,
                            Model model) {

        // Проверяем, не аутентифицирован ли уже пользователь
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isAuthenticated(auth)) {
            logger.debug("User already authenticated: {}, redirecting to dashboard", auth.getName());
            // ИСПРАВЛЕНИЕ: передаем два аргумента
            return "redirect:" + determineRedirectUrl((UserDetailsImpl) auth.getPrincipal(), redirectUrl);
        }

        // Сохраняем URL для редиректа после успешного входа
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            model.addAttribute("redirectUrl", redirectUrl);
        } else {
            // Пытаемся получить URL реферера или предыдущей страницы
            String referer = request.getHeader("Referer");
            if (referer != null && !referer.contains("/api/auth/")) {
                model.addAttribute("redirectUrl", referer);
            }
        }

        if (error != null) {
            model.addAttribute("error", "Неверное имя пользователя или пароль");
        }
        if (registered != null) {
            model.addAttribute("message", "Регистрация успешна! Теперь вы можете войти.");
        }
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    /**
     * Обработка формы входа (HTML)
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String loginForm(@RequestParam String username,
                            @RequestParam String password,
                            @RequestParam(value = "redirect", required = false) String redirectUrl,
                            Model model,
                            HttpServletResponse response) {
        try {
            logger.info("Form login attempt - Username: {}", username);

            // Аутентификация пользователя
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            // Генерация JWT токена
            String jwt = jwtUtils.generateJwtToken(authentication);

            // Установка токена в cookie
            setJwtCookie(response, jwt);

            // Установка аутентификации в SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Определение URL для редиректа
            String targetUrl = determineRedirectUrl(userDetails, redirectUrl);

            logger.info("Login successful for user: {}, redirecting to: {}", username, targetUrl);
            return "redirect:" + targetUrl;

        } catch (BadCredentialsException e) {
            logger.warn("Bad credentials for user: {}", username);
            model.addAttribute("error", "Неверное имя пользователя или пароль");
            model.addAttribute("loginRequest", new LoginRequest());
            return "login";
        } catch (Exception e) {
            logger.error("Login error for user: {}", username, e);
            model.addAttribute("error", "Ошибка входа. Попробуйте позже.");
            model.addAttribute("loginRequest", new LoginRequest());
            return "login";
        }
    }

    /**
     * API вход (JSON)
     */
    @PostMapping(value = "/signin", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> signinApiJson(@RequestBody LoginRequest loginRequest,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        try {
            logger.info("API login attempt - Username: {}", loginRequest.getUsername());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            String jwt = jwtUtils.generateJwtToken(authentication);
            setJwtCookie(response, jwt);

            // Установка аутентификации в SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());

            // ИСПРАВЛЕНИЕ: передаем null как второй параметр, так как для API redirect не используется
            Map<String, Object> responseBody = createAuthResponse(user, userDetails, jwt, null);
            logger.info("API login successful for user: {}", loginRequest.getUsername());

            return ResponseEntity.ok(responseBody);

        } catch (BadCredentialsException e) {
            logger.warn("Bad credentials for user: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Неверное имя пользователя или пароль"));
        } catch (Exception e) {
            logger.error("API login error for user: {}", loginRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Ошибка входа"));
        }
    }

    /**
     * Страница регистрации
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    /**
     * Обработка регистрации
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        // Проверка существования пользователя
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
            logger.info("User registered successfully: {}", registrationDto.getUsername());
            return "redirect:/api/auth/login?registered=true";
        } catch (RuntimeException e) {
            logger.error("Registration error for user: {}", registrationDto.getUsername(), e);
            model.addAttribute("error", "Ошибка при регистрации: " + e.getMessage());
            return "register";
        }
    }
    /**
     * Выход из системы
     */
    @GetMapping("/logout")
    public String logoutGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (auth != null) ? auth.getName() : "unknown";
            logger.info("Starting logout process for user: {}", username);

            // Дополнительная очистка сессии
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
                logger.debug("Session invalidated for user: {}", username);
            }

            // Очищаем SecurityContext
            SecurityContextHolder.clearContext();
            logger.debug("SecurityContext cleared for user: {}", username);

            // Удаляем JWT cookie
            removeJwtCookie(response);
            logger.debug("JWT cookie removed for user: {}", username);

            logger.info("Logout completed successfully for user: {}", username);
            return "redirect:/?logout=true&timestamp=" + System.currentTimeMillis();

        } catch (Exception e) {
            logger.error("Logout error", e);
            return "redirect:/?error=logout_failed";
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (auth != null) ? auth.getName() : "unknown";
            logger.info("Starting API logout process for user: {}", username);

            // 1. Очищаем SecurityContext
            SecurityContextHolder.clearContext();
            logger.debug("SecurityContext cleared for API logout, user: {}", username);

            // 2. Инвалидируем сессию
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
                logger.debug("Session invalidated for API logout, user: {}", username);
            }

            // 3. Удаляем куки с ПРАВИЛЬНЫМ именем
            removeAllAuthCookies(response);
            logger.debug("All auth cookies removed for API logout, user: {}", username);

            // 4. Заголовки против кэширования
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Logout successful");

            logger.info("API logout completed successfully for user: {}", username);
            return ResponseEntity.ok().body(responseBody);

        } catch (Exception e) {
            logger.error("API logout error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Logout failed"));
        }
    }
    private void removeAllAuthCookies(HttpServletResponse response) {
        // Удаляем куку с ПРАВИЛЬНЫМ именем
        removeCookie(response, "jwtToken"); // исправлено с "jwt" на "jwtToken"
        removeCookie(response, "JSESSIONID");
        removeCookie(response, "remember-me");
    }

    private void removeCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(isSecureCookie); // используйте ту же настройку, что и при установке
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }



    /**
     * Валидация JWT токена
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(Authentication authentication, HttpServletRequest request) {
        try {
            if (isAuthenticated(authentication)) {
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                User user = userService.findByUsername(userDetails.getUsername());

                Map<String, Object> response = new HashMap<>();
                response.put("status", "valid");
                response.put("user", authentication.getName());
                response.put("roles", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));
                response.put("id", user.getId());
                response.put("username", user.getUsername());
                response.put("email", user.getEmail());

                logger.debug("Token validation successful for user: {}", authentication.getName());
                return ResponseEntity.ok(response);
            }

            logger.warn("Token validation failed - user not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Токен недействителен"));

        } catch (Exception e) {
            logger.error("Token validation error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Ошибка валидации токена"));
        }
    }

    /**
     * Проверка аутентификации пользователя
     */
    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken);
    }

    /**
     * Определение URL для редиректа после входа
     */
    private String determineRedirectUrl(UserDetailsImpl userDetails, String requestedRedirect) {

        if (requestedRedirect != null && !requestedRedirect.isEmpty()) {
            return requestedRedirect;
        }

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        boolean isModerator = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_MODERATOR"));

        if (isAdmin) {
            return "/admin/dashboard";
        } else if (isModerator) {
            return "/moderator/dashboard";
        } else {
            return "/profile";
        }
    }

    /**
     * Установка JWT токена в cookie
     */
    private void setJwtCookie(HttpServletResponse response, String jwt) {
        try {
            Cookie jwtCookie = new Cookie("jwtToken", jwt);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(isSecureCookie);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(jwtExpirationMs / 1000); // Конвертируем в секунды
            response.addCookie(jwtCookie);
            logger.debug("JWT cookie set successfully");
        } catch (Exception e) {
            logger.error("Error setting JWT cookie", e);
        }
    }

    /**
     * Удаление JWT токена из cookie
     */
    private void removeJwtCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null); // или имя вашей JWT куки
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }


    /**
     * Создание ответа с данными аутентификации (перегруженная версия без redirectUrl)
     */
    private Map<String, Object> createAuthResponse(User user, UserDetailsImpl userDetails, String jwt) {
        return createAuthResponse(user, userDetails, jwt, null);
    }

    /**
     * Создание ответа с данными аутентификации (полная версия)
     */
    private Map<String, Object> createAuthResponse(User user, UserDetailsImpl userDetails, String jwt, String redirectUrl) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("token", jwt);
        responseBody.put("type", "Bearer");
        responseBody.put("id", user.getId());
        responseBody.put("username", user.getUsername());
        responseBody.put("email", user.getEmail());
        responseBody.put("enabled", user.isEnabled());
        responseBody.put("redirectUrl", determineRedirectUrl(userDetails, redirectUrl));
        responseBody.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        responseBody.put("expiresIn", jwtExpirationMs);
        return responseBody;
    }


    public static class LoginRequest {
        private String username;
        private String password;

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
    }

    public static class UserRegistrationDto {
        private String username;
        private String password;
        private String email;

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