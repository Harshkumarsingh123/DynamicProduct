package com.dynamic.product.common.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendOtpEmail(String recipientEmail, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(recipientEmail);
        message.setSubject("Email Verification OTP - PerfectKode");

        message.setText(
                "Hello,\n\n" +
                        "Welcome to PerfectKode!\n\n" +
                        "Your email verification OTP is: " + otp + "\n\n" +
                        "This OTP is valid for 10 minutes.\n" +
                        "For your security, please do not share this OTP with anyone.\n\n" +
                        "If you did not request this OTP, please ignore this email.\n\n" +
                        "Regards,\n" +
                        "PerfectKode Software Technologies Team"
        );

        javaMailSender.send(message);
    }

    public void sendWelcomeEmail(String recipientEmail) {

        System.out.println("Sending welcome email to: " + recipientEmail);
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(recipientEmail);
        message.setSubject("Welcome to PerfectKode!");

        message.setText(
                "Hello,\n\n" +
                        "Congratulations! Your email address has been successfully verified.\n\n" +
                        "You can now log in using your registered email address and password.\n\n" +
                        "Thank you for choosing PerfectKode. We look forward to being part of your journey.\n\n" +
                        "Best regards,\n" +
                        "PerfectKode Software Technologies Team"
        );

        javaMailSender.send(message);
    }
}
