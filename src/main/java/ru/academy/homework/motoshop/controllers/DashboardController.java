package ru.academy.homework.motoshop.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.academy.homework.motoshop.services.DashboardService;

import java.util.HashMap;
import java.util.Map;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin/dashboard")
    public String dashboardPage() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/dashboard/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();

        try {
            stats.put("usersCount", dashboardService.getUsersCount());
            stats.put("productsCount", dashboardService.getProductsCount());
            stats.put("ordersCount", dashboardService.getOrdersCount());

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            // В случае ошибки возвращаем нулевые значения
            stats.put("usersCount", 0L);
            stats.put("productsCount", 0L);
            stats.put("ordersCount", 0L);
            return ResponseEntity.ok(stats);
        }
    }
}