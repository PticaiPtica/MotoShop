package ru.academy.homework.motoshop.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.academy.homework.motoshop.dto.UserDto;
import ru.academy.homework.motoshop.entity.Role;
import ru.academy.homework.motoshop.entity.RoleName;
import ru.academy.homework.motoshop.entity.User;
import ru.academy.homework.motoshop.repository.RoleRepository;
import ru.academy.homework.motoshop.repository.UserRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public AdminService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAllWithRole().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public void updateUserRole(Long userId, RoleName newRoleName) {
        User user = userRepository.findByIdWithRole(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Находим роль по имени
        Role newRole = roleRepository.findByName(newRoleName)
                .orElseThrow(() -> new RuntimeException("Роль не найдена: " + newRoleName));

        // Проверяем, что не понижаем роль последнего администратора
        if (user.getRole().getName() == RoleName.ROLE_ADMIN && newRoleName != RoleName.ROLE_ADMIN) {
            long adminCount = userRepository.countByRoleName(RoleName.ROLE_ADMIN);
            if (adminCount <= 1) {
                throw new RuntimeException("Нельзя понизить роль последнего администратора");
            }
        }

        user.setRole(newRole);
        userRepository.save(user);
    }

    public boolean toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Не позволяем заблокировать последнего администратора
        if (user.getRole().getName() == RoleName.ROLE_ADMIN && user.isEnabled()) {
            long activeAdminCount = userRepository.countByRoleNameAndEnabledTrue(RoleName.ROLE_ADMIN);
            if (activeAdminCount <= 1) {
                throw new RuntimeException("Нельзя заблокировать последнего активного администратора");
            }
        }

        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return user.isEnabled();
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findByIdWithRole(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Не позволяем удалить последнего администратора
        if (user.getRole().getName() == RoleName.ROLE_ADMIN) {
            long adminCount = userRepository.countByRoleName(RoleName.ROLE_ADMIN);
            if (adminCount <= 1) {
                throw new RuntimeException("Нельзя удалить последнего администратора");
            }
        }

        userRepository.delete(user);
    }

    public Map<String, Object> getAdminStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByEnabledTrue();
        long adminsCount = userRepository.countByRoleName(RoleName.ROLE_ADMIN);

        return Map.of(
                "totalUsers", totalUsers,
                "activeUsers", activeUsers,
                "adminsCount", adminsCount,
                "disabledUsers", totalUsers - activeUsers
        );
    }

    private UserDto convertToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().getName().name(), // Получаем имя Enum из Role
                user.isEnabled(),
                user.getCreatedAt() != null ?
                        user.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "N/A"
        );
    }
}