package com.smarthr.config;

import com.smarthr.entity.Role;
import com.smarthr.entity.User;
import com.smarthr.enums.RoleType;
import com.smarthr.repository.RoleRepository;
import com.smarthr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("========================================");
        log.info("  Initialisation des données de base...");
        log.info("========================================");

        // Créer les rôles par défaut
        for (RoleType roleType : RoleType.values()) {
            if (!roleRepository.existsByName(roleType)) {
                Role role = Role.builder()
                        .name(roleType)
                        .description("Rôle " + roleType.name())
                        .build();
                roleRepository.save(role);
                log.info("✅ Rôle créé : {}", roleType.name());
            } else {
                log.info("⏭️  Rôle déjà existant : {}", roleType.name());
            }
        }

        // Créer les utilisateurs par défaut pour chaque rôle
        createTestUserIfNotExist("admin", "admin@smarthr.com", "admin123", RoleType.ADMIN);
        createTestUserIfNotExist("hr", "hr@smarthr.com", "hr123", RoleType.HR_MANAGER);
        createTestUserIfNotExist("manager", "manager@smarthr.com", "manager123", RoleType.MANAGER);
        createTestUserIfNotExist("employee", "employee@smarthr.com", "employee123", RoleType.EMPLOYEE);

        log.info("========================================");
        log.info("  Initialisation terminée !");
        log.info("========================================");
    }

    private void createTestUserIfNotExist(String username, String email, String password, RoleType roleType) {
        if (!userRepository.existsByUsername(username)) {
            Role role = roleRepository.findByName(roleType)
                    .orElseThrow(() -> new RuntimeException("Rôle introuvable: " + roleType));

            User user = User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .enabled(true)
                    .roles(Set.of(role))
                    .build();

            userRepository.save(user);
            log.info("✅ Utilisateur de test créé -> Rôle: {} | Username: {} | Password: {}", roleType.name(), username, password);
        } else {
            log.info("⏭️  Utilisateur déjà existant : {}", username);
        }
    }
}

