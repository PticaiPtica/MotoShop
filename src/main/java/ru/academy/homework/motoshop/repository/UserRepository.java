package ru.academy.homework.motoshop.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.academy.homework.motoshop.entity.Role;
import ru.academy.homework.motoshop.entity.RoleName;
import ru.academy.homework.motoshop.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    long countByRole(Role role);

    long countByEnabledTrue();

    // Загрузка пользователей с ролями
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role")
    List<User> findAllWithRole();

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.id = :id")
    Optional<User> findByIdWithRole(@Param("id") Long id);

    // Подсчет по ролям
    @Query("SELECT COUNT(u) FROM User u WHERE u.role.name = :roleName")
    long countByRoleName(@Param("roleName") RoleName roleName);


    @Query("SELECT COUNT(u) FROM User u WHERE u.role.name = :roleName AND u.enabled = true")
    long countByRoleNameAndEnabledTrue(@Param("roleName") RoleName roleName);


    // Поиск с пагинацией
    Page<User> findAll(Pageable pageable);

    // Поиск по роли с пагинацией
    @Query("SELECT u FROM User u WHERE u.role.name = :roleName")
    Page<User> findByRoleName(@Param("roleName") RoleName roleName, Pageable pageable);

    // Поиск активных/неактивных пользователей
    Page<User> findByEnabled(boolean active, Pageable pageable);

    // Комбинированный поиск
    @Query("SELECT u FROM User u WHERE u.role.name = :roleName AND u.enabled = :active")
    Page<User> findByRoleNameAndEnabled(@Param("roleName") RoleName roleName,
                                       @Param("active") boolean enabled,
                                       Pageable pageable);


}
