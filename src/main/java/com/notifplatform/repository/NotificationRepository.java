package com.notifplatform.repository;

import com.notifplatform.model.Notification;
import com.notifplatform.model.NotificationStatus;
import com.notifplatform.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByUserIdAndType(Long userId, NotificationType type);

    long countByStatus(NotificationStatus status);
}
