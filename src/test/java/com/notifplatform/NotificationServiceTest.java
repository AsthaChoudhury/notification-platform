package com.notifplatform;

import com.notifplatform.dto.NotificationDto.SendRequest;
import com.notifplatform.dto.NotificationDto.SendResponse;
import com.notifplatform.model.Notification;
import com.notifplatform.model.NotificationStatus;
import com.notifplatform.model.NotificationType;
import com.notifplatform.repository.NotificationRepository;
import com.notifplatform.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

/**
 * INTEGRATION TEST — runs with a real H2 database.
 *
 * @SpringBootTest loads the full Spring application context.
 * H2 is automatically configured from application.properties.
 * No mocking — we test the real flow end to end.
 *
 * @Transactional on the class means every test runs in a transaction
 * that is rolled back after the test. This keeps tests isolated —
 * data inserted in one test doesn't affect another.
 */
@SpringBootTest
@Transactional
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository repository;

    private SendRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new SendRequest();
        validRequest.setUserId(123L);
        validRequest.setType(NotificationType.EMAIL);
        validRequest.setMessage("Test notification message");
    }

    @Test
    @DisplayName("Send creates a record in the database")
    void send_createsNotificationRecord() {
        long countBefore = repository.count();

        notificationService.send(validRequest);

        long countAfter = repository.count();
        assertThat(countAfter).isEqualTo(countBefore + 1);
    }

    @Test
    @DisplayName("Send returns a response with notificationId")
    void send_returnsResponseWithId() {
        SendResponse response = notificationService.send(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.getNotificationId()).isNotNull();
        assertThat(response.getNotificationId()).isPositive();
    }

    @Test
    @DisplayName("Sent notification has userId and type stored correctly")
    void send_storesCorrectFields() {
        SendResponse response = notificationService.send(validRequest);

        Notification saved = repository.findById(response.getNotificationId()).orElseThrow();
        assertThat(saved.getUserId()).isEqualTo(123L);
        assertThat(saved.getType()).isEqualTo(NotificationType.EMAIL);
        assertThat(saved.getMessage()).isEqualTo("Test notification message");
    }

    @Test
    @DisplayName("getStatus throws exception for unknown ID")
    void getStatus_unknownId_throwsException() {
        assertThatThrownBy(() -> notificationService.getStatus(99999L))
                .isInstanceOf(NotificationService.NotificationNotFoundException.class)
                .hasMessageContaining("99999");
    }

    @Test
    @DisplayName("getByUser returns empty list for user with no notifications")
    void getByUser_noNotifications_returnsEmptyList() {
        var results = notificationService.getByUser(99999L);
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("getSummary returns counts map")
    void getSummary_returnsCounts() {
        notificationService.send(validRequest);

        var summary = notificationService.getSummary();
        assertThat(summary).containsKeys("total", "sent", "failed", "pending");
        assertThat(summary.get("total")).isPositive();
    }
}
