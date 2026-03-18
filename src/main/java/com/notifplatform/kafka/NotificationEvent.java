package com.notifplatform.kafka;

public class NotificationEvent {

    private Long notificationId;
    private Long userId;
    private String type;
    private String message;
    private int attemptNumber;
    private int maxRetries;

    public NotificationEvent() {}
    public NotificationEvent(Long notificationId, Long userId,
                             String type, String message,
                             int attemptNumber, int maxRetries) {
        this.notificationId = notificationId;
        this.userId         = userId;
        this.type           = type;
        this.message        = message;
        this.attemptNumber  = attemptNumber;
        this.maxRetries     = maxRetries;
    }
    public Long getNotificationId() { return notificationId; }
    public Long getUserId()         { return userId; }
    public String getType()         { return type; }
    public String getMessage()      { return message; }
    public int getAttemptNumber()   { return attemptNumber; }
    public int getMaxRetries()      { return maxRetries; }

    public void setNotificationId(Long notificationId) { this.notificationId = notificationId; }
    public void setUserId(Long userId)                 { this.userId = userId; }
    public void setType(String type)                   { this.type = type; }
    public void setMessage(String message)             { this.message = message; }
    public void setAttemptNumber(int attemptNumber)    { this.attemptNumber = attemptNumber; }
    public void setMaxRetries(int maxRetries)          { this.maxRetries = maxRetries; }

    @Override
    public String toString() {
        return "NotificationEvent{" +
                "notificationId=" + notificationId +
                ", userId=" + userId +
                ", type='" + type + '\'' +
                ", attempt=" + attemptNumber + "/" + maxRetries +
                '}';
    }
}