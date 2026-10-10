package com.dynamic.product.auth.repository;

import com.dynamic.product.auth.entity.Otp;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findTopByEmailOrderByCreatedAtDesc(String email);

    Optional<Otp> findTopByEmailAndVerifiedFalseOrderByCreatedAtDesc(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Otp o WHERE o.id = :id")
    Optional<Otp> findByIdForUpdate(@Param("id") Long id);
}
