package com.notifplatform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PushSender {

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
