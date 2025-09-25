package ru.academy.homework.motoshop.repository;


import ru.academy.homework.motoshop.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.academy.homework.motoshop.entity.RoleName;


import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Находит роль по имени.
     *
     * @param name имя роли для поиска
     * @return Optional с ролью, если найдена
     */
    Optional<Role> findByName(RoleName name);

    boolean existsByName(RoleName name);
}
