package ru.academy.homework.motoshop.Initializer;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.academy.homework.motoshop.entity.Role;
import ru.academy.homework.motoshop.entity.RoleName;
import ru.academy.homework.motoshop.repository.RoleRepository;
import ru.academy.homework.motoshop.repository.UserRepository;

@Component
public class DataInitializer {
    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void init() {
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
            }
        }
    }
}