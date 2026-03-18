package com.notifplatform.kafka;

import com.notifplatform.model.Notification;
import com.notifplatform.model.NotificationStatus;
import com.notifplatform.repository.NotificationRepository;
import com.notifplatform.service.EmailSender;
import com.notifplatform.service.PushSender;
import com.notifplatform.service.SmsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final EmailSender emailSender;
    private final SmsSender smsSender;
    private final PushSender pushSender;
    private final NotificationRepository repository;
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public NotificationConsumer(EmailSender emailSender,
                                SmsSender smsSender,
                                PushSender pushSender,
                                NotificationRepository repository,
                                KafkaTemplate<String, NotificationEvent> kafkaTemplate) {
        this.emailSender   = emailSender;
        this.smsSender     = smsSender;
        this.pushSender    = pushSender;
        this.repository    = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // ── EMAIL CONSUMER ──────────────────────────────────────────────

    /**
     * Listens to notification.email topic.
     * Every message that lands in that topic calls this method.
     *
     * groupId = "notification-workers"
     * If you run 2 instances of this app, Kafka splits the partitions
     * between them — no duplicate sends.
     */
    @KafkaListener(topics = "notification.email", groupId = "notification-workers")
    public void consumeEmail(NotificationEvent event) {
        log.info("consumed from notification.email → {}", event);
        processEvent(event);
    }

    // ── SMS CONSUMER ────────────────────────────────────────────────

    @KafkaListener(topics = "notification.sms", groupId = "notification-workers")
    public void consumeSms(NotificationEvent event) {
        log.info("Consumed from notification.sms → {}", event);
        processEvent(event);
    }
    @KafkaListener(topics = "notification.push", groupId = "notification-workers")
    public void consumePush(NotificationEvent event) {
        log.info("Consumed from notification.push → {}", event);
        processEvent(event);
    }

    @KafkaListener(topics = "notification.dead-letter", groupId = "dead-letter-processor")
    public void consumeDeadLetter(NotificationEvent event) {
        log.error("DEAD LETTER received — notificationId={} userId={} type={}",
                event.getNotificationId(), event.getUserId(), event.getType());
        updateStatus(event.getNotificationId(), NotificationStatus.DEAD_LETTER,
                "Exhausted all " + event.getMaxRetries() + " retries");
    }

    private void processEvent(NotificationEvent event) {
        updateStatus(event.getNotificationId(), NotificationStatus.PROCESSING, null);

        try {
            switch (event.getType()) {
                case "EMAIL" -> emailSender.send(event.getUserId(), event.getMessage());
                case "SMS"   -> smsSender.send(event.getUserId(), event.getMessage());
                case "PUSH"  -> pushSender.send(event.getUserId(), event.getMessage());
                default      -> throw new IllegalArgumentException("Unknown type: " + event.getType());
            }
            markSent(event.getNotificationId());
            log.info("✓ Notification [id={}] delivered successfully", event.getNotificationId());

        } catch (Exception ex) {
            log.error("✗ Notification [id={}] failed on attempt {}/{}: {}",
                    event.getNotificationId(),
                    event.getAttemptNumber(),
                    event.getMaxRetries(),
                    ex.getMessage());

            handleFailure(event, ex);
        }
    }

    private void handleFailure(NotificationEvent event, Exception ex) {
        if (event.getAttemptNumber() < event.getMaxRetries()) {
            log.info("↺ Retrying notification [id={}] — attempt {}/{}",
                    event.getNotificationId(),
                    event.getAttemptNumber() + 1,
                    event.getMaxRetries());

            updateStatus(event.getNotificationId(), NotificationStatus.RETRYING, ex.getMessage());
            incrementRetryCount(event.getNotificationId());
            NotificationEvent retryEvent = new NotificationEvent(
                    event.getNotificationId(),
                    event.getUserId(),
                    event.getType(),
                    event.getMessage(),
                    event.getAttemptNumber() + 1,
                    event.getMaxRetries()
            );
            String retryTopic = switch (event.getType()) {
                case "EMAIL" -> "notification.email";
                case "SMS"   -> "notification.sms";
                case "PUSH"  -> "notification.push";
                default      -> "notification.dead-letter";
            };

            kafkaTemplate.send(retryTopic, String.valueOf(event.getUserId()), retryEvent);

        } else {
            log.error("☠ Max retries exhausted for notification [id={}]. Sending to dead-letter.",
                    event.getNotificationId());

            kafkaTemplate.send("notification.dead-letter",
                    String.valueOf(event.getUserId()), event);
        }
    }
    private void updateStatus(Long id, NotificationStatus status, String errorMessage) {
        Optional<Notification> opt = repository.findById(id);
        if (opt.isPresent()) {
            Notification n = opt.get();
            n.setStatus(status);
            if (errorMessage != null) n.setErrorMessage(errorMessage);
            repository.save(n);
        }
    }

    private void markSent(Long id) {
        Optional<Notification> opt = repository.findById(id);
        if (opt.isPresent()) {
            Notification n = opt.get();
            n.setStatus(NotificationStatus.SENT);
            n.setSentAt(LocalDateTime.now());
            n.setErrorMessage(null);
            repository.save(n);
        }
    }

    private void incrementRetryCount(Long id) {
        Optional<Notification> opt = repository.findById(id);
        if (opt.isPresent()) {
            Notification n = opt.get();
            n.setRetryCount(n.getRetryCount() + 1);
            repository.save(n);
        }
    }
}