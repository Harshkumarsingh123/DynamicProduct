package com.dynamic.product.config;

import com.dynamic.product.entity.Role;
import com.dynamic.product.entity.User;
import com.dynamic.product.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.phone}")
    private String adminPhone;

    @Value("${admin.password}")
    private String adminPassword;


    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        if(!userRepository.existsByEmail(adminEmail)){

            User admin=new User();
            admin.setName("System Admin");
            admin.setEmail(adminEmail);
            admin.setPhone(adminPhone);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);
        }
    }
}
