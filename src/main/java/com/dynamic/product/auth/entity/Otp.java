package com.dynamic.product.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 254)
    private String email;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String otpHash;

    @Column(nullable = false)
    private Boolean verified = false;

    @Column(nullable = false)
    private Integer failedAttempts = 0;

    @Column(nullable = false)
    private Boolean locked = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (verified == null) {
            verified = false;
        }

        if (failedAttempts == null) {
            failedAttempts = 0;
        }

        if (locked == null) {
            locked = false;
        }
    }

}
