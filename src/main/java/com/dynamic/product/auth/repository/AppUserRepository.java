package com.dynamic.product.auth.repository;

import com.dynamic.product.auth.entity.AccountStatus;
import com.dynamic.product.auth.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser,Long> {
    boolean existsByEmail(String email);

    Optional<AppUser> findByEmail(String email);

    List<AppUser> findByLastLoginAtBefore(LocalDateTime cutoff);

    List<AppUser>
    findByAccountStatusAndEmailVerifiedTrueAndPromotionalConsentTrueAndLastLoginAtBefore(
            AccountStatus status,
            LocalDateTime cutoff
    );

    List<AppUser>
    findByAccountStatusAndEmailVerifiedTrueAndLastLoginAtBefore(
            AccountStatus accountStatus,
            LocalDateTime cutoff
    );


    @Query("""
    SELECT u FROM AppUser u
    WHERE u.accountStatus = :status
      AND u.emailVerified = true
      AND u.promotionalConsent = true
      AND u.lastLoginAt < :inactiveCutoff
      AND (
          u.lastInactivityCampaignSentAt IS NULL
          OR u.lastInactivityCampaignSentAt <= :campaignCutoff
      )
    """)
    List<AppUser> findEligibleCampaignUsers(
            @Param("status") AccountStatus status,
            @Param("inactiveCutoff") LocalDateTime inactiveCutoff,
            @Param("campaignCutoff") LocalDateTime campaignCutoff
    );

}
