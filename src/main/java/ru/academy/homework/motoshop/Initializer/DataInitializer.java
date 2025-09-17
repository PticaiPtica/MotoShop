package ru.academy.homework.motoshop.Initializer;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.academy.homework.motoshop.entity.Role;
import ru.academy.homework.motoshop.entity.RoleName;
import ru.academy.homework.motoshop.repository.RoleRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            Role userRole = new Role(RoleName.ROLE_USER);
            roleRepository.save(userRole);

            Role adminRole = new Role(RoleName.ROLE_ADMIN);
            roleRepository.save(adminRole);

            Role moderatorRole = new Role(RoleName.ROLE_MODERATOR);
            roleRepository.save(moderatorRole);
        }
    }
}