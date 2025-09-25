package ru.academy.homework.motoshop.Initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.academy.homework.motoshop.entity.RoleName;
import ru.academy.homework.motoshop.entity.Role;
import ru.academy.homework.motoshop.entity.User;
import ru.academy.homework.motoshop.repository.RoleRepository;
import ru.academy.homework.motoshop.repository.UserRepository;

/**
 * Инициализатор администратора при запуске приложения.
 * Создает учетную запись администратора, если она не существует.
 */
@Component
public class AdminInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${motoshop.admin.username:admin}")
    private String adminUsername;

    @Value("${motoshop.admin.email:admin@motoshop.ru}")
    private String adminEmail;

    @Value("${motoshop.admin.password:admin123}")
    private String adminPassword;

    @Value("${motoshop.admin.init-enabled:true}")
    private boolean adminInitEnabled;

    @Override
    public void run(String... args) throws Exception {
        if (!adminInitEnabled) {
            logger.info("Admin initialization is disabled");
            return;
        }

        // Проверяем существование ролей и создаем их если нужно
        initializeRoles();

        // Создаем администратора если его нет
        initializeAdminUser();
    }

    private void initializeRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = new Role(roleName);
                roleRepository.save(role);
                logger.info("Created role: {}", roleName);
            }
        }
    }

    private void initializeAdminUser() {
        if (userRepository.existsByUsername(adminUsername)) {
            logger.info("Admin user already exists: {}", adminUsername);
            return;
        }

        // Получаем роль администратора
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Admin role not found"));

        // Создаем пользователя администратора
        User adminUser = new User(adminUsername, adminEmail, passwordEncoder.encode(adminPassword));
        adminUser.setRole(adminRole);

        userRepository.save(adminUser);
        logger.info("Admin user created successfully: {}", adminUsername);
        logger.info("Default admin credentials - Username: {}, Password: {}", adminUsername, adminPassword);
        logger.warn("Please change the default admin password immediately!");
    }
}