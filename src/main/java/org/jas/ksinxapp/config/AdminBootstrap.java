package org.jas.ksinxapp.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jas.ksinxapp.model.User;
import org.jas.ksinxapp.repo.UserRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrap implements CommandLineRunner {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;
    @Value("${app.admin.password:}")
    private String adminPassword;
    @Value("${app.admin.full-name:Administrator}")
    private String adminFullName;

    @Override
    public void run(String... args) {
        if (adminEmail.isBlank() || adminPassword.isBlank() || userRepo.existsByRole(User.Role.ADMIN)) {
            return;
        }

        User admin = userRepo.findByEmail(adminEmail).orElseGet(User::new);
        admin.setEmail(adminEmail);
        admin.setFullName(adminFullName);
        admin.setRole(User.Role.ADMIN);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        userRepo.save(admin);

        log.info("Bootstrap ADMIN account ensured for {}", adminEmail);
    }
}
