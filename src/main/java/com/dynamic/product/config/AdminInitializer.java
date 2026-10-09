package com.dynamic.product.config;

import com.dynamic.product.entity.Role;
import com.dynamic.product.entity.AppUser;
import com.dynamic.product.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.phone}")
    private String adminPhone;

    @Value("${admin.password}")
    private String adminPassword;


    public AdminInitializer(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        if(!appUserRepository.existsByEmail(adminEmail)){

            AppUser admin=new AppUser();
            admin.setName("System Admin");
            admin.setEmail(adminEmail);
            admin.setPhone(adminPhone);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);

            appUserRepository.save(admin);
        }
    }
}
