package ru.academy.homework.motoshop.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.academy.homework.motoshop.entity.RoleName;
import ru.academy.homework.motoshop.entity.User;
import ru.academy.homework.motoshop.services.UserService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @Autowired
    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String usersPage(Model model) {
        model.addAttribute("usersCount", userService.getUsersCount());

        // Получаем названия ролей из enum
        List<String> roles = Arrays.stream(RoleName.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        model.addAttribute("roles", roles);

        return "admin/users";
    }

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort) {

        Map<String, Object> response = new HashMap<>();

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
            Page<User> usersPage = userService.findAllUsers(pageable);

            // Преобразуем пользователей для фронтенда
            List<Map<String, Object>> usersData = usersPage.getContent().stream()
                    .map(this::convertUserToMap)
                    .collect(Collectors.toList());

            response.put("users", usersData);
            response.put("currentPage", usersPage.getNumber());
            response.put("totalItems", usersPage.getTotalElements());
            response.put("totalPages", usersPage.getTotalPages());
            response.put("hasNext", usersPage.hasNext());
            response.put("hasPrevious", usersPage.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Ошибка при загрузке пользователей: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/api/count")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getUsersCount() {
        Map<String, Long> response = new HashMap<>();
        try {
            response.put("count", userService.getUsersCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("count", 0L);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/api/{userId}/role")
    @ResponseBody
    public ResponseEntity<Map<String, String>> changeUserRole(
            @PathVariable Long userId,
            @RequestParam String newRole) {

        Map<String, String> response = new HashMap<>();

        try {
            userService.changeUserRole(userId, newRole);

            response.put("status", "success");
            response.put("message", "Роль пользователя успешно изменена на " + getRoleDisplayName(newRole));
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Внутренняя ошибка сервера");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/api/{userId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, String>> toggleUserStatus(
            @PathVariable Long userId,
            @RequestParam boolean active) {

        Map<String, String> response = new HashMap<>();

        try {
            userService.setUserActiveStatus(userId, active);

            String status = active ? "активирован" : "деактивирован";
            response.put("status", "success");
            response.put("message", "Пользователь успешно " + status);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Внутренняя ошибка сервера");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/api/roles-stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRolesStatistics() {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Long> rolesStats = userService.getRolesStatistics();
            response.put("rolesStats", rolesStats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Ошибка при получении статистики: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/api/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.findById(userId);
            return ResponseEntity.ok(convertUserToMap(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Вспомогательные методы
    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("email", user.getEmail());
        userMap.put("role", user.getRole() != null ? user.getRole().getName().name() : "ROLE_USER");
        userMap.put("enabled", user.isEnabled());
        return userMap;
    }

    private String getRoleDisplayName(String role) {
        switch (role) {
            case "ROLE_ADMIN": return "Администратор";
            case "ROLE_MODERATOR": return "Модератор";
            case "ROLE_USER": return "Пользователь";
            default: return role;
        }
    }
}