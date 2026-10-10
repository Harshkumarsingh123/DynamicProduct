package com.dynamic.product.auth.service;

import com.dynamic.product.user.entity.AppUser;
import com.dynamic.product.user.repository.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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
                                "User not found: " + email
                        )
                );

        return User
                .withUsername(appUser.getEmail())
                .password(appUser.getPassword())
                .authorities(
                        new SimpleGrantedAuthority("ROLE_"+ appUser.getRole().name())
                )
                .disabled(!Boolean.TRUE.equals(appUser.getEmailVerified()))
                .build();

    }
}