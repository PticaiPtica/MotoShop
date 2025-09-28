package ru.academy.homework.motoshop.entity;

import jakarta.persistence.*;
import ru.academy.homework.motoshop.model.Product;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
    }

    // Конструкторы
    public Order() {
    }

    public Order(User user, String shippingAddress, String phoneNumber, String customerName, String customerEmail) {
        this.user = user;
        this.shippingAddress = shippingAddress;
        this.phoneNumber = phoneNumber;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.status = OrderStatus.PENDING;
    }

    // Вспомогательные методы
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
        calculateTotalAmount();
    }

    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
        calculateTotalAmount();
    }

    public void addProduct(Product product, Integer quantity) {
        // Проверяем, есть ли уже этот продукт в заказе
        OrderItem existingItem = orderItems.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Если продукт уже есть, увеличиваем количество
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            existingItem.setPrice(product.getPrice()); // Обновляем цену
        } else {
            // Если продукта нет, создаем новый элемент заказа
            OrderItem newItem = new OrderItem(this, product, quantity, product.getPrice());
            orderItems.add(newItem);
        }
        calculateTotalAmount();
    }

    private void calculateTotalAmount() {
        this.totalAmount = orderItems.stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
        calculateTotalAmount();
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", orderDate=" + orderDate +
                ", itemsCount=" + (orderItems != null ? orderItems.size() : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

enum OrderStatus {
    PENDING("В обработке"),
    CONFIRMED("Подтвержден"),
    SHIPPED("Отправлен"),
    DELIVERED("Доставлен"),
    CANCELLED("Отменен");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}