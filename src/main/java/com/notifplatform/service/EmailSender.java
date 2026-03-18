package com.notifplatform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailSender {

    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

    public void send(Long userId, String message) throws Exception {
        log.info("━━━ EMAIL ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  To      : user-{}-email@example.com", userId);
        log.info("  Message : {}", message);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        Thread.sleep(200);

        if (Math.random() < 0.10) {
            throw new RuntimeException("Email delivery failed: SMTP connection timeout");
        }

        log.info("✓ Email sent to user {}", userId);
    }
}