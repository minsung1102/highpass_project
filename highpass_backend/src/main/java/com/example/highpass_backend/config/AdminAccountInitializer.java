package com.example.highpass_backend.config;

import com.example.highpass_backend.entity.user.User;
import com.example.highpass_backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@highpass.local}")
    private String adminEmail;

    @Value("${app.admin.password:Admin1234!}")
    private String adminPassword;

    @Value("${app.admin.nickname:관리자}")
    private String adminNickname;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        userRepository.findByEmail(adminEmail)
                .ifPresentOrElse(this::promoteToAdmin, this::createAdmin);
    }

    private void promoteToAdmin(User user) {
        if (user.getRole() != User.Role.ADMIN) {
            user.updateRole(User.Role.ADMIN);
        }
        if (user.getStatus() != User.Status.ACTIVE) {
            user.updateStatus(User.Status.ACTIVE);
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.encodePassword(passwordEncoder.encode(adminPassword));
        }
    }

    private void createAdmin() {
        User admin = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .nickname(adminNickname)
                .role(User.Role.ADMIN)
                .status(User.Status.ACTIVE)
                .build();

        userRepository.save(admin);
    }
}
