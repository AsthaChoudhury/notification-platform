package com.notifplatform.service;

import com.notifplatform.dto.NotificationDto.SendRequest;
import com.notifplatform.dto.NotificationDto.SendResponse;
import com.notifplatform.dto.NotificationDto.StatusResponse;
import com.notifplatform.model.Notification;
import com.notifplatform.model.NotificationStatus;
import com.notifplatform.ratelimit.RateLimiter;
import com.notifplatform.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository repository;
    private final EmailSender emailSender;
    private final SmsSender smsSender;
    private final PushSender pushSender;
    private final RateLimiter rateLimiter;

    public NotificationService(NotificationRepository repository,
                               EmailSender emailSender,
                               SmsSender smsSender,
                               PushSender pushSender,
                               RateLimiter rateLimiter) {
        this.repository   = repository;
        this.emailSender  = emailSender;
        this.smsSender    = smsSender;
        this.pushSender   = pushSender;
        this.rateLimiter  = rateLimiter;
    }

    @Transactional
    public SendResponse send(SendRequest request) {
        log.info("Received notification request — userId={} type={}",
                request.getUserId(), request.getType());

        if (!rateLimiter.isAllowed(request.getUserId(), request.getType().name())) {
            throw new RateLimitExceededException(
                    String.format("Rate limit exceeded for user %d on %s. Max %d per minute.",
                            request.getUserId(), request.getType().name(), 10));
        }
        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setType(request.getType());
        notification.setMessage(request.getMessage());
        notification.setStatus(NotificationStatus.PENDING);
        notification = repository.save(notification);

        try {
            switch (request.getType()) {
                case EMAIL -> emailSender.send(request.getUserId(), request.getMessage());
                case SMS   -> smsSender.send(request.getUserId(), request.getMessage());
                case PUSH  -> pushSender.send(request.getUserId(), request.getMessage());
            }
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            repository.save(notification);
            log.info("Notification [id={}] sent successfully", notification.getId());

        } catch (Exception ex) {
            log.error("Notification [id={}] failed: {}", notification.getId(), ex.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(ex.getMessage());
            repository.save(notification);
        }

        SendResponse response = new SendResponse();
        response.setNotificationId(notification.getId());
        response.setStatus(notification.getStatus().name());
        response.setMessage(notification.getStatus() == NotificationStatus.SENT
                ? "Notification sent successfully"
                : "Notification failed: " + notification.getErrorMessage());
        response.setUserId(request.getUserId());
        response.setType(request.getType().name());
        response.setTimestamp(LocalDateTime.now().toString());
        return response;
    }

    public StatusResponse getStatus(Long id) {
        Notification n = repository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(
                        "No notification found with id: " + id));
        return buildStatusResponse(n);
    }

    public List<StatusResponse> getByUser(Long userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(this::buildStatusResponse)
                .toList();
    }

    public Map<String, Long> getSummary() {
        Map<String, Long> summary = new LinkedHashMap<>();
        summary.put("total",   repository.count());
        summary.put("sent",    repository.countByStatus(NotificationStatus.SENT));
        summary.put("failed",  repository.countByStatus(NotificationStatus.FAILED));
        summary.put("pending", repository.countByStatus(NotificationStatus.PENDING));
        return summary;
    }

    private StatusResponse buildStatusResponse(Notification n) {
        StatusResponse r = new StatusResponse();
        r.setId(n.getId());
        r.setUserId(n.getUserId());
        r.setType(n.getType().name());
        r.setStatus(n.getStatus().name());
        r.setRetryCount(n.getRetryCount());
        r.setErrorMessage(n.getErrorMessage());
        r.setCreatedAt(n.getCreatedAt() != null ? n.getCreatedAt().toString() : null);
        r.setSentAt(n.getSentAt() != null ? n.getSentAt().toString() : null);
        return r;
    }

    public static class NotificationNotFoundException extends RuntimeException {
        public NotificationNotFoundException(String message) { super(message); }
    }

    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String message) { super(message); }
    }
}