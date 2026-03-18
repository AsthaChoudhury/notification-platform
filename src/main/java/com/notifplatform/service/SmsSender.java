package com.notifplatform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SmsSender {

    private static final Logger log = LoggerFactory.getLogger(SmsSender.class);

    public void send(Long userId, String message) throws Exception {
        log.info("━━━ SMS ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  To      : +91-XXXXX-user-{}", userId);
        log.info("  Message : {}", message);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        Thread.sleep(150);

        if (Math.random() < 0.05) {
            throw new RuntimeException("SMS delivery failed: carrier rejected");
        }

        log.info("✓ SMS sent to user {}", userId);
    }
}