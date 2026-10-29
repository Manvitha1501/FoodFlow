package com.foodflow.config;

import com.foodflow.document.UserDocument;
import com.foodflow.enums.Role;
import com.foodflow.enums.AccountStatus;
import com.foodflow.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail("admin@foodflow.com")) {
            UserDocument admin = UserDocument.builder()
                    .fullName("System Admin")
                    .email("admin@foodflow.com")
                    .password(passwordEncoder.encode("password123"))
                    .phoneNumber("+1234567890")
                    .role(Role.ADMIN)
                    .accountStatus(AccountStatus.ACTIVE)
                    .build();
            userRepository.save(admin);
            System.out.println(">>> DatabaseSeeder: Seeded admin account (admin@foodflow.com / password123)");
        }
    }
}
