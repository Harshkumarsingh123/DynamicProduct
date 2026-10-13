
package com.dynamic.product.auth.service;

import com.dynamic.product.auth.entity.AccountStatus;
import com.dynamic.product.auth.entity.AppUser;
import com.dynamic.product.auth.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InactiveUserCampaignService {

    private static final Logger log =
            LoggerFactory.getLogger(InactiveUserCampaignService.class);

    private final AppUserRepository appUserRepository;
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public InactiveUserCampaignService(
            AppUserRepository appUserRepository,
            JavaMailSender javaMailSender) {
        this.appUserRepository = appUserRepository;
        this.javaMailSender = javaMailSender;
    }



    @Transactional(readOnly = true)
    public List<AppUser> getInactiveUsers() {
        LocalDateTime now = LocalDateTime.now();

        return appUserRepository.findEligibleCampaignUsers(
                AccountStatus.ACTIVE,
                now.minusDays(30),
                now.minusDays(30)
        );
    }

    @Scheduled(
            initialDelay = 10_000L,
            fixedDelay = 24 * 60 * 60 * 1000L
    )
    public void runInactiveUserCampaign() {

        log.info("Starting inactive user campaign");

        List<AppUser> inactiveUsers = getInactiveUsers();

        int eligibleUsers = inactiveUsers.size();
        int successfulEmails = 0;
        int failedEmails = 0;

        for (AppUser user : inactiveUsers) {
            try {
                sendPromotionalEmail(user);
                successfulEmails++;
            } catch (Exception exception) {
                failedEmails++;

                log.error(
                        "Failed to send promotional email to user ID {}",
                        user.getId(),
                        exception
                );
            }
        }

        deactivateUsersInactiveFor45Days();

        log.info(
                "Inactive campaign completed. Eligible: {}, Sent: {}, Failed: {}",
                eligibleUsers,
                successfulEmails,
                failedEmails
        );
    }


    private void sendPromotionalEmail(AppUser user) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(user.getEmail());
        message.setSubject("We miss you! Come back and explore");

        message.setText(
                "Hello,\n\n" +
                        "We noticed you haven't visited our website recently. " +
                        "Come back and explore our latest products and offers!\n\n" +
                        "Best regards,\nPerfectKode Software Technologies Team"
        );


        javaMailSender.send(message);

        user.setLastInactivityCampaignSentAt(LocalDateTime.now());
        appUserRepository.save(user);

        log.info("Promotional email sent successfully to user ID {}", user.getId());
    }

    @Transactional
    public int deactivateUsersInactiveFor45Days() {

        LocalDateTime cutoff = LocalDateTime.now().minusDays(45);

        List<AppUser> usersToDeactivate = appUserRepository
                        .findByAccountStatusAndEmailVerifiedTrueAndLastLoginAtBefore(
                                AccountStatus.ACTIVE,
                                cutoff
                        );

        for (AppUser user : usersToDeactivate) {
            user.setAccountStatus(AccountStatus.INACTIVE);
        }

        appUserRepository.saveAll(usersToDeactivate);

        log.info(
                "Deactivated {} users due to 45 days of inactivity",
                usersToDeactivate.size()
        );

        return usersToDeactivate.size();
    }
}
