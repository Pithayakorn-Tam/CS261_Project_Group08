package com.tumeet.backend.dto;

import com.tumeet.backend.model.ChatMessage;
import com.tumeet.backend.model.Event;

import java.time.LocalDateTime;

public class ChatRoomDto {

    private Long eventId;
    private String eventName;
    private String eventType;
    private Integer currentParticipants;
    private Integer eventSize;
    private String lifecycleStatus;
    private String lastMessage;
    private LocalDateTime lastMessageTime;

    public ChatRoomDto() {
    }

    public ChatRoomDto(Long eventId, String eventName, String eventType, Integer currentParticipants,
                       Integer eventSize, String lifecycleStatus, String lastMessage, LocalDateTime lastMessageTime) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventType = eventType;
        this.currentParticipants = currentParticipants;
        this.eventSize = eventSize;
        this.lifecycleStatus = lifecycleStatus;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }

    public static ChatRoomDto fromEvent(Event event, ChatMessage latestMessage) {
        if (event == null) {
            return null;
        }
        return new ChatRoomDto(
                event.getId(),
                event.getEventName(),
                event.getEventType(),
                event.getCurrentParticipants(),
                event.getEventSize(),
                event.getLifecycleStatus(),
                latestMessage != null ? latestMessage.getMessageText() : null,
                latestMessage != null ? latestMessage.getSentAt() : null
        );
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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Integer getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(Integer currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    public Integer getEventSize() {
        return eventSize;
    }

    public void setEventSize(Integer eventSize) {
        this.eventSize = eventSize;
    }

    public String getLifecycleStatus() {
        return lifecycleStatus;
    }

    public void setLifecycleStatus(String lifecycleStatus) {
        this.lifecycleStatus = lifecycleStatus;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
}
