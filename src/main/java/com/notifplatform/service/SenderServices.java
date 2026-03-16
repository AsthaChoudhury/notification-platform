package com.notifplatform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class EmailSender {

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


@Service
class SmsSender {

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

@Service
class PushSender {

    private static final Logger log = LoggerFactory.getLogger(PushSender.class);

    public void send(Long userId, String message) throws Exception {
        log.info("━━━ PUSH ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  Device  : FCM-token-user-{}", userId);
        log.info("  Message : {}", message);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        Thread.sleep(80);

        if (Math.random() < 0.15) {
            throw new RuntimeException("Push delivery failed: device token expired");
        }

        log.info("✓ Push sent to user {}", userId);
    }
}
