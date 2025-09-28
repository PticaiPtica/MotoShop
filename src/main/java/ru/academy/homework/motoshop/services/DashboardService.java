package ru.academy.homework.motoshop.services;

import org.springframework.stereotype.Service;


public interface DashboardService {
    long getUsersCount();
    long getProductsCount();
    long getOrdersCount();
}
