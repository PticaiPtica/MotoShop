package ru.academy.homework.motoshop.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.academy.homework.motoshop.repository.OrderRepository;
import ru.academy.homework.motoshop.repository.ProductRepository;
import ru.academy.homework.motoshop.repository.UserRepository;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public DashboardServiceImpl(UserRepository userRepository,
                                ProductRepository productRepository,
                                OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public long getUsersCount() {
        try {
            return userRepository.count();
        } catch (Exception e) {
            // Логируем ошибку и возвращаем 0
            System.err.println("Ошибка при получении количества пользователей: " + e.getMessage());
            return 0L;
        }
    }

    @Override
    public long getProductsCount() {
        try {
            return productRepository.count();
        } catch (Exception e) {
            System.err.println("Ошибка при получении количества товаров: " + e.getMessage());
            return 0L;
        }
    }

    @Override
    public long getOrdersCount() {
        try {
            return orderRepository.count();
        } catch (Exception e) {
            System.err.println("Ошибка при получении количества заказов: " + e.getMessage());
            return 0L;
        }
    }
}