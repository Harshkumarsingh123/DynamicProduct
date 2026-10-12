package com.dynamic.product.security;

import com.dynamic.product.auth.entity.AccountStatus;
import com.dynamic.product.auth.entity.AppUser;
import com.dynamic.product.auth.entity.Role;
import com.dynamic.product.auth.repository.AppUserRepository;
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
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setAccountStatus(AccountStatus.ACTIVE);
            admin.setEmailVerified(true);
            admin.setRole(Role.ADMIN);

            appUserRepository.save(admin);
        }
    }
}
