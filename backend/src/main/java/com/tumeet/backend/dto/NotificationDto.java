package com.tumeet.backend.dto;

import com.tumeet.backend.model.Event;
import com.tumeet.backend.model.Notification;

import java.time.LocalDateTime;

public class NotificationDto {

    private Long id;
    private Long eventId;
    private String eventName;
    private String type;
    private String title;
    private String message;
    private String badge;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public NotificationDto() {
    }

    public static NotificationDto fromEntity(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());

        Event event = notification.getEvent();
        if (event != null) {
            dto.setEventId(event.getId());
            dto.setEventName(event.getEventName());
        }

        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        if ("BADGE_UPGRADED".equalsIgnoreCase(notification.getType())) {
            dto.setBadge(notification.getTitle());
        }
        dto.setIsRead(Boolean.TRUE.equals(notification.getIsRead()));
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
