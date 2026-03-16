package com.notifplatform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = NotificationStatus.PENDING;
        }
        if (this.retryCount == null) {
            this.retryCount = 0;
        }
    }

    public Long getId()                  { return id; }
    public Long getUserId()              { return userId; }
    public NotificationType getType()    { return type; }
    public String getMessage()           { return message; }
    public NotificationStatus getStatus(){ return status; }
    public Integer getRetryCount()       { return retryCount; }
    public String getErrorMessage()      { return errorMessage; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public LocalDateTime getSentAt()     { return sentAt; }

    public void setId(Long id)                          { this.id = id; }
    public void setUserId(Long userId)                  { this.userId = userId; }
    public void setType(NotificationType type)          { this.type = type; }
    public void setMessage(String message)              { this.message = message; }
    public void setStatus(NotificationStatus status)    { this.status = status; }
    public void setRetryCount(Integer retryCount)       { this.retryCount = retryCount; }
    public void setErrorMessage(String errorMessage)    { this.errorMessage = errorMessage; }
    public void setCreatedAt(LocalDateTime createdAt)   { this.createdAt = createdAt; }
    public void setSentAt(LocalDateTime sentAt)         { this.sentAt = sentAt; }
}
