package ru.academy.homework.motoshop.repository;


import ru.academy.homework.motoshop.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);
}
