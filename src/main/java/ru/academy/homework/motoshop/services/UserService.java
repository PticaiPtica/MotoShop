package ru.academy.homework.motoshop.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.academy.homework.motoshop.entity.User;

import java.util.Map;

/**
 * Сервис для работы с пользователями.
 * Предоставляет методы для регистрации, проверки существования пользователей
 * и других операций, связанных с управлением учетными записями.
 */
public interface UserService {

    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param username имя пользователя (должно быть уникальным)
     * @param email    электронная почта пользователя (должна быть уникальной)
     * @param password пароль пользователя (будет зашифрован перед сохранением)
     * @throws IllegalArgumentException если пользователь с таким именем или email уже существует
     * @throws RuntimeException         при возникновении ошибок во время регистрации
     */
    void registerUser(String username, String email, String password);

    /**
     * Проверяет существование пользователя с указанным именем.
     *
     * @param username имя пользователя для проверки
     * @return true если пользователь с таким именем существует, иначе false
     */
    boolean existsByUsername(String username);

    /**
     * Проверяет существование пользователя с указанным email.
     *
     * @param email электронная почта для проверки
     * @return true если пользователь с таким email существует, иначе false
     */
    boolean existsByEmail(String email);

    /**
     * Находит пользователя по имени пользователя.
     *
     * @param username имя пользователя для поиска
     * @return объект User если найден, иначе null
     */
    User findByUsername(String username);

    Long count();

    Page<User> findAllUsers(Pageable pageable);

    long getUsersCount();

    void changeUserRole(Long userId, String newRole);

    void setUserActiveStatus(Long userId, boolean active);

    Map<String, Long> getRolesStatistics();

    User findById(Long userId);
}
