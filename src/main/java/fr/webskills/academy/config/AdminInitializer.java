package fr.webskills.academy.config;

import fr.webskills.academy.domain.User;
import fr.webskills.academy.domain.enums.Role;
import fr.webskills.academy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {
    @Bean
    CommandLineRunner initAdmin(
            UserRepository users,
            PasswordEncoder encoder,
            @Value("${app.admin.email}") String email,
            @Value("${app.admin.password}") String password) {
        return args ->
                users.findByEmail(email)
                        .orElseGet(
                                () -> {
                                    User u = new User();
                                    u.setEmail(email);
                                    u.setPassword(encoder.encode(password));
                                    u.setFirstName("Admin");
                                    u.setLastName("WebSkills");
                                    u.setRole(Role.ADMIN);
                                    u.setEnabled(true);
                                    return users.save(u);
                                });
    }
}
