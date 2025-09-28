package ru.academy.homework.motoshop.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.academy.homework.motoshop.entity.Role;
import ru.academy.homework.motoshop.entity.RoleName;
import ru.academy.homework.motoshop.entity.User;
import ru.academy.homework.motoshop.repository.RoleRepository;
import ru.academy.homework.motoshop.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void registerUser(String username, String email, String password) {
        if (existsByUsername(username)) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }
        if (existsByEmail(email)) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        // Находим роль USER по умолчанию
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("Роль USER не найдена в базе данных"));

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        // Исправлено: кодируем пароль перед сохранением
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(userRole);
        user.setEnabled(true);

        userRepository.save(user);
    }

    // Добавляем метод для проверки пароля при аутентификации
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }


    // Метод для обновления пароля пользователя
    public void updateUserPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Метод для проверки и аутентификации пользователя
    public User authenticateUser(String username, String password) {
        User user = findByUsername(username);
        if (user != null && user.isEnabled() &&
                passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElse(null);
    }

    @Override
    public Long count() {
        return userRepository.count();
    }

    @Override
    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public long getUsersCount() {
        return userRepository.count();
    }

    @Override
    public void changeUserRole(Long userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        try {
            RoleName roleName = RoleName.valueOf(newRole.toUpperCase());
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + newRole));

            user.setRole(role);
            userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Недопустимая роль: " + newRole);
        }
    }

    @Override
    public void setUserActiveStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        user.setEnabled(active);
        userRepository.save(user);
    }

    @Override
    public Map<String, Long> getRolesStatistics() {
        Map<String, Long> stats = new HashMap<>();

        for (RoleName roleName : RoleName.values()) {
            Long count = userRepository.countByRoleName(roleName);
            stats.put(roleName.name(), count);
        }

        return stats;
    }

    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }
}