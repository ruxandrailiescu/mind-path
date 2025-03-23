package ro.ase.acs.mind_path.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ro.ase.acs.mind_path.entity.User;
import ro.ase.acs.mind_path.entity.enums.UserRole;
import ro.ase.acs.mind_path.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner insertData() {
        return args -> {
            insertAdmin();
        };
    }

    private void insertAdmin() {
        String email = "admin@example.com";
        String password = "admin";
        String role = "ADMIN";
        String firstName = "admin";
        String lastName = "admin";

        if (userRepository.findByEmail(email)
                .isEmpty()) {
            User admin = new User();
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setRole(UserRole.valueOf(role));
            admin.setFirstName(firstName);
            admin.setLastName(lastName);
            userRepository.save(admin);
        }
    }
}
