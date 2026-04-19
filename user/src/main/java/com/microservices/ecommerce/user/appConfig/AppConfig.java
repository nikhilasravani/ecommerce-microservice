package com.microservices.ecommerce.user.appConfig;

import com.microservices.ecommerce.user.model.User;
import com.microservices.ecommerce.user.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner seedAdminUser(UserRepository userRepository,
                                           PasswordEncoder passwordEncoder,
                                           @Value("${app.admin.username}") String adminUsername,
                                           @Value("${app.admin.email}") String adminEmail,
                                           @Value("${app.admin.password}") String adminPassword) {
        return args -> {
            if (!userRepository.existsByEmail(adminEmail)) {
                User adminUser = User.builder()
                        .username(adminUsername)
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .role(Role.ROLE_ADMIN)
                        .build();
                userRepository.save(adminUser);
            }
        };
    }
}
