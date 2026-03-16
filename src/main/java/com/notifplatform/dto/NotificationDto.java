package com.notifplatform.dto;

import com.notifplatform.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public class NotificationDto {

    public static class SendRequest {

        @NotNull(message = "userId is required")
        @Positive(message = "userId must be a positive number")
        private Long userId;

        @NotNull(message = "type is required — use EMAIL, SMS or PUSH")
        private NotificationType type;

        @NotBlank(message = "message is required")
        private String message;

        public Long getUserId()           { return userId; }
        public NotificationType getType() { return type; }
        public String getMessage()        { return message; }

        public void setUserId(Long userId)           { this.userId = userId; }
        public void setType(NotificationType type)   { this.type = type; }
        public void setMessage(String message)       { this.message = message; }
    }

    public static class SendResponse {

        private Long notificationId;
        private String status;
        private String message;
        private Long userId;
        private String type;
        private String timestamp;

        public Long getNotificationId()  { return notificationId; }
        public String getStatus()        { return status; }
        public String getMessage()       { return message; }
        public Long getUserId()          { return userId; }
        public String getType()          { return type; }
        public String getTimestamp()     { return timestamp; }

        public void setNotificationId(Long id)     { this.notificationId = id; }
        public void setStatus(String status)       { this.status = status; }
        public void setMessage(String message)     { this.message = message; }
        public void setUserId(Long userId)         { this.userId = userId; }
        public void setType(String type)           { this.type = type; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }

    public static class StatusResponse {

        private Long id;
        private Long userId;
        private String type;
        private String status;
        private Integer retryCount;
        private String errorMessage;
        private String createdAt;
        private String sentAt;

        public Long getId()              { return id; }
        public Long getUserId()          { return userId; }
        public String getType()          { return type; }
        public String getStatus()        { return status; }
        public Integer getRetryCount()   { return retryCount; }
        public String getErrorMessage()  { return errorMessage; }
        public String getCreatedAt()     { return createdAt; }
        public String getSentAt()        { return sentAt; }

        public void setId(Long id)                      { this.id = id; }
        public void setUserId(Long userId)              { this.userId = userId; }
        public void setType(String type)                { this.type = type; }
        public void setStatus(String status)            { this.status = status; }
        public void setRetryCount(Integer retryCount)   { this.retryCount = retryCount; }
        public void setErrorMessage(String msg)         { this.errorMessage = msg; }
        public void setCreatedAt(String createdAt)      { this.createdAt = createdAt; }
        public void setSentAt(String sentAt)            { this.sentAt = sentAt; }
    }
}
