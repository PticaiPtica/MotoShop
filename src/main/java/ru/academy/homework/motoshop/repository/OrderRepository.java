package ru.academy.homework.motoshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.academy.homework.motoshop.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

}