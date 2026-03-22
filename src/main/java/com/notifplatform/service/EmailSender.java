package com.notifplatform.service;

import com.notifplatform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSender {

    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    public EmailSender(JavaMailSender mailSender, UserRepository userRepository) {
        this.mailSender   = mailSender;
        this.userRepository = userRepository;
    }

    public void send(Long userId, String message) throws Exception {

        String recipientEmail = userRepository.findById(userId)
                .map(user -> user.getEmail())
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + userId +
                                ". Create the user first via POST /api/users"));

        log.info("━━━ EMAIL ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  To      : {}", recipientEmail);
        log.info("  Message : {}", message);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(recipientEmail);
        mail.setSubject("Notification from platform");
        mail.setText(message);

        mailSender.send(mail);
        log.info("✓ Email sent to [{}] for user [{}]", recipientEmail, userId);
    }
}