package com.dynamic.product.service;

import com.dynamic.product.entity.AppUser;
import com.dynamic.product.repository.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    public CustomUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "AppUser not found: " + email
                        )
                );

        return new org.springframework.security.core.userdetails.User(
                appUser.getEmail(),
                appUser.getPassword(),
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + appUser.getRole().name()
                        )
                )
        );
    }
}