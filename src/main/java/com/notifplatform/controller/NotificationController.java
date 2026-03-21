package com.notifplatform.controller;

import com.notifplatform.dto.NotificationDto.SendRequest;
import com.notifplatform.dto.NotificationDto.SendResponse;
import com.notifplatform.dto.NotificationDto.StatusResponse;
import com.notifplatform.ratelimit.RateLimiter;
import com.notifplatform.service.NotificationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService service;
    private final RateLimiter rateLimiter;

    public NotificationController(NotificationService service, RateLimiter rateLimiter) {
        this.service = service;
        this.rateLimiter=rateLimiter;
    }

    @PostMapping("/send")
    public ResponseEntity<SendResponse> send(@Valid @RequestBody SendRequest request) {
        log.info("POST /send — userId={} type={}", request.getUserId(), request.getType());
        SendResponse response = service.send(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RateLimit-Limit", "5");
        headers.add("X-RateLimit-Remaining",
                String.valueOf(rateLimiter.getRemainingRequests(
                        request.getUserId(), request.getType().name())));
        headers.add("X-RateLimit-Reset",
                String.valueOf(rateLimiter.getSecondsUntilReset(
                        request.getUserId(), request.getType().name())));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .headers(headers)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StatusResponse> getStatus(@PathVariable Long id) {
        return ResponseEntity.ok(service.getStatus(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<StatusResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getByUser(userId));
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Long>> getAnalytics() {
        return ResponseEntity.ok(service.getSummary());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status",  "UP",
                "service", "notification-platform",
                "step",    "1"
        ));
    }

    @ExceptionHandler(NotificationService.RateLimitExceededException.class)
    public ResponseEntity<Map<String, String>> handleRateLimit(
            NotificationService.RateLimitExceededException ex) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)   // 429
                .body(Map.of(
                        "error",   "RATE_LIMIT_EXCEEDED",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(NotificationService.NotificationNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(
            NotificationService.NotificationNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().build();
    }
}
