package com.tumeet.backend.dto;

import com.tumeet.backend.model.Event;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EventDto {

    private Long id;
    private String eventName;
    private String eventType;
    private String location;
    private Integer eventSize;
    private Integer currentParticipants;
    private LocalDate eventDate;
    private String eventTime;
    private String status;
    private LocalDateTime createdAt;
    private CreatorDto creator;

    public EventDto() {
    }

    public static EventDto fromEntity(Event event) {
        if (event == null) {
            return null;
        }

        EventDto dto = new EventDto();
        dto.setId(event.getId());
        dto.setEventName(event.getEventName());
        dto.setEventType(event.getEventType());
        dto.setLocation(event.getLocation());
        dto.setEventSize(event.getEventSize());
        dto.setCurrentParticipants(event.getCurrentParticipants());
        dto.setEventDate(event.getEventDate());
        dto.setEventTime(event.getEventTime());
        dto.setStatus(event.getStatus());
        dto.setCreatedAt(event.getCreatedAt());

        if (event.getCreator() != null) {
            dto.setCreator(CreatorDto.fromEntity(event.getCreator()));
        }

        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getEventSize() {
        return eventSize;
    }

    public void setEventSize(Integer eventSize) {
        this.eventSize = eventSize;
    }

    public Integer getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(Integer currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public CreatorDto getCreator() {
        return creator;
    }

    public void setCreator(CreatorDto creator) {
        this.creator = creator;
    }
}
