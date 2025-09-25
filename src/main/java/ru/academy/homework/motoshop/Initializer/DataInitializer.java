package ru.academy.homework.motoshop.Initializer;


import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.academy.homework.motoshop.entity.Role;
import ru.academy.homework.motoshop.entity.RoleName;
import ru.academy.homework.motoshop.repository.RoleRepository;


public class DataInitializer {
    private final RoleRepository roleRepository;
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void init() {
        try {
            for (RoleName roleName : RoleName.values()) {
                if (roleRepository.findByName(roleName).isEmpty()) {
                    Role role = new Role();
                    role.setName(roleName);
                    roleRepository.save(role);
                    logger.info("Created role: {}", roleName);
                }
            }
        } catch (Exception e) {
            logger.error("Error initializing roles: {}", e.getMessage());
            // Продолжить работу приложения без инициализации ролей
        }
    }
}